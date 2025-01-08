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

import io.github.palexdev.mfxcore.builders.bindings.StringBindingBuilder;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.observables.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private final DoubleProperty scale = new SimpleDoubleProperty(1.0);

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

        When.onInvalidated(node.boundsInParentProperty())
            .then(b -> {
                /* Convert bounds and compute position*/
                Bounds nodeBounds = node.getLayoutBounds();
                Bounds toScene = node.localToScene(nodeBounds);
                Bounds toLocal = Optional.ofNullable(getParent())
                    .map(p -> p.sceneToLocal(toScene))
                    .orElse(ZERO);
                double x = toLocal.getMinX();
                double y = toLocal.getMinY();

                /* Compute size */
                double w = nodeBounds.getWidth() * getScale();
                double h = nodeBounds.getHeight() * getScale();

                /* Set properties */
                setPrefWidth(w);
                setPrefHeight(h);
                setTranslateX(x);
                setTranslateY(y);
            })
            .invalidating(parentProperty())
            .invalidating(scaleProperty())
            .executeNow()
            .listen();

        /* Bind label's text */
        boundsLabel.textProperty().bind(StringBindingBuilder.build()
            .setMapper(() -> {
                Bounds nodeBounds = node.getLayoutBounds();
                return "%.2f x %.2f".formatted(nodeBounds.getWidth(), nodeBounds.getHeight());
            })
            .addSources(node.layoutBoundsProperty())
            .get());

        setVisible(true);
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

    //================================================================================
    // Getters/Setters
    //================================================================================
    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale.set(scale);
    }
}
