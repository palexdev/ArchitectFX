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

package io.github.palexdev.architectfx.frontend.components;


import java.util.Optional;

import io.github.palexdev.mfxcore.builders.bindings.DoubleBindingBuilder;
import io.github.palexdev.mfxcore.builders.bindings.StringBindingBuilder;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class BoundsOverlay extends Region {
    //================================================================================
    // Properties
    //================================================================================
    private static final BoundingBox ZERO = new BoundingBox(0, 0, 0, 0);

    private final ObjectProperty<Node> node = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            onNodeChanged();
        }
    };

    private final Label boundsLabel;

    //================================================================================
    // Constructors
    //================================================================================
    public BoundsOverlay() {
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setViewOrder(-1);
        setMouseTransparent(true);
        setFocusTraversable(false);
        setVisible(false); // Start invisible
        getStyleClass().add("bounds-overlay");

        boundsLabel = new Label();
        boundsLabel.textProperty().bind(StringBindingBuilder.build()
            .setMapper(() -> {
                Bounds bounds = getLayoutBounds();
                return "%.2f x %.2f".formatted(bounds.getWidth(), bounds.getHeight());
            })
            .addSources(layoutBoundsProperty())
            .get());
        getChildren().add(boundsLabel);
    }

    //================================================================================
    // Methods
    //================================================================================
    public void showFor(Node node) {
        if (node == null) {
            setVisible(false);
            prefWidthProperty().unbind();
            prefHeightProperty().unbind();
            translateXProperty().unbind();
            translateYProperty().unbind();
        }
        this.node.set(node);
    }

    protected void onNodeChanged() {
        Node node = this.node.get();
        if (node == null) return;

        prefWidthProperty().bind(node.layoutBoundsProperty().map(Bounds::getWidth));
        prefHeightProperty().bind(node.layoutBoundsProperty().map(Bounds::getHeight));
        translateXProperty().bind(nodeX(node));
        translateYProperty().bind(nodeY(node));
        setVisible(true);
    }

    protected ObservableValue<Number> nodeX(Node node) {
        return DoubleBindingBuilder.build()
            .setMapper(() -> {
                Bounds toScene = node.localToScene(node.getLayoutBounds());
                Bounds toLocal = Optional.ofNullable(getParent())
                    .map(p -> p.sceneToLocal(toScene))
                    .orElse(ZERO);
                return toLocal.getMinX();
            })
            .addSources(node.boundsInParentProperty(), parentProperty())
            .get();
    }

    protected ObservableValue<Number> nodeY(Node node) {
        return DoubleBindingBuilder.build()
            .setMapper(() -> {
                Bounds toScene = node.localToScene(node.getLayoutBounds());
                Bounds toLocal = Optional.ofNullable(getParent())
                    .map(p -> p.sceneToLocal(toScene))
                    .orElse(ZERO);
                return toLocal.getMinY();
            })
            .addSources(node.boundsInParentProperty(), parentProperty())
            .get();
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    protected void layoutChildren() {
        layoutInArea(
            boundsLabel,
            0, 0, getWidth(), getHeight(), 0,
            HPos.CENTER, VPos.CENTER
        );
    }
}
