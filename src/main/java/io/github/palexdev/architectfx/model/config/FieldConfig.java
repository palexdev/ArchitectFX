package io.github.palexdev.architectfx.model.config;

import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.Tuple3;
import io.github.palexdev.architectfx.yaml.YamlParser;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.Tags.FIELD_TAG;
import static io.github.palexdev.architectfx.yaml.Tags.VALUE_TAG;

// TODO allow instance fields eventually
public class FieldConfig extends Config {
    //================================================================================
    // Properties
    //================================================================================
    private final Object value;

    //================================================================================
    // Constructors
    //================================================================================
    public FieldConfig(Class<?> owner, String field, Object value) {
        super(owner, field);
        this.value = value;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    protected static Optional<FieldConfig> parse(YamlParser parser, SequencedMap<String, Object> map) {
        Object value = map.get(VALUE_TAG);
        if (value == null) {
            Logger.warn("Field config does not specify the {} tag\n{}", VALUE_TAG, map);
            return Optional.empty();
        }

        Object fObj = map.get(FIELD_TAG);
        Tuple3<Class<?>, String, Object> fInfo = parser.reflector().getFieldInfo(fObj, false);
        if (fInfo == null || fInfo.a() == null) {
            Logger.warn(
                "Invalid config, either because field info is null or because field is not static, skipping..."
            );
            return Optional.empty();
        }

        // TODO allow null values eventually
        value = parser.parseValue(value);
        if (value == null) {
            Logger.warn("Config value is null, skipping...");
            return Optional.empty();
        }
        return Optional.of(new FieldConfig(fInfo.a(), fInfo.b(), value));
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
