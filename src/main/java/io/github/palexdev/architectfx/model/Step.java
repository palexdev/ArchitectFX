package io.github.palexdev.architectfx.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

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
        T ret;
        try {
            Logger.debug("Running step {} with args {}", name, Arrays.toString(args));
            ret = Reflect.on(obj)
                .call(name, args)
                .get();
            return Optional.ofNullable(transform ? ret : obj)
                .map(o -> (T) o);
        } catch (ReflectException ex) {
            // FIXME Try varargs fallback
            // This is hacky, but I have 0 ideas on how to solve this
            if ((ret = varArgsFallback(obj)) != null)
                return Optional.of(ret);

            Logger.error(
                "Failed to execute step {} on object {}\n{}",
                name, obj.getClass().getName(), ex
            );
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    protected <T> T varArgsFallback(Object obj) {
        if (args.length == 0) return null;
        try {
            Object arrayArg = Array.newInstance(args[0].getClass(), args.length);
            System.arraycopy(args, 0, arrayArg, 0, args.length);
            T ret = Reflect.on(obj)
                .call(name, arrayArg)
                .get();
            return transform ? ret : (T) obj;
        } catch (Exception ex) {
            Logger.error("Fallback to varargs method failed...");
        }
        return null;
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