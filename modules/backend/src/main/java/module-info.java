import io.inverno.core.annotation.Module;

@Module(sourcePackage = "io.github.palexdev.architectfx.backend")
module afx.backend {
    //***** Dependencies *****//
    // JavaFX
    requires javafx.graphics;

    // DI
    requires static io.inverno.core.annotation;

    // ANTLR
    requires org.antlr.antlr4.runtime;

    // Reflection
    requires io.github.classgraph;
    requires org.jooq.joor;

    // Maven
    requires dev.mccue.jresolve;

    // Logging
    requires org.tinylog.api;

    //***** Exports *****//
    // Deps Package
    exports io.github.palexdev.architectfx.backend.deps;

    // Enums Package
    exports io.github.palexdev.architectfx.backend.enums;

    // JUI package
    exports io.github.palexdev.architectfx.backend.jui;

    // Loaders Package
    exports io.github.palexdev.architectfx.backend.loaders;
    exports io.github.palexdev.architectfx.backend.loaders.jui;

    // Model Package
    exports io.github.palexdev.architectfx.backend.model;
    exports io.github.palexdev.architectfx.backend.model.types;

    // Resolver Package
    exports io.github.palexdev.architectfx.backend.resolver;

    // Utils Package
    exports io.github.palexdev.architectfx.backend.utils;
    exports io.github.palexdev.architectfx.backend.utils.reflection;
}