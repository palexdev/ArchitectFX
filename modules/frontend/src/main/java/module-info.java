import io.inverno.core.annotation.Module;

@Module(sourcePackage = "io.github.palexdev.architectfx.frontend")
module afx.frontend {
    //***** Dependencies *****//
    // Modules
    requires afx.backend;

    // JavaFX
    requires fr.brouillard.oss.cssfx;
    requires mfx.components;
    requires nfx.core;
    requires VirtualizedFX;

    // DI
    requires static io.inverno.core.annotation;
    requires io.inverno.core;

    // Yaml
    requires org.yaml.snakeyaml;

    // Logging
    requires org.tinylog.api;

    // Misc
    requires pragmatica.core;

    //***** Exports *****//
    // Base Package
    exports io.github.palexdev.architectfx.frontend;

    // DI Package
    exports io.github.palexdev.architectfx.frontend.di;

    // Events Package
    exports io.github.palexdev.architectfx.frontend.events;

    // Views Package
    exports io.github.palexdev.architectfx.frontend.views;
    exports io.github.palexdev.architectfx.frontend.views.base;
}