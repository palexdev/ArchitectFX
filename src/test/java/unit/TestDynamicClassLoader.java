package unit;

import static io.github.palexdev.architectfx.deps.MavenHelper.artifact;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.joor.Reflect;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.deps.DynamicClassLoader;
import javafx.geometry.Insets;
import utils.TestUtils;

@TestMethodOrder(OrderAnnotation.class)
public class TestDynamicClassLoader {

	@Order(1)
	@Test
	void testSimple() {
		try {
			DynamicClassLoader dcl = new DynamicClassLoader();
			Insets obj = Reflect.onClass(Insets.class.getName(), dcl).create(20.0).get();
			assertEquals(20.0, obj.getTop());
			assertEquals(20.0, obj.getRight());
			assertEquals(20.0, obj.getBottom());
			assertEquals(20.0, obj.getLeft());
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex);
		}
	}

	@Order(2)
	@Test
	void testWithDeps() {
		DependencyManager dm = DependencyManager.instance();
		dm.addDeps(
				artifact("io.github.palexdev", "materialfx", "11.17.0"),
				artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
			)
			.refresh();
		Object obj = dm.create("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
		assertNotNull(obj);
		assertEquals("io.github.palexdev.mfxcore.base.beans.Size", obj.getClass().getName());

		double w = TestUtils.getProperty(obj, "width");
		assertEquals(69.0, w);
		double h = TestUtils.getProperty(obj, "height");
		assertEquals(420.0, h);
	}

	@Order(3)
	@Test
	void testCleanup() {
		DependencyManager dm = DependencyManager.instance();
		dm.cleanDeps().refresh();
		assertEquals(0, dm.getDependencies().size());

		Object obj = dm.create("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
		assertNull(obj);

		dm.addDeps(
				artifact("io.github.palexdev", "materialfx", "11.17.0"),
				artifact("io.github.palexdev", "virtualizedfx", "21.6.0")
		).refresh();
		obj = dm.create("io.github.palexdev.mfxcore.base.beans.Size", 69.0, 420.0);
		assertNotNull(obj);
		assertEquals("io.github.palexdev.mfxcore.base.beans.Size", obj.getClass().getName());

		double w = TestUtils.getProperty(obj, "width");
		assertEquals(69.0, w);
		double h = TestUtils.getProperty(obj, "height");
		assertEquals(420.0, h);
	}
}