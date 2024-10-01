package io.github.palexdev.architectfx.model.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.ReflectionUtils;
import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.VarArgsHandler;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.*;

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
    protected static Optional<MethodConfig> parse(SequencedMap<String, ?> map) {
        Tuple2<Class<?>, String> methodInfo = ReflectionUtils.getMethodInfo(map.get(METHOD_TAG));
        if (methodInfo == null) {
            Logger.warn("Skipping config...\n{}", map);
            return Optional.empty();
        }

        Object[] args = Optional.ofNullable(map.get(ARGS_TAG))
            .map(YamlDeserializer.instance()::parseList)
            .map(List::toArray)
            .orElseGet(() -> new Object[0]);
        Object varargs = Optional.ofNullable(map.get(VARARGS_TAG))
            .map(YamlDeserializer.instance()::parseList)
            .map(VarArgsHandler::generateArray)
            .orElse(null);
        args = VarArgsHandler.combine(args, varargs);


        boolean transform = Optional.ofNullable(map.get(TRANSFORM_TAG))
            .map(o -> Boolean.parseBoolean(o.toString()))
            .orElse(false);
        return Optional.of(new MethodConfig(
            methodInfo.a(),
            methodInfo.b(),
            args
        ).setTransform(transform));
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

    public boolean transform() {
        return transform;
    }

    public MethodConfig setTransform(boolean transform) {
        this.transform = transform;
        return this;
    }
}
