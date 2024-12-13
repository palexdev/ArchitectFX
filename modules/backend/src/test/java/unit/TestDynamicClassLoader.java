package unit;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.deps.DynamicClassLoader;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import javafx.geometry.Insets;
import org.joor.Reflect;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.github.palexdev.architectfx.backend.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TestDynamicClassLoader {

    @Order(1)
    @Test
    void testSimple() {
        DynamicClassLoader dcl = new DynamicClassLoader();
        Insets obj = Reflect.onClass(Insets.class.getName(), dcl).create(20.0).get();
        assertEquals(20.0, obj.getTop());
        assertEquals(20.0, obj.getRight());
        assertEquals(20.0, obj.getBottom());
        assertEquals(20.0, obj.getLeft());
    }

    @Order(2)
    @Test
    void testWithDeps() {
        DependencyManager dm = new DependencyManager().addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        );
        Scanner scanner = new Scanner(dm);
        Reflector reflector = new Reflector(scanner);
        Object obj = reflector.instantiate("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
        assertNotNull(obj);
        assertEquals("io.github.palexdev.mfxcore.base.beans.Size", obj.getClass().getName());

        double w = reflector.get(obj, "width");
        assertEquals(69.0, w);
        double h = reflector.get(obj, "height");
        assertEquals(420.0, h);
    }

    @Order(3)
    @Test
    void testCleanup() {
        DependencyManager dm = new DependencyManager();
        Scanner scanner = new Scanner(dm);
        Reflector reflector = new Reflector(scanner);
        dm.cleanDeps();
        assertEquals(0, dm.dependencies().size());

        Object obj = reflector.instantiate("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
        assertNull(obj);

        dm.addDeps(
            artifact("io.github.palexdev", "materialfx", "11.17.0"),
            artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
        );
        obj = reflector.instantiate("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
        assertNotNull(obj);
        assertEquals("io.github.palexdev.mfxcore.base.beans.Size", obj.getClass().getName());

        double w = reflector.get(obj, "width");
        assertEquals(69.0, w);
        double h = reflector.get(obj, "height");
        assertEquals(420.0, h);
    }
}