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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxeffects.animations.motion.Motion;
import io.github.palexdev.rectcut.Rect;
import javafx.animation.Animation;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

public class ProjectCardOverlay extends Region {
    //================================================================================
    // Properties
    //================================================================================
    private Parent owner;
    private Supplier<Project> projectSupplier;

    private final MFXIconButton deleteBtn;
    private final MFXIconButton previewBtn;
    private final MFXIconButton showBtn;

    // Animations
    private Animation showAnimation;
    private Animation hideAnimation;

    //================================================================================
    // Constructors
    //================================================================================
    public ProjectCardOverlay() {
        deleteBtn = new MFXIconButton().filled();
        deleteBtn.getStyleClass().add("delete");
        deleteBtn.setOnAction(e -> delete());
        UIUtils.installTooltip(deleteBtn, "Remove Project");

        previewBtn = new MFXIconButton().tonal();
        previewBtn.getStyleClass().add("preview");
        previewBtn.setOnAction(e -> preview());
        UIUtils.installTooltip(previewBtn, "Load Preview");

        showBtn = new MFXIconButton().tonal();
        showBtn.getStyleClass().add("show");
        showBtn.setOnAction(e -> showInFileManager());
        UIUtils.installTooltip(showBtn, "Show in File Manager");

        setManaged(false);
        getChildren().addAll(deleteBtn, previewBtn, showBtn);
        getStyleClass().add("project-card-overlay");
    }

    //================================================================================
    // Methods
    //================================================================================

    public void show(Parent owner, Collection<Node> children) {
        if (Animations.isPlaying(hideAnimation))
            hideAnimation.stop();

        setOpacity(0.0);
        children.add(this);
        this.owner = owner;
        owner.requestLayout();

        showAnimation = TimelineBuilder.build()
            .add(KeyFrames.of(M3Motion.SHORT4, opacityProperty(), 1.0, Motion.EASE_IN))
            .getAnimation();
        showAnimation.play();
    }

    public void hide(Parent owner, Collection<Node> children) {
        if (Animations.isPlaying(showAnimation))
            showAnimation.stop();

        hideAnimation = TimelineBuilder.build()
            .add(KeyFrames.of(M3Motion.SHORT4, opacityProperty(), 0.0, Motion.EASE_OUT))
            .getAnimation();
        Animations.onStopped(hideAnimation, () -> {
            children.remove(this);
            owner.requestLayout();
        }, true);
        hideAnimation.play();
        this.owner = null;
    }

    protected void delete() {
        OverlayEvents.fire(OverlayEvents.REMOVE_EVENT, this);
    }

    protected void preview() {
        OverlayEvents.fire(OverlayEvents.LIVE_PREVIEW_EVENT, this);
    }

    protected void showInFileManager() {
        OverlayEvents.fire(OverlayEvents.FILE_EXPLORER_EVENT, this);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected double computeMinWidth(double height) {
        double dW = LayoutUtils.boundWidth(deleteBtn);
        double pW = LayoutUtils.boundWidth(previewBtn);
        double sW = LayoutUtils.boundWidth(showBtn);
        return snappedLeftInset() + snapSizeX(Math.max(dW, pW + sW)) + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        double dH = LayoutUtils.boundHeight(deleteBtn);
        double pH = LayoutUtils.boundHeight(previewBtn);
        double sH = LayoutUtils.boundHeight(showBtn);
        return snappedTopInset() + snapSizeY(Math.max(pH, sH) + dH) + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
        double w = Math.max(computeMinWidth(-1), getWidth());
        double h = Math.max(computeMinHeight(-1), getHeight());
        Rect area = new Rect(0, 0, w, h)
            .withInsets(new double[]{
                snappedTopInset(),
                snappedRightInset(),
                snappedBottomInset(),
                snappedLeftInset(),
            });

        // TOP
        Rect top = area.getTop(LayoutUtils.snappedBoundHeight(deleteBtn));
        top.cutRight(LayoutUtils.snappedBoundWidth(deleteBtn))
            .layout(deleteBtn::resizeRelocate);

        Rect bottom = area.getBottom(Math.max(
            LayoutUtils.snappedBoundHeight(previewBtn),
            LayoutUtils.snappedBoundHeight(showBtn)
        )).withHSpacing(12.0);
        bottom.cutRight(LayoutUtils.snappedBoundWidth(showBtn))
            .layout(showBtn::resizeRelocate);
        bottom.cutRight(LayoutUtils.snappedBoundWidth(previewBtn))
            .layout(previewBtn::resizeRelocate);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    public boolean isShowingFor(Parent parent) {
        return this.owner == parent;
    }

    public Project getProject() {
        return Optional.ofNullable(projectSupplier)
            .map(Supplier::get)
            .orElse(null);
    }

    public Supplier<Project> getProjectSupplier() {
        return projectSupplier;
    }

    public void setProjectSupplier(Supplier<Project> projectSupplier) {
        this.projectSupplier = projectSupplier;
    }

    //================================================================================
    // Custom Events
    //================================================================================
    public static class OverlayEvents extends Event {

        public static final EventType<OverlayEvents> ANY = new EventType<>("CELL_ACTION");
        public static final EventType<OverlayEvents> LIVE_PREVIEW_EVENT = new EventType<>(ANY, "LIVE_PREVIEW_EVENT");
        public static final EventType<OverlayEvents> FILE_EXPLORER_EVENT = new EventType<>(ANY, "FILE_EXPLORER_EVENT");
        public static final EventType<OverlayEvents> REMOVE_EVENT = new EventType<>(ANY, "REMOVE_EVENT");

        protected static void fire(EventType<OverlayEvents> type, ProjectCardOverlay overlay) {
            Parent target = overlay.getParent();
            Project project = overlay.getProject();
            if (target == null || project == null) return;
            fireEvent(target, new OverlayEvents(type, project));
        }

        private final Project project;

        public OverlayEvents(EventType<OverlayEvents> eventType, Project project) {
            super(eventType);
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }
}
