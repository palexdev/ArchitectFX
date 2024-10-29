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

package io.github.palexdev.architectfx.frontend.components.dialogs.base;

import java.util.Optional;

import io.github.palexdev.mfxcomponents.window.popups.MFXPopup;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxeffects.MFXScrimEffect;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class Dialog extends MFXPopup {
    //================================================================================
    // Properties
    //================================================================================
    private MFXScrimEffect scrim;
    private boolean scrimOwner = false;
    private double scrimStrength = 0.25;
    private When<?> ownerPosWhen;

    //================================================================================
    // Constructors
    //================================================================================
    protected Dialog() {
        setAutoHide(false);
        setConsumeAutoHidingEvents(true);
        setHideOnEscape(false);
        setContent(buildContent());
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract Node buildContent();

    //================================================================================
    // Methods
    //================================================================================
    protected void autoReposition() {
        if (getWindowOwner() != null) {
            windowReposition();
        } else {
            reposition();
        }
    }

    protected void applyScrim() {
        if (scrim == null) scrim = new MFXScrimEffect() {
            final EventHandler<Event> eh = Event::consume;

            @Override
            public void scrimWindow(Window window, double opacity) {
                super.scrimWindow(window, opacity);
                Parent root = window.getScene().getRoot();
                root.addEventFilter(Event.ANY, eh);
            }

            @Override
            public void removeEffect(Window window) {
                super.removeEffect(window);
                Parent root = window.getScene().getRoot();
                root.removeEventFilter(Event.ANY, eh);
            }
        };
        Window window = getOwnerWindow();
        if (window != null) {
            scrim.scrimWindow(window, scrimStrength);
        }
    }

    protected void unscrim() {
        Window window = getOwnerWindow();
        if (window != null) {
            scrim.removeEffect(window);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    protected void show() {
        Optional.ofNullable(getOwnerWindow())
            .filter(Stage.class::isInstance)
            .map(Stage.class::cast)
            .ifPresent(s -> {
                s.setAlwaysOnTop(true);
                ownerPosWhen = When.onInvalidated(s.xProperty())
                    .then(v -> autoReposition())
                    .invalidating(s.yProperty())
                    .invalidating(s.widthProperty())
                    .invalidating(s.heightProperty())
                    .listen();
                applyScrim();
            });
        super.show();
    }

    @Override
    public void hide() {
        if (getState().isClosing()) return; // TODO improve on the MaterialFX side
        Optional.ofNullable(getOwnerWindow())
            .filter(Stage.class::isInstance)
            .map(Stage.class::cast)
            .ifPresent(s -> {
                s.setAlwaysOnTop(false);
                unscrim();
            });
        if (ownerPosWhen != null){
            ownerPosWhen.dispose();
            ownerPosWhen = null;
        }
        super.hide();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public boolean isScrimOwner() {
        return scrimOwner;
    }

    public void setScrimOwner(boolean scrimOwner) {
        this.scrimOwner = scrimOwner;
    }

    public double getScrimStrength() {
        return scrimStrength;
    }

    public void setScrimStrength(double scrimStrength) {
        this.scrimStrength = scrimStrength;
    }
}
