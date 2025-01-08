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

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.loaders.jui.JUIBaseLoader;
import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.virtualizedfx.cells.base.VFXCell;
import io.github.palexdev.virtualizedfx.grid.VFXGrid;
import io.github.palexdev.virtualizedfx.grid.VFXGridManager;
import io.github.palexdev.virtualizedfx.grid.VFXGridSkin;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;

public class DnDGrid<T, C extends VFXCell<T>> extends VFXGrid<T, C> {
    //================================================================================
    // Properties
    //================================================================================
    private Consumer<List<Project>> onProjectsDropped;

    //================================================================================
    // Constructors
    //================================================================================
    public DnDGrid() {}

    public DnDGrid(ObservableList<T> items, Function<T, C> cellFactory) {
        super(items, cellFactory);
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void onFilesDropped(List<File> files) {
        if (onProjectsDropped != null) {
            List<Project> list = files.parallelStream()
                .filter(f -> f.getName().endsWith("." + JUIBaseLoader.EXTENSION))
                .map(f -> new Project(f.toPath()))
                .toList();
            onProjectsDropped.accept(list);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new DnDGridSkin<>(this);
    }

    @Override
    public Supplier<VFXGridManager<T, C>> defaultBehaviorProvider() {
        return () -> new DnDGridBehavior<>(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Consumer<List<Project>> getOnProjectsDropped() {
        return onProjectsDropped;
    }

    public void setOnProjectsDropped(Consumer<List<Project>> onProjectsDropped) {
        this.onProjectsDropped = onProjectsDropped;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class DnDGridSkin<T, C extends VFXCell<T>> extends VFXGridSkin<T, C> {
        private final Label dndLabel;

        public DnDGridSkin(VFXGrid<T, C> grid) {
            super(grid);

            dndLabel = new Label(
                "Drag & Drop JUI Projects Here",
                new MFXFontIcon()
            );
            dndLabel.setContentDisplay(ContentDisplay.TOP);
            dndLabel.visibleProperty().bind(grid.emptyProperty());
            dndLabel.getStyleClass().add("dnd");
            dndLabel.setManaged(false);
            getChildren().add(dndLabel);

            grid.setClip(null);
        }

        @Override
        protected void initBehavior(VFXGridManager<T, C> behavior) {
            behavior.init();
            VFXGrid<T, C> grid = getSkinnable();
            events(
                intercept(grid, DragEvent.DRAG_OVER)
                    .process(((DnDGridBehavior<T, C>) behavior)::dragOver),
                intercept(grid, DragEvent.DRAG_DROPPED)
                    .process(((DnDGridBehavior<T, C>) behavior)::dragDropped),
                intercept(grid, DragEvent.DRAG_EXITED)
                    .process(((DnDGridBehavior<T, C>) behavior)::dragExited)
            );
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);

            double lw = LayoutUtils.snappedBoundWidth(dndLabel);
            double lh = LayoutUtils.snappedBoundHeight(dndLabel);
            double max = Math.max(lw, lh);
            dndLabel.resize(max, max);
            positionInArea(dndLabel, x, y, w, h, 0, HPos.CENTER, VPos.CENTER);
        }
    }

    public static class DnDGridBehavior<T, C extends VFXCell<T>> extends VFXGridManager<T, C> {
        public DnDGridBehavior(VFXGrid<T, C> grid) {
            super(grid);
        }

        public void dragOver(DragEvent de) {
            VFXGrid<T, C> node = getNode();
            Dragboard db = de.getDragboard();
            if (de.getGestureSource() != node &&
                db.hasFiles() &&
                checkFiles(db)
            ) {
                PseudoClasses.setOn(node, "dnd", true);
                de.acceptTransferModes(TransferMode.COPY);
            }
            de.consume();
        }

        public void dragDropped(DragEvent de) {
            DnDGrid<T, C> node = (DnDGrid<T, C>) getNode();
            Dragboard db = de.getDragboard();
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                node.onFilesDropped(files);
            }
            de.setDropCompleted(true);
            de.consume();
        }

        public void dragExited(DragEvent de) {
            PseudoClasses.setOn(getNode(), "dnd", false);
        }

        protected boolean checkFiles(Dragboard db) {
            return db.getFiles().parallelStream()
                .anyMatch(f -> f.getName().endsWith("." + JUIBaseLoader.EXTENSION));
        }
    }
}
