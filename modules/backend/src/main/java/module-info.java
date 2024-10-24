import io.inverno.core.annotation.Module;

@Module(sourcePackage = "io.github.palexdev.architectfx.backend")
module afx.backend {
    //***** Dependencies *****//
    // JavaFX
    requires javafx.graphics;

    // DI
    requires static io.inverno.core.annotation;

    // YAML
    requires org.yaml.snakeyaml;

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

    // Model Package
    exports io.github.palexdev.architectfx.backend.model;
    exports io.github.palexdev.architectfx.backend.model.config;

    // Utils Package
    exports io.github.palexdev.architectfx.backend.utils;
    exports io.github.palexdev.architectfx.backend.utils.reflection;

    // Yaml Package
    exports io.github.palexdev.architectfx.backend.yaml;
}