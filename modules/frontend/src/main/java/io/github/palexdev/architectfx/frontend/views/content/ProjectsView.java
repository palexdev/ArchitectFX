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

package io.github.palexdev.architectfx.frontend.views.content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.loaders.jui.JUIBaseLoader;
import io.github.palexdev.architectfx.frontend.components.DnDGrid;
import io.github.palexdev.architectfx.frontend.components.ProjectCard;
import io.github.palexdev.architectfx.frontend.components.ProjectCardOverlay;
import io.github.palexdev.architectfx.frontend.components.TextField;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.components.layout.Box.Direction;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.model.PreviewModel;
import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.CollectionUtils;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.View;
import io.github.palexdev.architectfx.frontend.views.content.ProjectsView.ProjectsPane;
import io.github.palexdev.architectfx.frontend.views.content.ProjectsView.ProjectsViewBehavior;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXSegmentedButton;
import io.github.palexdev.mfxcomponents.controls.fab.MFXFab;
import io.github.palexdev.mfxcomponents.skins.MFXSegmentedButtonSkin.MFXSegment;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.enums.ScrollPaneEnums.ScrollBarPolicy;
import io.github.palexdev.virtualizedfx.grid.VFXGridHelper;
import io.inverno.core.annotation.Bean;
import javafx.animation.Animation;
import javafx.application.HostServices;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tinylog.Logger;

@Bean
public class ProjectsView extends View<ProjectsPane, ProjectsViewBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final AppModel appModel;
    private final PreviewModel previewModel;
    private final AppSettings settings;
    private final HostServices hostServices;

    //================================================================================
    // Constructors
    //================================================================================
    public ProjectsView(IEventBus events, Stage mainWindow, AppModel appModel, PreviewModel previewModel, AppSettings settings, HostServices hostServices) {
        super(events);
        this.mainWindow = mainWindow;
        this.appModel = appModel;
        this.previewModel = previewModel;
        this.settings = settings;
        this.hostServices = hostServices;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected ProjectsPane build() {
        return new ProjectsPane();
    }

    @Override
    protected Supplier<ProjectsViewBehavior> behaviorSupplier() {
        return ProjectsViewBehavior::new;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected class ProjectsPane extends Pane {
        private final Box header;
        private final DnDGrid<Project, ProjectCard> grid;
        private final VFXScrollPane vsp;
        private final MFXFab createFAB;
        private final MFXFab importFAB;

        private final int minColumns = 3;
        private final double V_GAP = 24.0;
        private final double FABS_GAP = 8.0;

        ProjectsPane() {
            MFXSegment nameSegment = new MFXSegment("Name", new MFXFontIcon("fas-font"));
            MFXSegment dateSegment = new MFXSegment("Date", new MFXFontIcon("fas-clock"));
            MFXSegmentedButton sortType = new MFXSegmentedButton(nameSegment, dateSegment);
            if (appModel.getProjectsSortBy() == Project.SortBy.NAME) {
                nameSegment.setSelected(true);
            } else {
                dateSegment.setSelected(true);
            }
            nameSegment.setOnAction(e -> behavior.sortBy(Project.SortBy.NAME));
            dateSegment.setOnAction(e -> behavior.sortBy(Project.SortBy.DATE));

            MFXIconButton sortMode = new MFXIconButton().outlined();
            sortMode.getStyleClass().add("sort-mode");
            When.onInvalidated(appModel.projectsSortModeProperty())
                .then(m -> {
                    PseudoClasses.setOn(sortMode, "ascending", m == Project.SortMode.ASCENDING);
                    PseudoClasses.setOn(sortMode, "descending", m == Project.SortMode.DESCENDING);
                })
                .executeNow()
                .listen();
            sortMode.setOnAction(e -> behavior.nextSortMode());

            TextField field = new TextField("", new MFXFontIcon(), null);
            field.setPromptText("Search");
            When.onInvalidated(field.textProperty())
                .then(behavior::filter)
                .listen();

            header = new Box(
                Direction.ROW,
                field,
                Box.separator(),
                sortType, sortMode
            ).addStyleClass("filter");
            getChildren().add(header);

            // Grid
            grid = new DnDGrid<>(
                appModel.getProjects().getView(),
                ProjectCard::new
            );
            grid.setOnProjectsDropped(behavior::addProjects);
            When.onInvalidated(grid.cellSizeProperty())
                .then(v -> grid.autoArrange(minColumns))
                .invalidating(grid.widthProperty())
                .invalidating(grid.hSpacingProperty())
                .executeNow()
                .listen();
            WhenEvent.intercept(grid, ProjectCardOverlay.OverlayEvents.ANY)
                .process(behavior::onOverlayAction)
                .register();

            // Scroll Pane;
            vsp = grid.makeScrollable();
            vsp.setHBarPolicy(ScrollBarPolicy.NEVER);
            getChildren().add(vsp);

            // Create FAB
            createFAB = new MFXFab("New Project", new MFXFontIcon()).extended();
            createFAB.setManaged(false);
            createFAB.getStyleClass().add("create");
            createFAB.setOnAction(e -> {
                // Debounce for a smoother UX
                UIUtils.delayAction(M3Motion.SHORT4, behavior::createProject);
            });
            getChildren().add(createFAB);

            // Import FAB
            importFAB = new MFXFab(new MFXFontIcon()).small();
            importFAB.setManaged(false);
            importFAB.getStyleClass().add("import");
            importFAB.setOnAction(e ->
                // Debounce for a smoother UX
                UIUtils.delayAction(M3Motion.SHORT4, behavior::addProjects)
            );
            UIUtils.installTooltip(importFAB, "Import Projects", Pos.CENTER_LEFT);
            getChildren().add(importFAB);

            getStyleClass().add("projects-view");
        }

        @Override
        protected double computeMinWidth(double height) {
            return prefWidth(height);
        }

        @Override
        protected double computePrefWidth(double height) {
            VFXGridHelper<Project, ProjectCard> gridHelper = grid.getHelper();
            double gridW = (minColumns * gridHelper.getTotalCellSize().getWidth()) - grid.getHSpacing();
            return snappedLeftInset() + snapSizeX(gridW + 12.0) + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            return prefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            double headerH = LayoutUtils.boundHeight(header);
            double rowH = grid.getHelper().getTotalCellSize().getHeight();
            double fabsH = LayoutUtils.boundHeight(importFAB) + LayoutUtils.boundHeight(createFAB) + FABS_GAP;
            return snappedTopInset() + snapSizeY(headerH + rowH + fabsH + V_GAP) + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            Rect area = Rect.of(0, 0, getWidth(), getHeight())
                .withVSpacing(V_GAP)
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            // Header
            area.cutTop(LayoutUtils.snappedBoundHeight(header))
                .layout(header::resizeRelocate);

            // Grid
            area.layout(vsp::resizeRelocate);

            // FABs
            double cfW = LayoutUtils.snappedBoundWidth(createFAB);
            double cfH = LayoutUtils.snappedBoundHeight(createFAB);
            Rect b1 = area.withVSpacing(FABS_GAP).cutBottom(cfH);
            b1.getRight(cfW)
                .resize((fw, fh) -> createFAB.autosize())
                .position(createFAB::relocate);

            double ifW = LayoutUtils.snappedBoundWidth(importFAB);
            double ifH = LayoutUtils.snappedBoundHeight(importFAB);
            Rect b2 = area.cutBottom(ifH);
            b2.getRight(ifW)
                .resize((fw, fh) -> importFAB.autosize())
                .position(importFAB::relocate);
        }
    }

    protected class ProjectsViewBehavior {
        private Animation filterDelay;

        public void onOverlayAction(ProjectCardOverlay.OverlayEvents event) {
            Project project = event.getProject();
            switch (event.getEventType().getName()) {
                case "LIVE_PREVIEW_EVENT" -> previewModel.setProject(project);
                case "FILE_EXPLORER_EVENT" -> hostServices.showDocument(project.getFile().getParent().toString());
                case "REMOVE_EVENT" -> appModel.getProjects().remove(project);
            }
        }

        public void filter(String text) {
            if (Animations.isPlaying(filterDelay)) filterDelay.stop();
            filterDelay = UIUtils.delayAction(
                M3Motion.SHORT4,
                () -> appModel.setFilter(text)
            );
        }

        public void sortBy(Project.SortBy sortBy) {
            appModel.setProjectsSortBy(sortBy);
        }

        public void nextSortMode() {
            Project.SortMode mode = appModel.getProjectsSortMode();
            appModel.setProjectsSortMode(
                (mode == Project.SortMode.ASCENDING) ?
                    Project.SortMode.DESCENDING :
                    Project.SortMode.ASCENDING
            );
        }

        public void createProject() {
            Path lastDir = getLastDir();
            FileChooser fc = new FileChooser();
            fc.setTitle("Create new JUI Project");
            fc.setInitialDirectory((lastDir != null) ? lastDir.toFile() : null);
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JUI Project", "*." + JUIBaseLoader.EXTENSION);
            fc.getExtensionFilters().add(filter);

            File file = fc.showSaveDialog(mainWindow);
            if (file == null) return;
            try {
                Files.writeString(
                    file.toPath(),
                    JUIBaseLoader.NEW_TEMPLATE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException ex) {
                Logger.error("Failed to write new template to file {} because:\n{}", file, ex);
            }
            CollectionUtils.addUnique(
                appModel.getProjects(),
                new Project(file.toPath())
            );
        }

        public void addProjects() {
            Path lastDir = getLastDir();
            FileChooser fc = new FileChooser();
            fc.setTitle("Import JUI Project");
            fc.setInitialDirectory((lastDir != null) ? lastDir.toFile() : null);
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JUI Project", "*." + JUIBaseLoader.EXTENSION);
            fc.getExtensionFilters().add(filter);

            List<File> files = fc.showOpenMultipleDialog(mainWindow);
            if (files == null) return;
            addProjects(files.stream().map(f -> new Project(f.toPath())).toList());
            settings.lastDir().set((lastDir == null) ? "" : lastDir.toString());
        }

        protected void addProjects(List<Project> projects) {
            for (Project project : projects) {
                CollectionUtils.addUnique(appModel.getProjects(), project);
            }
        }

        protected Path getLastDir() {
            String lastDir = settings.lastDir().get();
            Path toPath = Path.of(lastDir);
            return (!lastDir.isBlank() && Files.isDirectory(toPath)) ? toPath : null;
        }
    }
}
