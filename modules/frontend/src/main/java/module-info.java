/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

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
    requires VirtualizedFX;
    requires rectcut;

    // DI
    requires static io.inverno.core.annotation;
    requires io.inverno.core;

    // Logging
    requires org.tinylog.api;
    requires org.tinylog.api.slf4j;
    requires org.apache.logging.log4j.to.slf4j;

    // Misc
    requires directory.watcher;
    requires ImCache;

    //***** Exports *****//
    // Base Package
    exports io.github.palexdev.architectfx.frontend;

    // Components Package
    exports io.github.palexdev.architectfx.frontend.components;
    exports io.github.palexdev.architectfx.frontend.components.dialogs;
    exports io.github.palexdev.architectfx.frontend.components.dialogs.base;
    exports io.github.palexdev.architectfx.frontend.components.layout;
    exports io.github.palexdev.architectfx.frontend.components.selection;

    // DI Package
    exports io.github.palexdev.architectfx.frontend.di;

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
    exports io.github.palexdev.architectfx.frontend.views.content;
}