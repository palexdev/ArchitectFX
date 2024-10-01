package io.github.palexdev.architectfx.model.config;

import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.ReflectionUtils;
import io.github.palexdev.architectfx.utils.Tuple3;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.FIELD_TAG;
import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.VALUE_TAG;

public class FieldConfig extends Config {
    //================================================================================
    // Properties
    //================================================================================
    private final Object value;

    //================================================================================
    // Constructors
    //================================================================================
    public FieldConfig(Class<?> klass, String field, Object value) {
        super(klass, field);
        this.value = value;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    protected static Optional<FieldConfig> parse(SequencedMap<String, ?> map) {
        if (!map.containsKey(VALUE_TAG)) {
            Logger.error("Field config does not specify {} tag.\n{}", VALUE_TAG, map);
            return Optional.empty();
        }

        Tuple3<Class<?>, String, Object> fieldInfo = ReflectionUtils.getFieldInfo(map.get(FIELD_TAG));
        if (fieldInfo == null || fieldInfo.a() == null) {
            Logger.warn("Skipping config...");
            return Optional.empty();
        }

        Object value = map.get(VALUE_TAG);
        if (value == null) {
            Logger.error("Field config does not specify {} tag.\n{}", VALUE_TAG, map);
            return Optional.empty();
        }

        value = YamlDeserializer.instance().parseValue(value);
        return (value == null) ?
            Optional.empty() :
            Optional.of(new FieldConfig(null, null, null));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> run(Object obj) {
        try {
            Logger.debug("Running config {}. Setting static field to {}", name(), Objects.toString(value));
            Reflect.onClass(owner)
                .set(member, value);
            return Optional.of((T) obj);
        } catch (ReflectException ex) {
            Logger.error("Failed to execute config:\n{}", ex);
        }
        return Optional.empty();
    }

    //================================================================================
    // Getters
    //================================================================================
    public Object value() {
        return value;
    }
}
