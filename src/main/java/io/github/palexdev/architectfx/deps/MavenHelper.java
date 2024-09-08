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