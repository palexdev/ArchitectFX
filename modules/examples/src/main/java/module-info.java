module afx.examples {
    //***** Dependencies *****//
    // Modules
    requires afx.backend;

    // JavaFX/UI
    requires mfx.components;
    requires VirtualizedFX;
    requires rectcut;

    // Misc
    requires org.tinylog.api;
    requires com.google.gson;

    //***** Exports *****//
    // Base Package
    exports io.github.palexdev.architectfx.examples;

    // Common Package
    exports io.github.palexdev.architectfx.examples.common;

    // Notes
    exports io.github.palexdev.architectfx.examples.notes;

    // Utils
    exports io.github.palexdev.architectfx.examples.utils;

    // Weather
    exports io.github.palexdev.architectfx.examples.weather;

    //***** Exports *****//
    opens io.github.palexdev.architectfx.examples;
    opens io.github.palexdev.architectfx.examples.common;
    opens io.github.palexdev.architectfx.examples.notes;
    opens io.github.palexdev.architectfx.examples.weather;
}