package unit;

import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIBaseLoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import misc.DummyLoader;
import misc.InjectTestClass;
import misc.NonStaticInjectTestClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InjectionTest {

    @Test
    void testInject() throws IOException {
        Map<String, Object> injections = Map.of(
            "aString", "STR",
            "anInt", 4,
            "aDouble", 10.5,
            "aNested", new InjectTestClass()
        );
        String doc = """
            InjectTestClass {
              aString: $aString$
              anInt: $anInt$
              aDouble: $aDouble$
              nested: $aNested$
            }
            """;
        InjectTestClass root = new DummyLoader<InjectTestClass>()
            .setConfig(() -> new UILoader.Config()
                .setResolverFactory(uri -> {
                    Resolver resolver = new DefaultResolver(uri);
                    resolver.context().setInjections(injections);
                    return resolver;
                })
            )
            .load(new ByteArrayInputStream(doc.getBytes()), null)
            .root();

        assertEquals("STR", root.aString);
        assertEquals(4, root.anInt);
        assertEquals(10.5, root.aDouble);

        assertNotNull(root.nested);
        assertSame(injections.get("aNested"), root.nested);
        assertNull(root.nested.aString);
        assertEquals(0, root.nested.anInt);
        assertNull(root.nested.aDouble);
        assertNull(root.nested.nested);
    }

    @Test
    void testController() throws IOException {
        String doc = """
            .controller: misc.InjectTestClass$Controller {}
            
            InjectTestClass {
              .cid: 'obj'
            }
            """;

        JUIBaseLoader.Loaded<InjectTestClass> loaded = new DummyLoader<InjectTestClass>()
            .load(new ByteArrayInputStream(doc.getBytes()), null);

        InjectTestClass.Controller controller = loaded.controller(InjectTestClass.Controller.class);
        assertNotNull(controller);

        InjectTestClass root = loaded.root();
        assertEquals("done", root.aString);
        assertEquals(1, root.anInt);
        assertEquals(Double.POSITIVE_INFINITY, root.aDouble);
        assertNotNull(root.nested);
    }

    @Test
    void testControllerFactory() throws IOException {
        String doc = """
            .controller: misc.InjectTestClass$Controller {}
            
            InjectTestClass {
              .cid: 'obj'
            }
            """;

        JUIBaseLoader.Loaded<InjectTestClass> loaded = new DummyLoader<InjectTestClass>()
            .setConfig(() -> new UILoader.Config().setControllerFactory(() -> new InjectTestClass.Controller() {
                @Override
                public void initialize() {
                    super.initialize();
                    obj.aDouble = Double.NEGATIVE_INFINITY;
                    obj.nested = null;
                }
            }))
            .load(new ByteArrayInputStream(doc.getBytes()), null);

        InjectTestClass.Controller controller = loaded.controller(InjectTestClass.Controller.class);
        assertNotNull(controller);

        InjectTestClass root = loaded.root();
        assertEquals("done", root.aString);
        assertEquals(1, root.anInt);
        assertEquals(Double.NEGATIVE_INFINITY, root.aDouble);
        assertNull(root.nested);
    }

    @Test
    void testNonStaticControllerFail() throws IOException {
        String doc = """
            .controller: misc.NonStaticInjectTestClass$Controller {}
            
            InjectTestClass {
              .cid: 'obj'
            }
            """;

        UILoader.Loaded<InjectTestClass> res = new DummyLoader<InjectTestClass>()
            .load(new ByteArrayInputStream(doc.getBytes()), null);
        assertNull(res.controller());
    }

    @Test
    void testNonStaticController() throws IOException {
        NonStaticInjectTestClass enclosingInstance = new NonStaticInjectTestClass();
        UILoader.Loaded<NonStaticInjectTestClass> res = enclosingInstance.load();
        assertNotNull(res.controller());
        assertEquals("Successfully Injected!", res.controller(NonStaticInjectTestClass.Controller.class).aString);
    }
}
