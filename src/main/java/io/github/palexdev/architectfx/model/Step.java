package io.github.palexdev.architectfx.model;

import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class Step {
    //================================================================================
    // Properties
    //================================================================================
    private final String name;
    private Object[] args;
    private boolean transform = false;

    //================================================================================
    // Constructors
    //================================================================================
    public Step(String name) {
        this(name, new Object[0]);
    }

    public Step(String name, Object... args) {
        this.name = name;
        this.args = args;
    }

    //================================================================================
    // Methods
    //================================================================================

    @SuppressWarnings("unchecked")
    public <T> Optional<T> run(Object obj) {
        try {
            Logger.debug("Running step {} with args {}", name, Arrays.toString(args));
            Object ret = Reflect.on(obj)
                .call(name, args)
                .get();
            return Optional.ofNullable(transform ? ret : obj)
                .map(o -> (T) o);
        } catch (ReflectException ex) {
            Logger.error(
                "Failed to execute step {} on object",
                name, obj.getClass().getName()
            );
            Logger.error(ex);
        }
        return Optional.empty();
    }


    //================================================================================
    // Getters/Setters
    //================================================================================

    public String name() {
        return name;
    }

    public Object[] args() {
        return args;
    }

    public Step setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Step setArgs(Collection<Object> args) {
        this.args = args.toArray();
        return this;
    }

    public boolean isTransform() {
        return transform;
    }

    public Step setTransform(boolean transform) {
        this.transform = transform;
        return this;
    }
}