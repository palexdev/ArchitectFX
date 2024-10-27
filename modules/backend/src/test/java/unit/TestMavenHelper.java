package unit;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.palexdev.architectfx.backend.deps.MavenHelper;
import org.junit.jupiter.api.Test;

import static io.github.palexdev.architectfx.backend.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMavenHelper {

    @Test
    void testDownloadArtifacts() {
        Path[] artifacts = MavenHelper.downloadFiles(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        );
        assertEquals(6, artifacts.length);

        // Expected artifacts
        Path[] paths = new Path[]{
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/mfxresources/11.10.0/mfxresources-11.10.0.jar"),
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/materialfx/11.17.0/materialfx-11.17.0.jar"),
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/mfxeffects/11.4.0/mfxeffects-11.4.0.jar"),
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/mfxcore/11.10.0/mfxcore-11.10.0.jar"),
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/mfxlocalization/11.1.0/mfxlocalization-11.1.0.jar"),
            MavenHelper.MAVEN_LOCAL.resolve("io/github/palexdev/virtualizedfx/21.6.0/virtualizedfx-21.6.0.jar"),
        };

        for (Path p : paths) {
            assertTrue(Files.exists(p));
        }
    }
}