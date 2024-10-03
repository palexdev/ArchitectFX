package unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.model.config.MethodConfig;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static io.github.palexdev.architectfx.yaml.Tags.CONFIG_TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.TestUtils.parser;

public class TestConfigs {

    @Test
    void testMethodConfigs() {
        List<Config> configs = List.of(
            new MethodConfig(null, "add", "A"),
            new MethodConfig(null, "add", "B"),
            new MethodConfig(null, "add", "C"),
            new MethodConfig(null, "add", "D"),
            new MethodConfig(null, "set", 2, "Z"),
            new MethodConfig(null, "subList", 2, 4).transform(true)
        );

        Optional<List<String>> res = Optional.of(new ArrayList<>());
        for (Config config : configs) {
            assertTrue(res.isPresent());
            if (config.member().equals("subList")) {
                // Test up to this point
                assertEquals(4, res.get().size());
                assertEquals(List.of("A", "B", "Z", "D"), res.get());
            }
            res = config.run(res.get());
        }

        assertTrue(res.isPresent());
        assertEquals(2, res.get().size());
        assertEquals(List.of("Z", "D"), res.get());
    }

    @Test
    void testMethodConfigsAsYaml() {
        String yaml = """
            .config: [
              { .method: "add", .args: ["A"] },
              { .method: "add", .args: ["B"] },
              { .method: "add", .args: ["C"] },
              { .method: "add", .args: ["D"] },
              { .method: "set", .args: [2, "Z"] },
              { .method: "subList", .args: [2, 4], .transform: true },
            ]
            """;
        SequencedMap<String, Object> map = new Yaml().load(yaml);
        assertTrue(map.containsKey(CONFIG_TAG));
        assertEquals(ArrayList.class, map.get(CONFIG_TAG).getClass());
        List<Config> configs = parser().parseConfigs(map.get(CONFIG_TAG));

        Optional<List<String>> res = Optional.of(new ArrayList<>());
        for (Config config : configs) {
            assertTrue(res.isPresent());
            if (config.member().equals("subList")) {
                // Test up to this point
                assertEquals(4, res.get().size());
                assertEquals(List.of("A", "B", "Z", "D"), res.get());
            }
            res = config.run(res.get());
        }

        assertTrue(res.isPresent());
        assertEquals(2, res.get().size());
        assertEquals(List.of("Z", "D"), res.get());
    }
}
