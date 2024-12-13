package io.github.palexdev.architectfx.backend.enums;

/// Enumeration which specifies how a collection should be handled during deserialization.
public enum CollectionHandleStrategy {
    /// This constant indicates the elements are going to be added to the target.
    ADD,
    /// This constant indicated the target collection will be cleared before adding the new elements.
    SET,
    ;

    /// Converts symbols such as `=` and `+=` to the appropriate set method.
    public static CollectionHandleStrategy fromString(String s) {
        return switch (s) {
            case "+=" -> ADD;
            case ":", "=" -> SET;
            default -> null;
        };
    }
}
