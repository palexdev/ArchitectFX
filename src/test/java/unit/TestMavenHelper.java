package unit;

import static io.github.palexdev.architectfx.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.junit.jupiter.api.Test;

import io.github.palexdev.architectfx.deps.MavenHelper;

public class TestMavenHelper {

	@Test
	void testDownloadArtifacts() {
		MavenHelper dm = new MavenHelper();
		MavenResolvedArtifact[] artifacts = dm.downloadArtifacts(
			artifact("io.github.palexdev", "materialfx", "11.17.0"),
			artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
		);
		assertEquals(14, artifacts.length);

		// Expected artifacts
		// Note that JavaFX is ignored because its platform dependant therefore unconvenient to test
		Path mavenPath = Path.of(System.getProperty("user.home"), ".m2");
		Path[] paths = new Path[] {
			mavenPath.resolve("repository/io/github/palexdev/materialfx/11.17.0/materialfx-11.17.0.jar"),
    		mavenPath.resolve("repository/io/github/palexdev/mfxcore/11.8.0/mfxcore-11.8.0.jar"),
    		mavenPath.resolve("repository/io/github/palexdev/mfxlocalization/11.1.0/mfxlocalization-11.1.0.jar"),
    		mavenPath.resolve("repository/io/github/palexdev/mfxresources/11.9.1/mfxresources-11.9.1.jar"),
    		mavenPath.resolve("repository/io/github/palexdev/virtualizedfx/21.6.0/virtualizedfx-21.6.0.jar"),
    		mavenPath.resolve("repository/io/github/palexdev/mfxeffects/11.4.0/mfxeffects-11.4.0.jar"),
		};
		for (Path p : paths) {
			assertTrue(Files.exists(p));
		}
	}
}