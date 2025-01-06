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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.enums.ScrollPaneEnums.ScrollBarPolicy;
import io.github.palexdev.virtualizedfx.grid.VFXGrid;
import io.github.palexdev.virtualizedfx.grid.VFXGridHelper;
import io.inverno.core.annotation.Bean;
import javafx.animation.PauseTransition;
import javafx.application.HostServices;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
        private final VFXGrid<Project, ProjectCard> grid;
        private final VFXScrollPane vsp;
        private final MFXFab importFAB;

        private final int minColumns = 3;
        private final double V_GAP = 24.0;

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
            grid = new VFXGrid<>(
                appModel.getProjects().getView(),
                ProjectCard::new
            );
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

            // Import FAB
            importFAB = new MFXFab("Import", new MFXFontIcon()).extended();
            importFAB.setManaged(false);
            importFAB.getStyleClass().add("import");
            importFAB.setOnAction(e ->
                // Debounce for a smoother UX
                PauseBuilder.build()
                    .setDuration(M3Motion.SHORT4)
                    .setOnFinished(end -> behavior.addProject())
                    .getAnimation()
                    .play()
            );
            getChildren().add(importFAB);

            getStyleClass().add("projects-view");

            // TODO debug
            UIUtils.debugTheme(this, "css/views/ProjectsView.css");
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
            double iH = LayoutUtils.boundHeight(importFAB);
            return snappedTopInset() + snapSizeY(headerH + rowH + iH + V_GAP) + snappedBottomInset();
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

            // FAB
            Rect bottom = area.getBottom(LayoutUtils.snappedBoundHeight(importFAB));
            bottom.cutRight(LayoutUtils.snappedBoundWidth(importFAB))
                .layout(importFAB::resizeRelocate);
        }
    }

    protected class ProjectsViewBehavior {
        private PauseTransition filterDelay;

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
            filterDelay = PauseBuilder.build()
                .setDuration(M3Motion.MEDIUM1)
                .setOnFinished(e -> appModel.setFilter(text))
                .getAnimation();
            filterDelay.play();
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

        public void addProject() {
            Path lastDir = getLastDir();
            FileChooser fc = new FileChooser();
            fc.setTitle("Import JUI Project");
            fc.setInitialDirectory((lastDir != null) ? lastDir.toFile() : null);
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JUI Project", "*.jui");
            fc.getExtensionFilters().add(filter);

            List<File> files = Optional.ofNullable(fc.showOpenMultipleDialog(mainWindow)).orElseGet(List::of);
            for (File file : files) {
                CollectionUtils.addUnique(
                    appModel.getProjects(),
                    new Project(file.toPath())
                );
                settings.lastDir().set((lastDir == null) ? "" : lastDir.toString());
            }
        }

        protected Path getLastDir() {
            String lastDir = settings.lastDir().get();
            Path toPath = Path.of(lastDir);
            return (!lastDir.isBlank() && Files.isDirectory(toPath)) ? toPath : null;
        }
    }
}
