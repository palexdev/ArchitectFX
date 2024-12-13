package io.github.palexdev.architectfx.backend.enums;

/// This enumeration specifies reserved words in _JUI_.
public enum Keyword {
    /// The keyword `this` in Java.
    THIS,
    /// The keyword `null` in Java. We treat it like this because it is special
    NULL,
    /// Identifies an injection point in _JUI_. An injection point is a variable enclosed in dollar signs which will be
    /// replaced with an object during the load process.
    INJECTION,
    ;
}
