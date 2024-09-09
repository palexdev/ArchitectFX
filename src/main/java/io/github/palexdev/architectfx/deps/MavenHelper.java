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

package io.github.palexdev.architectfx.deps;

import java.io.File;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;

public class MavenHelper {
	//================================================================================
	// Properties
	//================================================================================
	private final MavenResolverSystem resolver;

	//================================================================================
	// Constructors
	//================================================================================
	public MavenHelper() {
		this.resolver = Maven.resolver();
	}

	//================================================================================
	// Static Methods
	//================================================================================
	public static String artifact(String group, String name, String version) {
		return group + ":" + name + ":" + version;
	}

	//================================================================================
	// Methods
	//================================================================================
	public File[] downloadFiles(String... artifacts) {
		return resolver.resolve(artifacts)
			.withTransitivity()
			.asFile();
	}

	public MavenResolvedArtifact[] downloadArtifacts(String... artifacts) {
		return resolver.resolve(artifacts)
			.withTransitivity()
			.asResolvedArtifact();
	}
}