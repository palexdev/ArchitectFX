package unit;

import io.github.classgraph.ClassInfoList;
import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner.ScanScope;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;

import static io.github.palexdev.architectfx.backend.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScanner {

    @Test
    void testSearchSimpleName() {
        DependencyManager dm = new DependencyManager();
        ClassInfoList l = new Scanner(dm).searchClasses("GridPane", Scanner.ScanScope.ALL);
        assertEquals(1, l.size());
    }

    @Test
    void testSearchFullName() {
        DependencyManager dm = new DependencyManager();
        ClassInfoList l = new Scanner(dm).searchClasses("javafx.scene.layout.GridPane", Scanner.ScanScope.ALL);
        assertEquals(1, l.size());
    }

    @Test
    void testSearchDeps() {
        DependencyManager dm = new DependencyManager().addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        );
        Scanner scanner = new Scanner(dm);
        ClassInfoList l = scanner.searchClasses("io.github.palexdev.mfxcore.base.beans.Size", ScanScope.DEPS);
        assertEquals(1, l.size());
    }

    @Test
    void testMultipleResults() {
        DependencyManager dm = new DependencyManager().addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        );
        Scanner scanner = new Scanner(dm);
        ClassInfoList l = scanner.searchClasses("Label", ScanScope.DEPS)
            .filter(i -> i.extendsSuperclass(Node.class));
        assertEquals(2, l.size());
        assertEquals("io.github.palexdev.mfxcore.controls.Label", l.getFirst().getName());
        assertEquals("javafx.scene.control.Label", l.get(1).getName());
    }
}
