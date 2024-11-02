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

package io.github.palexdev.architectfx.frontend.components;

import java.util.List;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.mfxcomponents.controls.base.MFXControl;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcore.behavior.BehaviorBase;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.StyleUtils;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.ConsumerTransition;
import io.github.palexdev.mfxeffects.enums.Interpolators;
import io.github.palexdev.mfxresources.fonts.MFXIconWrapper;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.CornerRadii;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class CountdownIcon extends MFXControl<CountdownIcon.CountdownIconBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<Duration> countdown = new SimpleObjectProperty<>(Duration.ZERO);
    private final ObjectProperty<CountdownStatus> status = new SimpleObjectProperty<>(CountdownStatus.STOPPED);
    private When<?> onStatus;

    //================================================================================
    // Constructors
    //================================================================================
    public CountdownIcon() {
        this(Duration.ZERO);
    }

    public CountdownIcon(double millis) {
        this(Duration.millis(millis));
    }

    public CountdownIcon(Duration countdown) {
        setCountdown(countdown);
        getStyleClass().setAll(defaultStyleClasses());
        setDefaultBehaviorProvider();

        UIUtils.debugTheme(this,  "css/components/CountdownIcon.css");
    }

    //================================================================================
    // Methods
    //================================================================================
    public void start() {
        setStatus(CountdownStatus.STARTED);
    }

    public void stop() {
        setStatus(CountdownStatus.STOPPED);
    }

    public void setOnStatus(CountdownStatus status, Runnable action) {
        if (onStatus != null)
            onStatus.dispose();
        onStatus = When.onInvalidated(statusProperty())
            .condition(s -> s == status)
            .then(s -> action.run())
            .listen();
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new CountdownIconSkin(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("countdown-icon");
    }

    @Override
    public Supplier<CountdownIconBehavior> defaultBehaviorProvider() {
        return () -> new CountdownIconBehavior(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Duration getCountdown() {
        return countdown.get();
    }

    public ObjectProperty<Duration> countdownProperty() {
        return countdown;
    }

    public void setCountdown(Duration countdown) {
        this.countdown.set(countdown);
    }

    public CountdownStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<CountdownStatus> statusProperty() {
        return status;
    }

    public void setStatus(CountdownStatus status) {
        this.status.set(status);
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    public static class CountdownIconBehavior extends BehaviorBase<CountdownIcon> {
        public CountdownIconBehavior(CountdownIcon icon) {
            super(icon);
        }
    }

    public static class CountdownIconSkin extends MFXSkinBase<CountdownIcon, CountdownIconBehavior> {
        private final Rectangle bg;
        private final MFXIconWrapper iconWrapper;

        private Animation bgAnimation;

        public CountdownIconSkin(CountdownIcon icon) {
            super(icon);

            bg = new Rectangle();
            bg.setManaged(false);
            bg.getStyleClass().add("bg");

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(icon.widthProperty());
            clip.heightProperty().bind(icon.heightProperty());
            bg.setClip(clip);

            listeners(
                When.onInvalidated(icon.countdownProperty())
                    .then(c -> updateAnimation())
                    .executeNow(),
                When.onInvalidated(icon.statusProperty())
                    .then(this::playStop),
                When.onInvalidated(icon.backgroundProperty())
                    .then(b -> {
                        CornerRadii radius = StyleUtils.parseCornerRadius(icon);
                        clip.setArcWidth(radius.getTopLeftHorizontalRadius() * 2);
                        clip.setArcHeight(radius.getTopLeftHorizontalRadius() * 2);
                    })
                    .executeNow()
            );

            iconWrapper = new MFXIconWrapper();

            getChildren().addAll(bg, iconWrapper);
        }

        protected void updateAnimation() {
            CountdownIcon icon = getSkinnable();
            if (Animations.isPlaying(bgAnimation))
                bgAnimation.stop();

            bgAnimation = ConsumerTransition.of(f -> {
                    double newW = icon.getWidth() - (icon.getWidth() * f);
                    bg.setWidth(newW);
                }, icon.getCountdown(), Interpolators.LINEAR)
                .setOnFinishedFluent(e -> icon.setStatus(CountdownStatus.FINISHED));
            playStop(icon.getStatus());
        }

        protected void playStop(CountdownStatus status) {
            if (Animations.isPlaying(bgAnimation) || status == CountdownStatus.STOPPED) {
                bgAnimation.stop();
                return;
            }
            bgAnimation.playFromStart();
        }

        @Override
        public double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefWidth(height);
        }

        @Override
        public double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefHeight(width);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            double hInsets = snappedLeftInset() + snappedRightInset();
            double vInsets = snappedTopInset() + snappedBottomInset();
            bg.setHeight(h + vInsets);
            bg.setWidth(w + hInsets);
            bg.relocate(0, 0);

            iconWrapper.resizeRelocate(0, 0, w + hInsets, h + vInsets);

        }
    }

    public enum CountdownStatus {
        STOPPED,
        STARTED,
        FINISHED
    }
}
