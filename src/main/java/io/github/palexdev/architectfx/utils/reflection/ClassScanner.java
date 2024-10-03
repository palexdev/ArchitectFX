/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/MaterialFX)
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

package io.github.palexdev.architectfx.utils.reflection;

import java.io.File;
import java.util.*;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.utils.ImportsSet;
import org.tinylog.Logger;

// TODO this should not be static! Neither the DependencyManager
public class ClassScanner {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final Set<String> imports = new ImportsSet();
    private static final Map<String, ClassInfoList> scanCache = new HashMap<>();
    private static final Map<String, Class<?>> searchCache = new HashMap<>();

    //================================================================================
    // Constructors
    //================================================================================
    private ClassScanner() {}

    //================================================================================
    // Static Methods
    //================================================================================

    public static Class<?> findClass(String className) throws ClassNotFoundException {
        DependencyManager dm = DependencyManager.instance();
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
            } catch (ClassNotFoundException ex) {
                Logger.trace("Invalid name or class not found: {}", ex);
            }
        }

        // Last resort, use ClassGraph
        Logger.warn("Resorting to ClassGraph to find class {}, this may take a while for the first scan...", className);
        ClassInfoList results = searchClasses(className, ScanScope.DEPS);
        if (results.isEmpty()) throw new ClassNotFoundException("Class not found: " + className);
        if (results.size() > 1) throw new IllegalStateException(
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

    public static ClassInfoList searchClasses(String className, ScanScope scope) {
        // Check cache first
        if (scanCache.containsKey(className)) return scanCache.get(className);

        // Determine if the className is simple or fully qualified
        // and set the query accordingly
        String query = className.contains(".") ?
            className :
            "*." + className;
        Logger.trace("Scan query: {}", query);

        ClassGraph cg = scope.build()
            .acceptClasses(query);
        try (ScanResult res = cg.scan()) {
            Logger.trace("ClassGraph scan terminated...");
            Logger.trace("Caching scan results...");
            ClassInfoList list = res.getAllClasses()
                .filter(i -> !i.getPackageName().startsWith("java.awt") &&
                             !i.getPackageName().startsWith("javax.swing")
                );
            scanCache.put(className, list);
            return list;
        } catch (Exception ex) {
            Logger.error("Error occurred during ClassGraph scan: {}", ex.getMessage());
            return ClassInfoList.emptyList();
        }
    }

    public static void setImports(Collection<String> imports) {
        ClassScanner.imports.clear();
        ClassScanner.imports.addAll(imports);
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    public enum ScanScope {
        ALL {
            @Override
            public ClassGraph build() {
                return new ClassGraph()
                    .enableSystemJarsAndModules();
            }
        },
        DEPS {
            @Override
            public ClassGraph build() {
                Set<File> deps = DependencyManager.instance().dependencies();
                if (deps.isEmpty()) {
                    Logger.error("No dependencies found to execute ClassGraph scan with DEPS scope, fallback to ALL...");
                    return ALL.build();
                }

                return new ClassGraph()
                    .enableSystemJarsAndModules()
                    .overrideClasspath(deps.toArray())
                    .overrideClasspath(System.getProperty("java.class.path"));
            }
        },
        ;

        public abstract ClassGraph build();
    }
}