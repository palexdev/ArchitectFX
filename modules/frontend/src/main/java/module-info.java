import io.inverno.core.annotation.Module;

@Module(sourcePackage = "io.github.palexdev.architectfx.frontend")
module afx.frontend {
    //***** Dependencies *****//
    // Modules
    requires afx.backend;

    // JavaFX/UI
    requires javafx.graphics;
    requires javafx.swing;
    requires fr.brouillard.oss.cssfx;
    requires mfx.components;
    requires nfx.core;
    requires VirtualizedFX;
    requires rectcut;

    // DI
    requires static io.inverno.core.annotation;
    requires io.inverno.core;

    // Logging
    requires org.tinylog.api;
    requires org.tinylog.api.slf4j;

    // Misc
    requires directory.watcher;
    requires pragmatica.core;
    requires ImCache;

    //***** Exports *****//
    // Base Package
    exports io.github.palexdev.architectfx.frontend;

    // Components Package
    exports io.github.palexdev.architectfx.frontend.components;
    exports io.github.palexdev.architectfx.frontend.components.base;
    exports io.github.palexdev.architectfx.frontend.components.dialogs;
    exports io.github.palexdev.architectfx.frontend.components.dialogs.base;
    exports io.github.palexdev.architectfx.frontend.components.layout;
    exports io.github.palexdev.architectfx.frontend.components.selection;
    exports io.github.palexdev.architectfx.frontend.components.vfx;
    exports io.github.palexdev.architectfx.frontend.components.vfx.cells;
    exports io.github.palexdev.architectfx.frontend.components.vfx.columns;
    exports io.github.palexdev.architectfx.frontend.components.vfx.rows;

    // DI Package
    exports io.github.palexdev.architectfx.frontend.di;

    // Enums package
    exports io.github.palexdev.architectfx.frontend.enums;

    // Events Package
    exports io.github.palexdev.architectfx.frontend.events;

    // Model Package
    exports io.github.palexdev.architectfx.frontend.model;

    // Settings Package
    exports io.github.palexdev.architectfx.frontend.settings;

    // Theming Package
    exports io.github.palexdev.architectfx.frontend.theming;

    // Utils
    exports io.github.palexdev.architectfx.frontend.utils;
    exports io.github.palexdev.architectfx.frontend.utils.ui;

    // Views Package
    exports io.github.palexdev.architectfx.frontend.views;
}