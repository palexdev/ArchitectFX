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