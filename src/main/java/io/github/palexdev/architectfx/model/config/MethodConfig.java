package io.github.palexdev.architectfx.model.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.reflection.ReflectionUtils;
import io.github.palexdev.architectfx.yaml.YamlParser;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.Tags.METHOD_TAG;
import static io.github.palexdev.architectfx.yaml.Tags.TRANSFORM_TAG;

public class MethodConfig extends Config {
    //================================================================================
    // Properties
    //================================================================================
    private final Object[] args;
    private boolean transform;

    //================================================================================
    // Constructors
    //================================================================================
    public MethodConfig(Class<?> owner, String member, Object... args) {
        super(owner, member);
        this.args = args;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    protected static Optional<MethodConfig> parse(YamlParser parser, SequencedMap<String, Object> map) {
        Tuple2<Class<?>, String> mInfo = ReflectionUtils.getMethodInfo(map.get(METHOD_TAG));
        if (mInfo == null) {
            Logger.warn("Method config does not specify the {} tag\n{}", METHOD_TAG, map);
            return Optional.empty();
        }

        Object[] args = parser.parseArgs(map);
        boolean transform = Optional.ofNullable(map.get(TRANSFORM_TAG))
            .map(o -> Boolean.parseBoolean(o.toString()))
            .orElse(false);
        return Optional.of(new MethodConfig(mInfo.a(), mInfo.b(), args).transform(transform));
    }

    //================================================================================
    // Methods
    //================================================================================
    @SuppressWarnings("unchecked")
    protected <T> Optional<T> runStandard(Object obj) throws ReflectException {
        Logger.debug("Running config {} on standard method {} with args {}", name(), member, Arrays.toString(args));
        T ret = Reflect.on(obj)
            .call(member, args)
            .get();
        return Optional.ofNullable(transform ? ret : (T) obj);
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> runStatic(Object obj) throws ReflectException {
        Logger.debug("Running config {} on static method {} with args {}", name(), member, Arrays.toString(args));
        T ret = Reflect.onClass(owner)
            .call(member, args)
            .get();
        return Optional.ofNullable(transform ? ret : (T) obj);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public <T> Optional<T> run(Object obj) {
        try {
            return owner != null ? runStatic(obj) : runStandard(obj);
        } catch (ReflectException ex) {
            Logger.error("Failed to execute config:\n{}", ex);
        }
        return Optional.empty();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Object[] args() {
        return args;
    }

    public boolean isTransform() {
        return transform;
    }

    public MethodConfig transform(boolean transform) {
        this.transform = transform;
        return this;
    }
}
