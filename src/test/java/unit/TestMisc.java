package unit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import app.Launcher;
import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.yaml.YamlLoader;
import io.github.palexdev.architectfx.yaml.YamlParser;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import misc.TestController;
import misc.TestUtils;
import misc.User;
import misc.UserWrapper;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static io.github.palexdev.architectfx.utils.CastUtils.asYamlMap;
import static io.github.palexdev.architectfx.yaml.Tags.TYPE_TAG;
import static io.github.palexdev.architectfx.yaml.Tags.VALUE_TAG;
import static misc.TestUtils.getProperty;
import static misc.TestUtils.parser;
import static org.junit.jupiter.api.Assertions.*;

public class TestMisc {

    @Test
    void testSimpleCollection() {
        String document = """
            list:
              - 0
              - 1
              - 2
              - 3
              - "Integer.MAX_VALUE"
              - "This is a string"
              - "io.github.palexdev.architectfx.enums.Type.COLLECTION"
              - "Double.MAX_VALUE"
            """;
        Object yaml = asYamlMap(new Yaml().load(document)).get("list");
        List<Object> parsed = parser().parseList(yaml);
        assertEquals(8, parsed.size());

        int i = 0;
        for (Object o : parsed.subList(0, 4)) {
            assertInstanceOf(Integer.class, o);
            assertEquals(i, (int) o);
            i++;
        }

        Object o4 = parsed.get(4);
        assertInstanceOf(Integer.class, o4);
        assertEquals(Integer.MAX_VALUE, (int) o4);

        Object o5 = parsed.get(5);
        assertInstanceOf(String.class, o5);
        assertEquals("This is a string", o5);

        Object o6 = parsed.get(6);
        assertInstanceOf(Type.class, o6);
        assertEquals(Type.COLLECTION, o6);

        Object o7 = parsed.get(7);
        assertInstanceOf(Double.class, o7);
        assertEquals(Double.MAX_VALUE, (double) o7);
    }

    @Test
    void testComplexCollection() {
        String document = """
            users:
              - {.type: User, .args: ["User 0", "0000"]}
              - {.type: User, .args: ["User 1", "1111"]}
              - {.type: User, .args: ["User 2", "01234"]}
              - {.type: User, .args: ["User 3", "password"]}
            """;
        Object yaml = asYamlMap(new Yaml().load(document)).get("users");
        List<User> parsed = parser().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        User o0 = parsed.getFirst();
        assertEquals(o0.name(), "User 0");
        assertEquals(o0.password(), "0000");

        // Test 1
        User o1 = parsed.get(1);
        assertEquals(o1.name(), "User 1");
        assertEquals(o1.password(), "1111");

        // Test 2
        User o2 = parsed.get(2);
        assertEquals(o2.name(), "User 2");
        assertEquals(o2.password(), "01234");

        // Test 3
        User o3 = parsed.getLast();
        assertEquals(o3.name(), "User 3");
        assertEquals(o3.password(), "password");
    }

    @Test
    void testComplexCollectionArgs() {
        String document = """
            list:
              - {.type: UserWrapper, .args: [
                  {.type: User, .args: ["User 0", "0000"]}
                ]
              }
              - {.type: UserWrapper, .args: [
                  {.type: User, .args: ["User 1", "1111"]}
                ]
              }
              - {.type: UserWrapper, .args: [
                  {.type: User, .args: ["User 2", "01234"]}
                ]
              }
              - {.type: UserWrapper, .args: [
                  {.type: User, .args: ["User 3", "password"]}
                ]
              }
            """;

        Object yaml = asYamlMap(new Yaml().load(document)).get("list");
        List<UserWrapper> parsed = parser().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        UserWrapper o0 = parsed.getFirst();
        assertEquals(o0.name(), "User 0");
        assertEquals(o0.password(), "0000");

        // Test 1
        UserWrapper o1 = parsed.get(1);
        assertEquals(o1.name(), "User 1");
        assertEquals(o1.password(), "1111");

        // Test 2
        UserWrapper o2 = parsed.get(2);
        assertEquals(o2.name(), "User 2");
        assertEquals(o2.password(), "01234");

        // Test 3
        UserWrapper o3 = parsed.getLast();
        assertEquals(o3.name(), "User 3");
        assertEquals(o3.password(), "password");
    }

    @Test
    void testSimpleVarargs() {
        String document = """
            list:
              - {.type: User, .varargs: [1, 2, 3, 4]}
              - {.type: User, .args: ["User 1", "1111"], .varargs: [5, 6, 7, 8]}
              - {
                 .type: User,
                 .args: ["User 2", "01234"],
                 .config: [
                   {.method: setNumbers, .varargs: [9, 10]}
                 ]
                }
              - {
                 .type: User,
                 .args: ["User 3", "password"],
                 .config: [
                   {.method: setData, .args: ["Special User", "specialpass"], .varargs: [100, 200, 300, 400]}
                 ]
                }
            """;

        Object yaml = asYamlMap(new Yaml().load(document)).get("list");
        List<User> parsed = parser().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        User o0 = parsed.getFirst();
        assertEquals(o0.name(), "@placeholder");
        assertEquals(o0.password(), "@placeholder");
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, o0.numbers());

        // Test 1
        User o1 = parsed.get(1);
        assertEquals(o1.name(), "User 1");
        assertEquals(o1.password(), "1111");
        assertArrayEquals(new Integer[]{5, 6, 7, 8}, o1.numbers());

        // Test 2
        User o2 = parsed.get(2);
        assertEquals(o2.name(), "User 2");
        assertEquals(o2.password(), "01234");
        assertArrayEquals(new Integer[]{9, 10}, o2.numbers());

        // Test 3
        User o3 = parsed.getLast();
        assertEquals(o3.name(), "Special User");
        assertEquals(o3.password(), "specialpass");
        assertArrayEquals(new Integer[]{100, 200, 300, 400}, o3.numbers());
    }

    @Test
    void testStaticVariables() {
        String document = """
            list:
              - {.type: User, .args: ["User.PLACEHOLDER", "User.PLACEHOLDER"]}
              - {.type: User, .args: ["", ""],
                 name: "io.github.palexdev.architectfx.yaml.Tags.TYPE_TAG",
                 password: "io.github.palexdev.architectfx.yaml.Tags.VALUE_TAG"
                }
            """;

        Object yaml = asYamlMap(new Yaml().load(document)).get("list");
        List<User> parsed = parser().parseList(yaml);
        assertEquals(2, parsed.size());

        // Test 0
        User o0 = parsed.getFirst();
        assertEquals("@placeholder", o0.name());
        assertEquals("@placeholder", o0.password());

        // Test 1
        User o1 = parsed.get(1);
        assertEquals(TYPE_TAG, o1.name());
        assertEquals(VALUE_TAG, o1.password());
    }

    @Test
    void testFactory() throws IOException {
        String document = """
            .imports: [
              "io.github.palexdev.materialfx.builders.control.IconBuilder",
              "javafx.scene.paint.Color"
            ]
            
            .deps: [
              "io.github.palexdev:materialfx:11.17.0"
            ]
            
            MFXIconWrapper:
              .factory:
                - {.method: IconBuilder.icon, .transform: true}
                - {.method: setColor, .args: [Color.RED]}
                - {.method: setDescription, .args: ["fas-user"]}
                - {.method: setSize, .args: [64.0]}
                - {.method: wrapIcon, .args: [64.0, true, true], .transform: true}
            """;
        Parent icon = new YamlLoader().load(new ByteArrayInputStream(document.getBytes())).rootNode();
        assertNotNull(icon);
        assertEquals("MFXIconWrapper", icon.getClass().getSimpleName());

        assertEquals(64.0, getProperty(icon, "size"));

        Object fIcon = getProperty(icon, "icon");
        assertNotNull(fIcon);
        assertEquals("MFXFontIcon", fIcon.getClass().getSimpleName());

        assertEquals(64.0, getProperty(fIcon, "size"));
        assertEquals(Color.RED, getProperty(fIcon, "color"));
        assertEquals("fas-user", getProperty(fIcon, "description"));
    }

    @Test
    void testChainSupplier() throws ClassNotFoundException {
        YamlLoader loader = new YamlLoader()
            .setParallel(true)
            .setControllerFactory(c -> new TestController())
            .addToScanCache(LocalDateTime.class);
        YamlDeserializer deserializer = loader.getDeserializerFactory().get();
        assertNotNull(deserializer);
        assertTrue(deserializer.isParallel());
        assertNotNull(deserializer.getControllerFactory());
        assertNotNull(deserializer.getScanner().findClass(LocalDateTime.class.getName()));
    }

    @Test
    void testDeserializerConfig() throws IOException {
        TestUtils.forceInitFX();
        YamlLoader loader = new YamlLoader()
            .withDeserializer(() -> new YamlDeserializer(d -> {
                DependencyManager dm = new DependencyManager();
                ClassScanner scanner = new ClassScanner(dm);
                Reflector reflector = new Reflector(dm, scanner);
                YamlParser parser = new YamlParser(d, scanner, reflector);
                return new YamlDeserializer.YamlDeserializerConfig(dm, scanner, reflector, parser, true);
            }));
        Entity root = loader.load(Launcher.class.getClassLoader().getResource("assets/TextFields.jdsl")).root();
        assertNotNull(root);
    }
}
