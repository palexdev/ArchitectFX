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

import java.util.List;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.components.ZoomControls.ZoomControlsBehavior;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.mfxcomponents.controls.base.MFXControl;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.behavior.BehaviorBase;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.utils.NumberUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ZoomControls extends MFXControl<ZoomControlsBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final DoubleProperty min = new SimpleDoubleProperty(0.25) {
        @Override
        protected void invalidated() {
            invalidateValue();
        }
    };
    private final DoubleProperty value = new SimpleDoubleProperty(1.0) {
        @Override
        public void set(double newValue) {
            super.set(NumberUtils.clamp(
                newValue,
                getMin(),
                getMax()
            ));
        }
    };
    private final DoubleProperty max = new SimpleDoubleProperty(2.0) {
        @Override
        protected void invalidated() {
            invalidateValue();
        }
    };
    private final DoubleProperty zoomIncrement = new SimpleDoubleProperty(0.25);

    //================================================================================
    //Constructors
    //================================================================================
    public ZoomControls() {
        setDefaultBehaviorProvider();
        getStyleClass().setAll(defaultStyleClasses());
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void invalidateValue() {
        setValue(getValue());
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new ZoomControlsSkin(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("zoom-controls");
    }

    @Override
    public Supplier<ZoomControlsBehavior> defaultBehaviorProvider() {
        return () -> new ZoomControlsBehavior(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public double getMin() {
        return min.get();
    }

    public DoubleProperty minProperty() {
        return min;
    }

    public void setMin(double min) {
        this.min.set(min);
    }

    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setValue(double value) {
        this.value.set(value);
    }

    public double getMax() {
        return max.get();
    }

    public DoubleProperty maxProperty() {
        return max;
    }

    public void setMax(double max) {
        this.max.set(max);
    }

    public double getZoomIncrement() {
        return zoomIncrement.get();
    }

    public DoubleProperty zoomIncrementProperty() {
        return zoomIncrement;
    }

    public void setZoomIncrement(double zoomIncrement) {
        this.zoomIncrement.set(zoomIncrement);
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class ZoomControlsSkin extends MFXSkinBase<ZoomControls, ZoomControlsBehavior> {
        private final MFXIconButton incBtn;
        private final MFXIconButton decBtn;
        private final Label zoomLabel;
        private final Box container;

        /*
         * TODO: currently, the skin uses a Box container for a easy and quick layout implementation
         *  Of course the downside of such choice is having an extra node in the scene-graph for no reason at all
         *  The idea solution I was thinking about, was to "extract" the layout algorithms from the JavaFX panes and make
         *  them applicable on any generic region
         */

        public ZoomControlsSkin(ZoomControls zoom) {
            super(zoom);

            zoomLabel = new Label();
            zoomLabel.textProperty().bind(zoom.valueProperty().map("%.2f"::formatted));

            incBtn = new MFXIconButton();
            incBtn.getStyleClass().add("increment");

            decBtn = new MFXIconButton();
            decBtn.getStyleClass().add("decrement");

            container = new Box(
                Box.Direction.COLUMN,
                incBtn, zoomLabel, decBtn
            );
            getChildren().setAll(container);
        }

        @Override
        protected void initBehavior(ZoomControlsBehavior behavior) {
            super.initBehavior(behavior);
            events(
                WhenEvent.intercept(incBtn, ActionEvent.ACTION)
                    .process(e -> behavior.increment()),
                WhenEvent.intercept(decBtn, ActionEvent.ACTION)
                    .process(e -> behavior.decrement()),
                WhenEvent.intercept(zoomLabel, MouseEvent.MOUSE_CLICKED)
                    .condition(e -> e.getButton() == MouseButton.SECONDARY)
                    .process(e -> behavior.reset())
            );
        }

        @Override
        public double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefWidth(height);
        }

        @Override
        public double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefHeight(width);
        }
    }

    public static class ZoomControlsBehavior extends BehaviorBase<ZoomControls> {

        public ZoomControlsBehavior(ZoomControls zoom) {
            super(zoom);
        }

        public void increment() {
            ZoomControls zoom = getNode();
            zoom.setValue(zoom.getValue() + zoom.getZoomIncrement());
        }

        public void decrement() {
            ZoomControls zoom = getNode();
            zoom.setValue(zoom.getValue() - zoom.getZoomIncrement());
        }

        public void reset() {
            ZoomControls zoom = getNode();
            zoom.setValue(1.0);
        }
    }
}
