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
import static org.junit.jupiter.api.Assertions.*;

public class TestMisc {

    @Test
    void testSimpleCollection() {
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
    void testComplexCollection() {
        String document = """
            users:
              - {.type: User, .args: ["User 0", "0000"]}
              - {.type: User, .args: ["User 1", "1111"]}
              - {.type: User, .args: ["User 2", "01234"]}
              - {.type: User, .args: ["User 3", "password"]}
            """;
        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("users");
        List<User> parsed = YamlDeserializer.instance().parseList(yaml);
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

        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("list");
        List<UserWrapper> parsed = YamlDeserializer.instance().parseList(yaml);
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
             .steps: [
               {.name: setNumbers, .varargs: [9, 10]}
             ]
            }
          - {
             .type: User,
             .args: ["User 3", "password"],
             .steps: [
               {.name: setData, .args: ["Special User", "specialpass"], .varargs: [100, 200, 300, 400]}
             ]
            }
        """;

        Object yaml = asYamlMap(new Yaml().load(new ByteArrayInputStream(document.getBytes()))).get("list");
        List<User> parsed = YamlDeserializer.instance().parseList(yaml);
        assertEquals(4, parsed.size());

        // Test 0
        User o0 = parsed.getFirst();
        assertEquals(o0.name(), "@placeholder");
        assertEquals(o0.password(), "@placeholder");
        assertArrayEquals(new Integer[]{1,2,3,4}, o0.numbers());

        // Test 1
        User o1 = parsed.get(1);
        assertEquals(o1.name(), "User 1");
        assertEquals(o1.password(), "1111");
        assertArrayEquals(new Integer[]{5,6,7,8}, o1.numbers());

        // Test 2
        User o2 = parsed.get(2);
        assertEquals(o2.name(), "User 2");
        assertEquals(o2.password(), "01234");
        assertArrayEquals(new Integer[]{9, 10}, o2.numbers());

        // Test 3
        User o3 = parsed.getLast();
        assertEquals(o3.name(), "Special User");
        assertEquals(o3.password(), "specialpass");
        assertArrayEquals(new Integer[]{100,200,300,400}, o3.numbers());
    }
}
