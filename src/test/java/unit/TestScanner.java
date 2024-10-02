package unit;

import io.github.classgraph.ClassInfoList;
import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner.ScanScope;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;

import static io.github.palexdev.architectfx.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScanner {

    @Test
    void testSearchSimpleName() {
        ClassInfoList l = ClassScanner.searchClasses("GridPane", ClassScanner.ScanScope.ALL);
        assertEquals(1, l.size());
    }

    @Test
    void testSearchFullName() {
        ClassInfoList l = ClassScanner.searchClasses("javafx.scene.layout.GridPane", ClassScanner.ScanScope.ALL);
        assertEquals(1, l.size());
    }

    @Test
    void testSearchDeps() {
        DependencyManager.instance().addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        ).refresh();
        ClassInfoList l = ClassScanner.searchClasses("io.github.palexdev.mfxcore.base.beans.Size", ScanScope.DEPS);
        assertEquals(1, l.size());
    }

    @Test
    void testMultipleResults() {
        DependencyManager.instance().addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        ).refresh();
        ClassInfoList l = ClassScanner.searchClasses("Label", ScanScope.DEPS)
            .filter(i -> i.extendsSuperclass(Node.class));
        assertEquals(2, l.size());
        assertEquals("io.github.palexdev.mfxcore.controls.Label", l.getFirst().getName());
        assertEquals("javafx.scene.control.Label", l.get(1).getName());
    }
}
