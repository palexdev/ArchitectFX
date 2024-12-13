package unit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIBaseLoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import misc.DummyLoader;
import misc.InjectTestClass;
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

        assertEquals("STR", root.getAString());
        assertEquals(4, root.getAnInt());
        assertEquals(10.5, root.getADouble());

        assertNotNull(root.getNested());
        assertSame(injections.get("aNested"), root.getNested());
        assertNull(root.getNested().getAString());
        assertEquals(0, root.getNested().getAnInt());
        assertNull(root.getNested().getADouble());
        assertNull(root.getNested().getNested());
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
        controller.init();

        InjectTestClass root = loaded.root();
        assertEquals("done", root.getAString());
        assertEquals(1, root.getAnInt());
        assertEquals(Double.POSITIVE_INFINITY, root.getADouble());
        assertNotNull(root.getNested());
    }
}
