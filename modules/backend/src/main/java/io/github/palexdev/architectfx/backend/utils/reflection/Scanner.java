package io.github.palexdev.architectfx.backend.utils.reflection;

import java.nio.file.Path;
import java.util.*;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.utils.ImportsSet;
import org.tinylog.Logger;

/// This class offers core functionalities to the loading process. Thanks to the third-party library
/// [ClassGraph](https://github.com/classgraph/classgraph), it allows users to write documents _without imports_
/// and without fully-qualified names.
///
/// There is a little catch though: you are allowed to skip imports as far as no conflicts occur. For example, if you
/// use a class `User` in the document, but on the classpath there are multiple classes with the same simple name `User`,
/// this is a conflict that we can't solve automatically. In such cases, you can either add an import, or use the
/// fully-qualified name.
///
/// Depending on the context, the scanner is capable of inspecting only the document's dependencies or the whole
/// classpath (with some exceptions).
///
/// Depends on the [DependencyManager], see [ScanScope].
///
/// **Important Notes**
///
/// Scans can be quite heavy on performance. The more scans are issued, the more time is required to load a document.
/// There are many ways to mitigate this. On the system side, there are caches in place to avoid running scans with
/// the same query more than once. On the user side, you can resort to imports _OR_ you can pre-populate the search cache
/// by using [#addToScanCache(Class\[\])]. Another optimization on the system side which is crucial to mention, is that
/// the system already pre-populates the search cache with some common classes specified by [#CORE_CLASS_CACHE].
/// You see, by default [ClassGraph] does not scan the JDK because it requires a **LOT** of time and memory. The result
/// is that when you use something like this in a document: `Integer.MAX_VALUE`, it ends up generating a [ClassNotFoundException].
/// So, the solution I came up with was to pre-populate the cache with some common-use classes.

public class Scanner {
    //================================================================================
    // Static Properties
    //================================================================================

    /// An array of common/needed classes used to pre-populate the search cache.
    /// This way we avoid scanning too many things (like the entire JDK for example)
    public static final Class<?>[] CORE_CLASS_CACHE = new Class<?>[]{
        // Primitives
        boolean.class,
        byte.class,
        char.class,
        double.class,
        float.class,
        int.class,
        long.class,
        short.class,
        // Wrappers
        Boolean.class,
        Byte.class,
        Character.class,
        Double.class,
        Float.class,
        Integer.class,
        Long.class,
        Short.class,
        // "Utility"
        Object.class,
        Class.class,
        Enum.class,
        Objects.class,
        Optional.class,
        Math.class,
        String.class,
        System.class,
        // Collections
        Arrays.class,
        Collections.class,
        List.class,
        Map.class,
        Set.class
    };

    //================================================================================
    // Properties
    //================================================================================
    private final DependencyManager dm;
    private final Set<String> imports;
    private final Map<String, Class<?>> classCache = new HashMap<>();

    //================================================================================
    // Constructors
    //================================================================================
    public Scanner(DependencyManager dm) {
        this(dm, new ImportsSet());
    }

    public Scanner(DependencyManager dm, Set<String> imports) {
        this.dm = dm;
        this.imports = imports;
        addToScanCache(CORE_CLASS_CACHE);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Attempts at finding the corresponding class given a name which could be either simple or fully qualified.
    ///
    /// In case it's fully qualified, simply delegates to [DependencyManager#loadClass(String)] to load the class.
    ///
    /// In case it's already been found by a previous scan, returns from cache.
    ///
    /// Next thing to check are imports. If the class in either allowed by a specific import or a star import, caches
    /// the scan and returns the class loaded by [DependencyManager#loadClass(String)]
    ///
    /// The last resort is to use [ClassGraph] to scan the classpath and the dependencies, delegates to
    /// [#searchClasses(String, ScanScope)]. At this point three things can happen:
    /// 1) The scan results are empty, throws a [ClassNotFoundException]
    /// 2) There are more than one result, throws a [IllegalArgumentException]. The user is expected to fix the conflicts
    /// in some way
    /// 3) The single result is loaded by [DependencyManager#loadClass(String)], cached and returned.
    public Class<?> findClass(String className) throws ClassNotFoundException {
        // Check if it's a fully qualified name
        // (naive approach, contains dot)
        // In such case no need to classCache
        if (className.contains(".")) {
            Class<?> klass = dm.loadClass(className);
            if (klass != null) return klass;
            throw new ClassNotFoundException("Class not found: " + className);
        }

        // Simple names handling
        // Check classCache first
        if (classCache.containsKey(className))
            return classCache.get(className);

        // Then try with imports and add to classCache
        for (String imp : imports) {
            try {
                Class<?> klass = switch (imp) {
                    case String s when s.endsWith(className) -> dm.loadClass(s);
                    case String s when s.endsWith("*") -> {
                        String pkg = s.substring(0, s.lastIndexOf('.'));
                        yield dm.loadClass(pkg + "." + className);
                    }
                    default -> null;
                };

                if (klass != null) {
                    classCache.put(className, klass);
                    return klass;
                }
            } catch (ClassNotFoundException ignored) {}
        }

        // Last resort, use ClassGraph
        Logger.warn("Resorting to ClassGraph to find class {}, this may take a while for the first scan...", className);
        ClassInfoList results = searchClasses(className, ScanScope.DEPS);
        if (results.isEmpty()) throw new ClassNotFoundException("Class not found: " + className);
        if (results.size() > 1) throw new IllegalArgumentException(
            "More than one class for name %s have been found: %s".formatted(className, Arrays.toString(results.toArray()))
        );

        String fqName = results.getFirst().getName();
        Class<?> klass = dm.loadClass(fqName);
        if (klass == null)
            throw new ClassNotFoundException("Failed to load class: " + fqName);
        Logger.trace("Found class: {}", fqName);
        classCache.put(className, klass);
        return klass;
    }

    /// Uses [ClassGraph] to search for all classes with the given `className`.
    /// If the results are not empty, also caches the search for faster subsequent calls.
    ///
    /// @see ScanScope
    public ClassInfoList searchClasses(String className, ScanScope scope) {
        // Determine if the className is simple or fully qualified and set the query accordingly
        String query = className.contains(".") ?
            className :
            "*." + className;
        Logger.trace("Scan query: {}", query);

        ClassGraph cg = scope.build(dm)
            .acceptClasses(query);
        try (ScanResult res = cg.scan()) {
            Logger.trace("ClassGraph scan terminated...");
            return res.getAllClasses();
        } catch (Exception ex) {
            Logger.error("Error occurred during ClassGraph scan: {}", ex.getMessage());
            return ClassInfoList.emptyList();
        }
    }

    /// Adds the given classes to the `searchCache`, which not only can be used to avoid conflicts without adding imports
    /// in the document, but also makes [#findClass(String)] scans faster (because of course it's not going to rely on [ClassGraph]).
    public void addToScanCache(Class<?>... classes) {
        for (Class<?> klass : classes) {
            classCache.put(klass.getSimpleName(), klass);
        }
    }

    //================================================================================
    // Inner Classes
    //================================================================================

    /// An enum factory which builds a [ClassGraph] instance based on how many things we want to scan.
    ///
    /// For a targeted scan, which only includes the dependencies specified by the [DependencyManager] and the
    /// classpath, use [#DEPS]. Otherwise, use [#ALL]

    public enum ScanScope {
        ALL {
            @Override
            public ClassGraph build(DependencyManager dm) {
                return new ClassGraph();
            }
        },
        DEPS {
            @Override
            public ClassGraph build(DependencyManager dm) {
                Set<Path> deps = dm.dependencies();
                if (deps.isEmpty()) {
                    Logger.debug("No dependencies found to execute ClassGraph scan with DEPS scope, fallback to ALL...");
                    return ALL.build(dm);
                }
                return new ClassGraph()
                    .overrideClassLoaders(
                        ClassLoader.getSystemClassLoader(),
                        dm.loader()
                    );
            }
        },
        ;

        public abstract ClassGraph build(DependencyManager dm);
    }
}
