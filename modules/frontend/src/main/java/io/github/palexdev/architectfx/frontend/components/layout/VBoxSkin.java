/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
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

package io.github.palexdev.architectfx.frontend.components.layout;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static io.github.palexdev.architectfx.frontend.components.layout.Box.getConstraint;
import static io.github.palexdev.architectfx.frontend.components.layout.Box.setConstraint;

public class VBoxSkin extends SkinBase<Box> {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final String MARGIN_CONSTRAINT = "vbox-margin";
    private static final String VGROW_CONSTRAINT = "vbox-vgrow";

    //================================================================================
    // Properties
    //================================================================================
    private final VBox root;

    //================================================================================
    // Constructors
    //================================================================================
    public VBoxSkin(Box box) {
        super(box);
        root = new VBox();
        root.getStyleClass().add("container");
        root.alignmentProperty().bindBidirectional(box.alignmentProperty());
        root.spacingProperty().bindBidirectional(box.spacingProperty());
        Bindings.bindContent(root.getChildren(), box.getContainerChildren());
        getChildren().add(root);
    }

    //================================================================================
    // Static Methods
    //================================================================================

    protected static void setVgrow(Node child, Priority value) {
        setConstraint(child, VGROW_CONSTRAINT, value);
    }

    protected static Priority getVgrow(Node child) {
        return (Priority) getConstraint(child, VGROW_CONSTRAINT);
    }

    protected static void setMargin(Node child, Insets value) {
        setConstraint(child, MARGIN_CONSTRAINT, value);
    }

    protected static Insets getMargin(Node child) {
        return (Insets) getConstraint(child, MARGIN_CONSTRAINT);
    }

    protected static void clearConstraints(Node child) {
        setVgrow(child, null);
        setMargin(child, null);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public void dispose() {
        Box box = getSkinnable();
        Bindings.unbindContent(root.getChildren(), box.getContainerChildren());
        super.dispose();
    }
}
