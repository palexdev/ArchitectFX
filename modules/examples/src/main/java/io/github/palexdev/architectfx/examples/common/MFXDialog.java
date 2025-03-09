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

package io.github.palexdev.architectfx.examples.common;

import io.github.palexdev.mfxcomponents.window.popups.MFXPopup;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.StageUtils;
import io.github.palexdev.mfxeffects.MFXScrimEffect;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class MFXDialog<R> extends MFXPopup {
    //================================================================================
    // Properties
    //================================================================================
    private boolean inNestedEventLoop = false;

    private MFXScrimEffect scrim;
    private boolean scrimOwner = false;
    private double scrimStrength = 0.25;
    private When<?> ownerPosWhen;
    private boolean draggable = false;

    //================================================================================
    // Constructors
    //================================================================================
    protected MFXDialog() {
        setAutoHide(false);
        setConsumeAutoHidingEvents(true);
        setHideOnEscape(false);

        /*
         * Workaround: popups can be focused only if the owner window is also focused
         */
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Window owner = getOwnerWindow();
            if (owner != null && !owner.isFocused())
                owner.requestFocus();
        });
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract R getResult();

    //================================================================================
    // Methods
    //================================================================================
    public R showAndWait(Window window, Pos anchor) {
        if (!Platform.canStartNestedEventLoop()) {
            throw new IllegalStateException("showAndWait is not allowed during animation or layout processing");
        }
        assert !inNestedEventLoop;

        show(window, anchor);
        inNestedEventLoop = true;
        Platform.enterNestedEventLoop(this);
        return getResult();
    }

    public Optional<R> showAndWaitOpt(Window window, Pos anchor) {
        return Optional.ofNullable(showAndWait(window, anchor));
    }

    protected void autoReposition() {
        if (getOwnerWindow() != null) {
            windowReposition();
        } else {
            reposition();
        }
    }

    protected void applyScrim() {
        if (scrim == null) scrim = new MFXScrimEffect() {
            @Override
            public void scrimWindow(Window window, double opacity) {
                Parent root = Optional.ofNullable(window)
                    .map(Window::getScene)
                    .map(Scene::getRoot)
                    .orElse(null);
                if (root == null) return;
                super.scrimWindow(window, opacity);
            }

            @Override
            public void removeEffect(Window window) {
                Parent root = Optional.ofNullable(window)
                    .map(Window::getScene)
                    .map(Scene::getRoot)
                    .orElse(null);
                if (root == null) return;
                super.removeEffect(window);
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

    protected void makeDraggable(Node byNode) {
        if (!draggable) return;
        Window w = getOwnerWindow();
        if (w instanceof Stage s)
            StageUtils.makeDraggable(s, byNode);
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
            .ifPresent(w -> unscrim());
        super.hide();
        inNestedEventLoop = false;
        Platform.exitNestedEventLoop(this, null);
    }

    public void dispose() {
        if (ownerPosWhen != null) {
            ownerPosWhen.dispose();
            ownerPosWhen = null;
        }
        setOwner(null);
        setContent(null);
        scrim = null;
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

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }
}
