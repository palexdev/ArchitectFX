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

import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.controls.progress.MFXProgressIndicator;
import io.github.palexdev.mfxcomponents.controls.progress.ProgressDisplayMode;
import io.github.palexdev.mfxcomponents.skins.MFXIconButtonSkin;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.util.Duration;

public class CountdownIcon extends MFXIconButton {
    //================================================================================
    // Properties
    //================================================================================
    private final ReadOnlyBooleanWrapper playing = new ReadOnlyBooleanWrapper(false);
    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(Duration.ZERO);
    private Runnable countdownAction;

    //================================================================================
    // Methods
    //================================================================================
    public void play() {
        setPlaying(true);
    }

    public void stop() {
        setPlaying(false);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new CountdownIconSkin(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public boolean isPlaying() {
        return playing.get();
    }

    public ReadOnlyBooleanProperty playingProperty() {
        return playing.getReadOnlyProperty();
    }

    protected void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    public void setDuration(long millis) {
        this.duration.set(Duration.millis(millis));
    }

    public Runnable getCountdownAction() {
        return countdownAction;
    }

    public void setCountdownAction(Runnable countdownAction) {
        this.countdownAction = countdownAction;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class CountdownIconSkin extends MFXIconButtonSkin {
        private final MFXProgressIndicator indicator;
        private Animation countdownAnimation;

        public CountdownIconSkin(CountdownIcon button) {
            super(button);

            indicator = new MFXProgressIndicator(0.0);
            indicator.setDisplayMode(ProgressDisplayMode.CIRCULAR);
            indicator.setAnimated(false);
            indicator.setMouseTransparent(true);
            indicator.setVisible(false);
            getChildren().add(indicator);

            listeners(
                When.onInvalidated(button.playingProperty())
                    .then(this::countdown)
            );
        }

        protected void countdown(boolean play) {
            if (Animations.isPlaying(countdownAnimation))
                countdownAnimation.stop();
            if (!play) {
                indicator.setVisible(false);
                return;
            }

            CountdownIcon button = (CountdownIcon) getSkinnable();
            Duration duration = button.getDuration();
            if (Duration.ZERO.equals(duration)) {
                Optional.ofNullable(button.getCountdownAction()).ifPresent(Runnable::run);
                return;
            }

            countdownAnimation = TimelineBuilder.build()
                .add(KeyFrames.of(Duration.ZERO, indicator.progressProperty(), 1.0))
                .add(KeyFrames.of(duration, indicator.progressProperty(), 0.0))
                .setOnFinished(e -> {
                    indicator.setVisible(false);
                    Optional.ofNullable(button.getCountdownAction()).ifPresent(Runnable::run);
                })
                .getAnimation();
            indicator.setVisible(true);
            countdownAnimation.play();
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            indicator.resize(w + 4, h + 4);
            positionInArea(
                indicator,
                x, y, w, h, 0,
                HPos.CENTER, VPos.CENTER
            );
        }
    }
}
