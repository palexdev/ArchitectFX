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

package io.github.palexdev.architectfx.frontend.components.vfx;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import io.github.palexdev.architectfx.frontend.components.vfx.RecentsGrid.RecentsGridBehavior;
import io.github.palexdev.architectfx.frontend.components.vfx.cells.RecentCell;
import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.utils.FileUtils;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.mfxcomponents.controls.base.MFXControl;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.behavior.BehaviorBase;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.utils.GridUtils;
import io.github.palexdev.mfxcore.utils.StringUtils;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxresources.builders.IconBuilder;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.grid.VFXGrid;
import io.github.palexdev.virtualizedfx.grid.VFXGridHelper;
import io.github.palexdev.virtualizedfx.grid.VFXGridSkin;
import javafx.animation.Animation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;
import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

public class RecentsGrid extends MFXControl<RecentsGridBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final StringProperty filter = new SimpleStringProperty() {
        @Override
        protected void invalidated() {
            debouncedFilter.playFromStart();
        }
    };
    private final ObservableList<Recent> source;
    private final FilteredList<Recent> filtered;

    private final Animation debouncedFilter;

    private Path lastDir;

    //================================================================================
    // Constructors
    //================================================================================
    public RecentsGrid(ObservableList<Recent> source, String lastDir) {
        this.source = source;
        filtered = new FilteredList<>(source);

        debouncedFilter = PauseBuilder.build()
            .setDuration(M3Motion.SHORT4)
            .setOnFinished(e -> filter())
            .getAnimation();

        setDefaultBehaviorProvider();
        getStyleClass().setAll(defaultStyleClasses());

        Path toPath = Path.of(lastDir);
        if (!lastDir.isBlank() && Files.isDirectory(toPath)) {
            this.lastDir = toPath;
        }
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void filter() {
        String s = getFilter();
        if (s == null || s.isEmpty()) {
            filtered.setPredicate(null);
            return;
        }
        filtered.setPredicate(r -> StringUtils.containsIgnoreCase(r.file().toString(), s));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new RecentsGridSkin(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("recents-grid");
    }

    @Override
    public Supplier<RecentsGridBehavior> defaultBehaviorProvider() {
        return () -> new RecentsGridBehavior(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ObservableList<Recent> getSource() {
        return source;
    }

    public FilteredList<Recent> getFiltered() {
        return filtered;
    }

    public String getFilter() {
        return filter.get();
    }

    public StringProperty filterProperty() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter.set(filter);
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class RecentsGridBehavior extends BehaviorBase<RecentsGrid> {

        public RecentsGridBehavior(RecentsGrid grid) {
            super(grid);
        }

        public void dragOver(DragEvent de) {
            RecentsGrid grid = getNode();
            Dragboard db = de.getDragboard();
            if (db.hasFiles() &&
                db.getFiles().size() == 1 &&
                !Files.isDirectory(db.getFiles().getFirst().toPath()) &&
                "jdsl".equals(FileUtils.getExtension(db.getFiles().getFirst()))
            ) {
                PseudoClasses.setOn(grid, "dnd", true);
                de.acceptTransferModes(TransferMode.COPY);
            }
            de.consume();
        }

        public void dragDropped(DragEvent de) {
            RecentsGrid grid = getNode();
            Dragboard db = de.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();
                addRecent(file);
            }
            de.setDropCompleted(true);
            de.consume();
        }

        public void dragExited(DragEvent de) {
            PseudoClasses.setOn(getNode(), "dnd", false);
        }

        public void addProject(ActionEvent ae) {
            RecentsGrid grid = getNode();
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose a JDSL file");
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JDSL files", "*.jdsl");
            fc.getExtensionFilters().add(filter);
            if (grid.lastDir != null && Files.isDirectory(grid.lastDir)) {
                fc.setInitialDirectory(grid.lastDir.toFile());
            }

            Window parent = Optional.ofNullable(grid.getScene())
                .map(Scene::getWindow)
                .orElse(null);
            if (parent == null)
                Logger.error("Cannot browse for files because parent window could not be found");
            File file = fc.showOpenDialog(parent);
            if (file != null) {
                addRecent(file);
                grid.lastDir = file.getParentFile().toPath();
            }
        }

        public void clearFilter() {
            RecentsGrid grid = getNode();
            grid.setFilter(null);
        }

        protected void addRecent(File file) {
            RecentsGrid grid = getNode();
            ObservableList<Recent> source = grid.getSource();
            Path toPath = file.toPath();
            Set<Recent> tmp = new HashSet<>(source);
            if (tmp.add(new Recent(toPath))) {
                source.setAll(tmp);
                FXCollections.sort(source);
            }
        }
    }

    protected static class RecentsGridSkin extends MFXSkinBase<RecentsGrid, RecentsGridBehavior> {
        // Top
        private final Text header;
        private final MFXIconButton addBtn;
        private final HBox searchBar;
        private final TextField field;

        // Center
        private final VFXScrollPane vsp;
        private final VFXGrid<Recent, RecentCell> grid;

        public RecentsGridSkin(RecentsGrid component) {
            super(component);

            // Header
            header = new Text("Projects");
            header.getStyleClass().add("header");

            // Buttons
            addBtn = new MFXIconButton().tonal();
            addBtn.getStyleClass().add("add");
            UIUtils.installTooltip(addBtn, "Add Project");

            // Search bar
            field = new TextField();
            field.setPromptText("Search");
            field.textProperty().bindBidirectional(component.filterProperty());

            searchBar = new HBox(
                IconBuilder.build().addStyleClasses("leading").get(),
                field,
                IconBuilder.build().addStyleClasses("trailing")
                    .addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                        if (e.getButton() == MouseButton.PRIMARY)
                            Optional.ofNullable(getBehavior())
                                .ifPresent(RecentsGridBehavior::clearFilter);
                    })
                    .get()
            );
            searchBar.getStyleClass().add("search");

            // Grid
            grid = new VFXGrid<>(component.getFiltered(), RecentCell::new) {
                @Override
                protected SkinBase<?, ?> buildSkin() {
                    return new VFXGridSkin<>(this) {
                        {
                            // The clip covers the shadow, plus the VSP already clips the content
                            setClip(null);
                        }
                    };
                }

                @Override
                protected double computePrefWidth(double height) {
                    VFXGridHelper<Recent, RecentCell> helper = getHelper();
                    if (helper == null) return 0.0;
                    Size cs = helper.getTotalCellSize();
                    int columns = getColumnsNum();
                    return (columns * cs.getWidth()) - getHSpacing();
                }

                @Override
                protected double computePrefHeight(double width) {
                    VFXGridHelper<Recent, RecentCell> helper = getHelper();
                    if (helper == null) return 0.0;
                    Size cs = helper.getTotalCellSize();
                    int columns = getColumnsNum();
                    int rows = GridUtils.indToRow(grid.size(), columns);
                    return (rows * cs.getHeight()) - getVSpacing();
                }
            };
            vsp = grid.makeScrollable();

            // Finalize
            addListeners();
            getChildren().setAll(header, addBtn, searchBar, vsp);
        }

        private void addListeners() {
            listeners(
                onInvalidated(grid.widthProperty())
                    .then(w -> grid.autoArrange(3))
                    .invalidating(grid.cellSizeProperty())
                    .invalidating(grid.hSpacingProperty())
            );
        }

        @Override
        protected void initBehavior(RecentsGridBehavior behavior) {
            super.initBehavior(behavior);
            events(
                intercept(grid, DragEvent.DRAG_OVER)
                    .process(behavior::dragOver),
                intercept(grid, DragEvent.DRAG_DROPPED)
                    .process(behavior::dragDropped),
                intercept(grid, DragEvent.DRAG_EXITED)
                    .process(behavior::dragExited),
                intercept(addBtn, ActionEvent.ACTION)
                    .process(behavior::addProject)
            );
        }

        @Override
        public double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefWidth(-1);
        }

        @Override
        public double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            double gridW = LayoutUtils.snappedBoundWidth(vsp);
            double topW = LayoutUtils.snappedBoundWidth(header) + LayoutUtils.snappedBoundWidth(addBtn) + LayoutUtils.snappedBoundWidth(searchBar);
            return snappedLeftInset() +
                   Math.max(gridW, topW) +
                   8.0 +
                   snappedRightInset();
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            Rect area = Rect.of(x, y, w, h)
                .withVSpacing(12.0);

            /*
             * FIXME: it's not very easy to center content within an area with Rect
             *
             * TODO convert numbers to variables
             */

            /* Top */
            double hW = LayoutUtils.snappedBoundWidth(header);
            double hH = LayoutUtils.snappedBoundHeight(header);

            double bW = LayoutUtils.snappedBoundWidth(addBtn);
            double bH = LayoutUtils.snappedBoundHeight(addBtn);

            double sW = LayoutUtils.snappedBoundWidth(searchBar);
            double sH = LayoutUtils.snappedBoundHeight(searchBar);

            double topHeight = DoubleStream.of(hH, bH, sH).max().orElse(0.0);
            Rect top = area.cutTop(topHeight)
                .withInsets(0.0, 16.0, 0.0, 16.0);

            // Header
            top.cutLeft(LayoutUtils.snappedBoundWidth(header))
                .resize((w1, h1) -> header.autosize())
                .position((x1, y1) -> header.relocate(x1, (topHeight - hH) / 2.0));

            // S. Bar
            top.withHSpacing(searchBar.getSpacing()); // Spacing only for buttons
            top.cutRight(sW)
                .resize((w1, h1) -> searchBar.autosize())
                .position((x1, y1) -> searchBar.relocate(x1, (topHeight - sH) / 2.0));

            // Button
            top.cutRight(LayoutUtils.snappedBoundWidth(addBtn))
                .resize((bw, bh) -> addBtn.autosize())
                .position((x1, y1) -> addBtn.relocate(x1, (topHeight - bH) / 2.0));

            // Center
            area.layout(vsp::resizeRelocate);
        }
    }
}
