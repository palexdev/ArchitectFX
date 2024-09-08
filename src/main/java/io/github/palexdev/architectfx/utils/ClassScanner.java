package io.github.palexdev.architectfx.utils;
import java.io.File;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tinylog.Logger;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.github.palexdev.architectfx.deps.DependencyManager;

public class ClassScanner {
	//================================================================================
	// Static Properties
	//================================================================================
	private static final Map<String, ClassInfoList> scanCache = new HashMap<>();

	//================================================================================
	// Constructors
	//================================================================================
	
	private ClassScanner() {}

	//================================================================================
	// Static Methods
	//================================================================================

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
			ClassInfoList list = res.getAllClasses();
			scanCache.put(className, list);
			return list;
		} catch (Exception ex) {
			Logger.error("Error occurred during ClassGraph scan: {}", ex.getMessage());
			return ClassInfoList.emptyList();
		}
	}

	//================================================================================
	// Internal Classes
	//================================================================================
	public enum ScanScope {
		ALL {
			@Override
			public ClassGraph build() {
				return new ClassGraph();
			}
		},
		DEPS {
			@Override
			public ClassGraph build() {
				Set<File> deps = DependencyManager.instance().getDependencies();
				if (deps.isEmpty()) {
					Logger.error("No dependencies found to execute ClassGraph scan with DEPS scope, fallback to ALL...");
					return ALL.build();
				}
			    return new ClassGraph()
			    	.overrideClasspath(deps.stream().map(File::getAbsolutePath).toArray());
			}
		},
		;

		public abstract ClassGraph build();
	}
}