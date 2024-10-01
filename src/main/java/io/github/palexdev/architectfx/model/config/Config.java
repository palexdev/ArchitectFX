package io.github.palexdev.architectfx.model.config;

import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.CastUtils;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.FIELD_TAG;
import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.METHOD_TAG;

public abstract class Config {
    //================================================================================
    // Properties
    //================================================================================
    protected final Class<?> owner;
    protected final String member;

    //================================================================================
    // Constructors
    //================================================================================
    protected Config(Class<?> owner, String member) {
        this.owner = owner;
        this.member = member;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static Optional<? extends Config> parse(Object obj) {
        if (!(obj instanceof SequencedMap<?, ?>)) return Optional.empty();
        SequencedMap<String, ?> map = CastUtils.asYamlMap(obj);
        if (map.containsKey(FIELD_TAG)) return FieldConfig.parse(map);
        if (map.containsKey(METHOD_TAG)) return MethodConfig.parse(map);
        Logger.error("Config does not specify {} or {} tags.\n{}", FIELD_TAG, METHOD_TAG, map);
        return Optional.empty();
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    public abstract <T> Optional<T> run(Object obj);

    //================================================================================
    // Getters
    //================================================================================
    public String name() {
        return owner != null ?
            owner.getName() + "." + member :
            member;
    }

    public Class<?> owner() {
        return owner;
    }

    public String member() {
        return member;
    }
}
