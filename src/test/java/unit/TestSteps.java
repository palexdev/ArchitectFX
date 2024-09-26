package unit;

import io.github.palexdev.architectfx.model.Step;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.STEPS_TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSteps {

    @Test
    void testSimple() {
        List<Step> steps = List.of(
            new Step("add", "A"),
            new Step("add", "B"),
            new Step("add", "C"),
            new Step("add", "D"),
            new Step("set", 2, "Z"),
            new Step("subList", 2, 4).setTransform(true)
        );

        Optional<List<String>> res = Optional.of(new ArrayList<>());
        for (Step step : steps) {
            assertTrue(res.isPresent());
            if (step.name().equals("subList")) {
                // Test up to this point
                assertEquals(4, res.get().size());
                assertEquals(List.of("A", "B", "Z", "D"), res.get());
            }
            res = step.run(res.get());
        }

        assertTrue(res.isPresent());
        assertEquals(2, res.get().size());
        assertEquals(List.of("Z", "D"), res.get());
    }

    @Test
    void testSimpleAsYaml() {
        String yaml = """
            .steps: [
              { name: "add", args: ["A"] },
              { name: "add", args: ["B"] },
              { name: "add", args: ["C"] },
              { name: "add", args: ["D"] },
              { name: "set", args: [2, "Z"] },
              { name: "subList", args: [2, 4], transform: true },
            ]
            """;
        SequencedMap<String, Object> map = new Yaml().load(yaml);
        assertTrue(map.containsKey(STEPS_TAG));
        assertEquals(ArrayList.class, map.get(STEPS_TAG).getClass());
        List<Step> steps = YamlDeserializer.instance().parseSteps(((List<?>) map.get(STEPS_TAG)));

        Optional<List<String>> res = Optional.of(new ArrayList<>());
        for (Step step : steps) {
            assertTrue(res.isPresent());
            if (step.name().equals("subList")) {
                // Test up to this point
                assertEquals(4, res.get().size());
                assertEquals(List.of("A", "B", "Z", "D"), res.get());
            }
            res = step.run(res.get());
        }

        assertTrue(res.isPresent());
        assertEquals(2, res.get().size());
        assertEquals(List.of("Z", "D"), res.get());
    }
}
