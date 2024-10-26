/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.architectfx.backend.utils.reflection;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.utils.ImportsSet;
import io.github.palexdev.architectfx.backend.yaml.Keyword;
import org.tinylog.Logger;

/// This class offers core functionalities to the deserialization process. Thanks to the third-party library
/// [ClassGraph](https://github.com/classgraph/classgraph), allows users to write documents _without imports_ and without
/// full-qualified names. It's also responsible for locating URL resources.
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
/// There are many ways to mitigate this. On the system side, there are many caches in place to avoid running scans with
/// the same query more than once. On the user side, you can resort to imports _OR_ you can pre-populate the search cache
/// by using [#addToScanCache(Class\[\])]. Another optimization on the system side which is crucial to mention, is that
/// the system already pre-populates the search cache with some common classes specified by [#CORE_CLASS_CACHE].
/// You see, by default [ClassGraph] does not scan the JDK because it requires a **LOT** of time and memory. The result
/// is that when you use something like this in a document: `Integer.MAX_VALUE`, it would generate a [ClassNotFoundException].
/// So, the solution I came up with was to pre-populate the cache with some common-use classes.
public class ClassScanner {
    //================================================================================
    // Static Properties
    //================================================================================

    /// With this prefix, the system can tell whether a string might be a resource (so we need to scan for it) or not.
    public static final String RESOURCES_PREFIX = "@";

    /// An array of common/needed classes used to pre-populate the search cache.
    /// This way we avoid scanning to many things (like the entire JDK for example)
    ///
    /// (Since it would be too long to list all the classes here, please bear with me and check the
    ///  [source](https://github.com/palexdev/ArchitectFX/blob/main/src/main/java/io/github/palexdev/architectfx/utils/reflection/ClassScanner.java))
    public static final Class<?>[] CORE_CLASS_CACHE = new Class<?>[]{
        // ArchitectFX
        Keyword.class,
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

    /// Classpath entries got from `System.getProperty("java.class.path")` and split by `;`.
    ///
    /// Used by [ClassGraph], could be useful for future optimizations.
    public static final String[] PROJECT_CLASSPATH = Arrays.stream(System.getProperty("java.class.path").split(";"))
        .toArray(String[]::new);

    //================================================================================
    // Properties
    //================================================================================
    private DependencyManager dm;
    private final Set<String> imports = new ImportsSet();
    private final Map<String, ClassInfoList> scanCache = new HashMap<>();
    private final Map<String, Class<?>> searchCache = new HashMap<>();
    private final Map<String, URI> resourceCache = new HashMap<>();

    //================================================================================
    // Constructors
    //================================================================================
    public ClassScanner(DependencyManager dm) {
        this.dm = dm;
        addToScanCache(CORE_CLASS_CACHE);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Attempts at finding the corresponding class given a name which could be either simple or fully qualified.
    ///
    /// In case it's fully qualified, simply delegated to [DependencyManager#loadClass(String)] to get the class.
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
    public Class<?> findClass(String className) throws ClassNotFoundException, IllegalArgumentException {
        // Check if it's a fully qualified name
        // (naive approach, contains dot)
        // In such case no need to cache
        if (className.contains(".")) {
            Class<?> klass = dm.loadClass(className);
            if (klass != null) return klass;
            throw new ClassNotFoundException("Class not found: " + className);
        }

        // Simple names handling
        // Check cache first
        if (searchCache.containsKey(className)) return searchCache.get(className);

        // Then try with imports and add to cache
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
                    searchCache.put(className, klass);
                    return klass;
                }
            } catch (ClassNotFoundException ignored) {}
        }
        if (!imports.isEmpty())
            Logger.debug("Class {} not found in imports\n{}", className, Arrays.toString(imports.toArray()));

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
        searchCache.put(className, klass);
        return klass;
    }

    /// Naive approach to find a URI resource, given its name and the scan's scope.
    ///
    /// If the resource was already found by a previous scan, returns from cache.
    ///
    /// Otherwise, resorts to [ClassGraph] to find a complete list of the resources on the classpath, which are then
    /// filtered by a [Pattern] built on the given name.
    ///
    /// Similarly to [#findClass(String)], in case of no match, our multiple matches a warning is issued and `null` will
    /// be returned. Otherwise, caches and returns the resource.
    public URI findResource(String resource, ScanScope scope) {
        // Check cache first
        if (resourceCache.containsKey(resource)) return resourceCache.get(resource);

        // Scan
        ClassGraph cg = scope.build(dm);
        try (ScanResult res = cg.scan()) {
            Logger.trace("ClassGraph scan terminated...");
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(resource) + "$");
            Logger.debug("Filtering resources by pattern: {}", pattern);
            ResourceList resources = res.getResourcesMatchingPattern(pattern);
            if (resources.size() == 1) {
                URI uri = resources.getFirst().getURI();
                resourceCache.put(resource, uri);
                return uri;
            }

            if (resources.isEmpty()) {
                Logger.warn("Resource {} not found", resource);
            } else {
                Logger.warn("More than one resource found for name {}", resource);
            }
            return null;
        }
    }

    /// Uses [ClassGraph] to search for all classes with the given `className`.
    /// If the results are not empty, also caches the search for faster subsequent calls.
    ///
    /// @see ScanScope
    public ClassInfoList searchClasses(String className, ScanScope scope) {
        // Check cache first
        if (scanCache.containsKey(className)) return scanCache.get(className);

        // Determine if the className is simple or fully qualified
        // and set the query accordingly
        String query = className.contains(".") ?
            className :
            "*." + className;
        Logger.trace("Scan query: {}", query);

        ClassGraph cg = scope.build(dm)
            .acceptClasses(query);
        try (ScanResult res = cg.scan()) {
            Logger.trace("ClassGraph scan terminated...");
            Logger.trace("Caching scan results...");
            ClassInfoList list = res.getAllClasses();
            if (!list.isEmpty()) scanCache.put(className, list);
            return list;
        } catch (Exception ex) {
            Logger.error("Error occurred during ClassGraph scan: {}", ex.getMessage());
            return ClassInfoList.emptyList();
        }
    }

    /// Adds the given classes to the `searchCache`, which not only can be used to avoid conflicts without adding imports
    /// in the document, but also makes [#findClass(String)] scans faster (because of course it's not going to rely on [ClassGraph]).
    public void addToScanCache(Class<?>... classes) {
        for (Class<?> klass : classes) {
            searchCache.put(klass.getSimpleName(), klass);
        }
    }

    /// Sets the imports statemets used by [#findClass(String)] to the given collection of imports.
    /// Needless to say, this is a _set_ opearation, previous imports are going to be removed.
    public void setImports(Collection<String> imports) {
        this.imports.clear();
        this.imports.addAll(imports);
    }

    /// Closes all the scans (if any is still open), clears all the caches, and sets the [DependencyManager] reference to `null`
    public void dispose() {
        ScanResult.closeAll();
        dm = null;
        imports.clear();
        scanCache.clear();
        searchCache.clear();
    }

    //================================================================================
    // Internal Classes
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
                    Logger.warn("No dependencies found to execute ClassGraph scan with DEPS scope, fallback to ALL...");
                    return ALL.build(dm);
                }

                return new ClassGraph()
                    .overrideClasspath(deps.toArray())
                    .overrideClasspath((Object[]) PROJECT_CLASSPATH);
            }
        },
        ;

        public abstract ClassGraph build(DependencyManager dm);
    }
}