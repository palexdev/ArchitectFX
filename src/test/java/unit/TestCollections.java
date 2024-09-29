package unit;

import java.io.ByteArrayInputStream;
import java.util.List;

import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import utils.User;
import utils.UserWrapper;

import static io.github.palexdev.architectfx.utils.CastUtils.asYamlMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TestCollections {

    @Test
    void testSimple() {
        String document = """
            list: [0, 1, 2, 3, "This is a string", "io.github.palexdev.architectfx.enums.Type.COLLECTION"]
            """;
        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("list");
        List<Object> parsed = YamlDeserializer.instance().parseList(yaml);
        assertEquals(6, parsed.size());

        int i = 0;
        for (Object o : parsed.subList(0, 4)) {
            assertInstanceOf(Integer.class, o);
            assertEquals(i, (int) o);
            i++;
        }

        Object o4 = parsed.get(4);
        assertInstanceOf(String.class, o4);
        assertEquals("This is a string", o4);

        Object o5 = parsed.get(5);
        assertInstanceOf(Type.class, o5);
        assertEquals(Type.COLLECTION, o5);
    }

    @Test
    void testComplex() {
        String document = """
            users:
              - {.type: User, .args: ["User 0", "0000"]}
              - {.type: User, .args: ["User 1", "1111"]}
              - {.type: User, .args: ["User 2", "01234"]}
              - {.type: User, .args: ["User 3", "password"]}
            """;
        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("users");
        List<Object> parsed = YamlDeserializer.instance().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        Object o0 = parsed.getFirst();
        assertInstanceOf(User.class, o0);
        assertEquals(((User) o0).name(), "User 0");
        assertEquals(((User) o0).password(), "0000");

        // Test 1
        Object o1 = parsed.get(1);
        assertInstanceOf(User.class, o1);
        assertEquals(((User) o1).name(), "User 1");
        assertEquals(((User) o1).password(), "1111");

        // Test 2
        Object o2 = parsed.get(2);
        assertInstanceOf(User.class, o2);
        assertEquals(((User) o2).name(), "User 2");
        assertEquals(((User) o2).password(), "01234");

        // Test 3
        Object o3 = parsed.getLast();
        assertInstanceOf(User.class, o3);
        assertEquals(((User) o3).name(), "User 3");
        assertEquals(((User) o3).password(), "password");
    }

    @Test
    void testComplexArgs() {
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

        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("list");
        List<Object> parsed = YamlDeserializer.instance().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        Object o0 = parsed.getFirst();
        assertInstanceOf(UserWrapper.class, o0);
        assertEquals(((UserWrapper) o0).name(), "User 0");
        assertEquals(((UserWrapper) o0).password(), "0000");

        // Test 1
        Object o1 = parsed.get(1);
        assertInstanceOf(UserWrapper.class, o1);
        assertEquals(((UserWrapper) o1).name(), "User 1");
        assertEquals(((UserWrapper) o1).password(), "1111");

        // Test 2
        Object o2 = parsed.get(2);
        assertInstanceOf(UserWrapper.class, o2);
        assertEquals(((UserWrapper) o2).name(), "User 2");
        assertEquals(((UserWrapper) o2).password(), "01234");

        // Test 3
        Object o3 = parsed.getLast();
        assertInstanceOf(UserWrapper.class, o3);
        assertEquals(((UserWrapper) o3).name(), "User 3");
        assertEquals(((UserWrapper) o3).password(), "password");
    }

}
