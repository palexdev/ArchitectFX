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

package io.github.palexdev.architectfx.frontend.components.dialogs.base;

import java.util.Optional;

import io.github.palexdev.architectfx.frontend.components.Scrimmable;
import io.github.palexdev.mfxcomponents.window.popups.MFXPopup;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxeffects.MFXScrimEffect;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
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
    private boolean aotStatus = false;

    //================================================================================
    // Constructors
    //================================================================================
    protected Dialog() {
        setAutoHide(false);
        setConsumeAutoHidingEvents(true);
        setHideOnEscape(false);
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract Node buildContent();

    //================================================================================
    // Methods
    //================================================================================
    protected void autoReposition() {
        if (getOwnerWindow() != null) {
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
                Parent root = Optional.ofNullable(window)
                    .map(Window::getScene)
                    .map(Scene::getRoot)
                    .orElse(null);
                if (root == null) return;
                if (root instanceof Scrimmable s) {
                    s.apply(((Rectangle) getScrimNode()), opacity);
                } else {
                    super.scrimWindow(window, opacity);
                }
                root.addEventFilter(Event.ANY, eh);
            }

            @Override
            public void removeEffect(Window window) {
                Parent root = Optional.ofNullable(window)
                    .map(Window::getScene)
                    .map(Scene::getRoot)
                    .orElse(null);
                if (root == null) return;
                if (root instanceof Scrimmable s) {
                    s.removeScrim((Rectangle) getScrimNode());
                } else {
                    super.removeEffect(window);
                }
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
        if (scrim != null && window != null) {
            scrim.removeEffect(window);
        }
    }

    protected void dispose() {
        if (ownerPosWhen != null) {
            ownerPosWhen.dispose();
            ownerPosWhen = null;
        }
        setOwner(null);
        setContent(null);
        scrim = null;
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
                aotStatus = s.isAlwaysOnTop();
                //s.setAlwaysOnTop(true);
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
        Optional.ofNullable(getOwnerWindow())
            .filter(Stage.class::isInstance)
            .map(Stage.class::cast)
            .ifPresent(s -> {
                // Restore old aot status rather than setting it to false
                s.setAlwaysOnTop(aotStatus);
                unscrim();
            });
        super.hide();
        dispose();
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
