package io.github.palexdev.architectfx.model;

import java.util.SequencedMap;

import org.tinylog.Logger;

public class Property {
    //================================================================================
    // Properties
    //================================================================================
    private final String name;
    private final String type;
    private final Object value;

    //================================================================================
    // Constructors
    //================================================================================
    public Property(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static String getPropertyType(String name, Object property) {
        Logger.trace("Determining property type for Name:{} Value:{}", name, property);
        if (property instanceof SequencedMap<?,?> m) {
            Logger.trace("Type is complex...");
            Object type = m.remove("type");
            if (type instanceof String s) {
                Logger.trace("Found type {} for property {}", s, name);
                return s;
            } else {
                Logger.error("Unable to determine type for Name:{} Value:{}", name, property);
                throw new IllegalArgumentException(
                    "Unexpected type %s for property %s".formatted(name,  property)
                );
            }
        }
        String className = property.getClass().getSimpleName();
        Logger.trace("Found type {} for property {}", className, name);
        return className;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        return "Property{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", value=" + value +
            '}';
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
