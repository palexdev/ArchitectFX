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

import java.util.List;

import io.github.palexdev.mfxcomponents.controls.base.MFXStyleable;
import io.github.palexdev.mfxcore.base.properties.styleable.StyleableDoubleProperty;
import io.github.palexdev.mfxcore.base.properties.styleable.StyleableObjectProperty;
import io.github.palexdev.mfxcore.utils.fx.StyleUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Box extends Control implements MFXStyleable {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<Node> children = FXCollections.observableArrayList();

    //================================================================================
    // Constructors
    //================================================================================
    public Box() {}

    public Box(Direction direction) {
        this(direction, new Node[0]);
    }

    public Box(Node... children) {
        this(Direction.COLUMN, children);
    }

    public Box(Direction direction, Node... children) {
        setDirection(direction);
        this.children.setAll(children);
        getStyleClass().setAll(defaultStyleClasses());
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static Region separator() {
        return separator("separator");
    }

    public static Region separator(String... styleClasses) {
        Region r = new Region();
        r.getStyleClass().add("separator");
        r.getStyleClass().addAll(styleClasses);
        HBoxSkin.setHgrow(r, Priority.ALWAYS);
        VBoxSkin.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    public static Region separator(double size) {
        Region r = new Region();
        r.getStyleClass().add("separator");
        r.setPrefSize(size, size);
        return r;
    }

    static void setConstraint(Node node, Object key, Object value) {
        if (value == null) {
            node.getProperties().remove(key);
        } else {
            node.getProperties().put(key, value);
        }
        if (node.getParent() != null) {
            node.getParent().requestLayout();
        }
    }

    static Object getConstraint(Node node, Object key) {
        if (node.hasProperties()) {
            return node.getProperties().get(key);
        }
        return null;
    }

    //================================================================================
    // Methods
    //================================================================================
    public void addSeparator() {
        getContainerChildren().add(separator());
    }

    public void addSeparator(int index) {
        getContainerChildren().add(index, separator());
    }

    public void setGrow(Node child, Priority value) {
        if (getDirection() == Direction.COLUMN) {
            VBoxSkin.setVgrow(child, value);
        } else {
            HBoxSkin.setHgrow(child, value);
        }
    }

    public Priority getGrow(Node child) {
        if (getDirection() == Direction.COLUMN) {
            return VBoxSkin.getVgrow(child);
        }
        return HBoxSkin.getHgrow(child);
    }

    public void setMargin(Node child, Insets value) {
        if (getDirection() == Direction.COLUMN) {
            VBoxSkin.setMargin(child, value);
        } else {
            HBoxSkin.setMargin(child, value);
        }
    }

    public Insets getMargin(Node child) {
        if (getDirection() == Direction.COLUMN) {
            return VBoxSkin.getMargin(child);
        }
        return HBoxSkin.getMargin(child);
    }

    public Box addStyleClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Skin<?> createDefaultSkin() {
        return getDirection() == Direction.ROW ? new HBoxSkin(Box.this) : new VBoxSkin(Box.this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("box");
    }

    //================================================================================
    // Styleable Properties
    //================================================================================
    private final StyleableObjectProperty<Direction> direction = new StyleableObjectProperty<>(
        StyleableProperties.DIRECTION,
        this,
        "direction",
        Direction.COLUMN
    ) {
        @Override
        protected void invalidated() {
            if (getSkin() != null) {
                Skin<?> newSkin = createDefaultSkin();
                setSkin(newSkin);
            }
        }
    };

    private final StyleableObjectProperty<Pos> alignment = new StyleableObjectProperty<>(
        StyleableProperties.ALIGNMENT,
        this,
        "alignment",
        Pos.CENTER
    );

    private final StyleableDoubleProperty spacing = new StyleableDoubleProperty(
        StyleableProperties.SPACING,
        this,
        "spacing",
        0.0
    );

    public Direction getDirection() {
        return direction.get();
    }

    public StyleableObjectProperty<Direction> directionProperty() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction.set(direction);
    }

    public Pos getAlignment() {
        return alignment.get();
    }

    public StyleableObjectProperty<Pos> alignmentProperty() {
        return alignment;
    }

    public void setAlignment(Pos alignment) {
        this.alignment.set(alignment);
    }

    public double getSpacing() {
        return spacing.get();
    }

    public StyleableDoubleProperty spacingProperty() {
        return spacing;
    }

    public void setSpacing(double spacing) {
        this.spacing.set(spacing);
    }

    //================================================================================
    // CssMetaData
    //================================================================================
    private static class StyleableProperties {
        private static final StyleablePropertyFactory<Box> FACTORY = new StyleablePropertyFactory<>(Control.getClassCssMetaData());
        private static final List<CssMetaData<? extends Styleable, ?>> cssMetaDataList;

        private static final CssMetaData<Box, Direction> DIRECTION =
            FACTORY.createEnumCssMetaData(
                Direction.class,
                "-fx-direction",
                Box::directionProperty,
                Direction.COLUMN
            );

        private static final CssMetaData<Box, Pos> ALIGNMENT =
            FACTORY.createEnumCssMetaData(
                Pos.class,
                "-fx-alignment",
                Box::alignmentProperty,
                Pos.CENTER
            );

        private static final CssMetaData<Box, Number> SPACING =
            FACTORY.createSizeCssMetaData(
                "-fx-spacing",
                Box::spacingProperty,
                0.0
            );

        static {
            cssMetaDataList = StyleUtils.cssMetaDataList(
                Control.getClassCssMetaData(),
                DIRECTION, ALIGNMENT, SPACING
            );
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.cssMetaDataList;
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ObservableList<Node> getContainerChildren() {
        return children;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum Direction {
        COLUMN,
        ROW
    }
}
