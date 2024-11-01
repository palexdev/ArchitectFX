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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import static io.github.palexdev.architectfx.frontend.components.layout.Box.getConstraint;
import static io.github.palexdev.architectfx.frontend.components.layout.Box.setConstraint;

public class HBoxSkin extends SkinBase<Box> {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final String MARGIN_CONSTRAINT = "hbox-margin";
    private static final String HGROW_CONSTRAINT = "hbox-hgrow";

    //================================================================================
    // Properties
    //================================================================================
    private final HBox root;

    //================================================================================
    // Constructors
    //================================================================================
    public HBoxSkin(Box box) {
        super(box);
        root = new HBox();
        root.getStyleClass().add("container");
        root.alignmentProperty().bindBidirectional(box.alignmentProperty());
        root.paddingProperty().bindBidirectional(box.paddingProperty());
        root.spacingProperty().bindBidirectional(box.spacingProperty());
        Bindings.bindContent(root.getChildren(), box.getContainerChildren());
        getChildren().add(root);
    }

    //================================================================================
    // Static Methods
    //================================================================================

    protected static void setHgrow(Node child, Priority value) {
        setConstraint(child, HGROW_CONSTRAINT, value);
    }

    protected static Priority getHgrow(Node child) {
        return (Priority) getConstraint(child, HGROW_CONSTRAINT);
    }

    protected static void setMargin(Node child, Insets value) {
        setConstraint(child, MARGIN_CONSTRAINT, value);
    }

    protected static Insets getMargin(Node child) {
        return (Insets) getConstraint(child, MARGIN_CONSTRAINT);
    }

    protected static void clearConstraints(Node child) {
        setHgrow(child, null);
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
