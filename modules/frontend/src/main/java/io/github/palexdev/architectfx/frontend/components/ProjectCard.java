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

import io.github.palexdev.architectfx.frontend.components.ProjectCardOverlay.OverlayEvents;
import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.architectfx.frontend.utils.DateTimeUtils;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.imcache.transforms.CenterCrop;
import io.github.palexdev.mfxcore.base.properties.styleable.StyleableDoubleProperty;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.controls.Text;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxcore.utils.fx.StyleUtils;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.cells.CellBaseBehavior;
import io.github.palexdev.virtualizedfx.cells.VFXCellBase;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class ProjectCard extends VFXCellBase<Project> {

    //================================================================================
    // Constructors
    //================================================================================
    public ProjectCard(Project item) {
        super(item);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public List<String> defaultStyleClasses() {
        return List.of("project-card");
    }

    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new ProjectCardSkin(this);
    }

    //================================================================================
    // StyleableProperties
    //================================================================================
    private final StyleableDoubleProperty previewRadius = new StyleableDoubleProperty(
        StyleableProperties.PREVIEW_RADIUS,
        this,
        "previewRadius",
        0.0
    );

    public double getPreviewRadius() {
        return previewRadius.get();
    }

    public StyleableDoubleProperty previewRadiusProperty() {
        return previewRadius;
    }

    public void setPreviewRadius(double previewRadius) {
        this.previewRadius.set(previewRadius);
    }

    //================================================================================
    // CssMetaData
    //================================================================================
    public static class StyleableProperties {
        private static final StyleablePropertyFactory<ProjectCard> FACTORY = new StyleablePropertyFactory<>(VFXCellBase.getClassCssMetaData());
        private static final List<CssMetaData<? extends Styleable, ?>> cssMetaDataList;

        private static final CssMetaData<ProjectCard, Number> PREVIEW_RADIUS =
            FACTORY.createSizeCssMetaData(
                "-fx-preview-radius",
                ProjectCard::previewRadiusProperty,
                0.0
            );

        static {
            cssMetaDataList = StyleUtils.cssMetaDataList(
                VFXCellBase.getClassCssMetaData(),
                PREVIEW_RADIUS
            );
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.cssMetaDataList;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected static class ProjectCardSkin extends SkinBase<VFXCellBase<Project>, CellBaseBehavior<Project>> {
        // One overlay shared across the cells
        private static final ProjectCardOverlay overlay = new ProjectCardOverlay();

        private final Text title;
        private final ImageView preview;
        private final StackPane previewContainer;
        private final Text lastModified;

        private final double V_GAP = 24.0;

        public ProjectCardSkin(ProjectCard cell) {
            super(cell);

            title = new Text();
            title.getStyleClass().add("title");
            title.textProperty().bind(cell.itemProperty().map(Project::getName));

            preview = new ImageView();
            preview.setManaged(false);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(preview.fitWidthProperty());
            clip.heightProperty().bind(preview.fitHeightProperty());
            clip.arcWidthProperty().bind(cell.previewRadiusProperty());
            clip.arcHeightProperty().bind(cell.previewRadiusProperty());
            preview.setClip(clip);

            previewContainer = new StackPane(preview);

            lastModified = new Text();
            lastModified.getStyleClass().add("modified");
            lastModified.textProperty().bind(cell.itemProperty().flatMap(Project::lastModifiedProperty)
                .map(ms -> DateTimeUtils.modifiedToHuman(ms.longValue()))
            );

            getChildren().addAll(title, previewContainer, lastModified);

            listeners(
                When.onInvalidated(cell.itemProperty().flatMap(Project::previewProperty))
                    .then(i -> {
                        double w = preview.getFitWidth();
                        double h = preview.getFitHeight();
                        if (w <= 0 || h <= 0) return;
                        Image out = UIUtils.transform(i, new CenterCrop(w, h));
                        preview.setImage(out);
                    })
                    .invalidating(preview.fitWidthProperty())
                    .invalidating(preview.fitHeightProperty()),
                When.onInvalidated(cell.hoverProperty())
                    .then(v -> {
                        if (!v) {
                            overlay.hide(cell, getChildren());
                            overlay.setProjectSupplier(null);
                        } else {
                            overlay.show(cell, getChildren());
                            overlay.setProjectSupplier(cell::getItem);
                        }
                    })
                    .executeNow(cell::isHover)
            );
        }

        @Override
        protected void initBehavior(CellBaseBehavior<Project> behavior) {
            behavior.init();
            VFXCellBase<Project> cell = getSkinnable();
            events(
                // Workaround for hover property not working under specific conditions for whatever reason
                // Thank you very much JavaFX
                WhenEvent.intercept(cell, MouseEvent.MOUSE_MOVED)
                    .process(e -> {
                        if (!overlay.isShowingFor(cell)) {
                            overlay.show(cell, getChildren());
                            overlay.setProjectSupplier(cell::getItem);
                            e.consume();
                        }
                    }),
                WhenEvent.intercept(cell, MouseEvent.MOUSE_CLICKED)
                    .condition(e ->
                        e.getButton() == MouseButton.PRIMARY &&
                        e.getClickCount() % 2 == 0
                    )
                    .process(e -> OverlayEvents.fire(OverlayEvents.LIVE_PREVIEW_EVENT, overlay))
            );
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            VFXCellBase<Project> cell = getSkinnable();
            if (overlay.getParent() == cell) {
                overlay.resizeRelocate(0, 0, cell.getWidth(), cell.getHeight());
            }

            Rect area = new Rect(x, y, w, h)
                .withVSpacing(V_GAP)
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            area.cutTop(LayoutUtils.snappedBoundHeight(title))
                .layout(title::resizeRelocate);

            area.cutBottom(LayoutUtils.snappedBoundHeight(lastModified))
                .layout(lastModified::resizeRelocate);

            preview.setFitWidth(w - snappedRightInset() - snappedLeftInset());
            preview.setFitHeight(area.height());
            area.resize((rw, rh) -> previewContainer.resize(preview.getFitWidth(), rh))
                .position(previewContainer::relocate);
        }
    }
}
