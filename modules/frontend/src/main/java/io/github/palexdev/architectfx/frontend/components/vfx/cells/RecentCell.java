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

package io.github.palexdev.architectfx.frontend.components.vfx.cells;


import java.util.List;

import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.utils.DateTimeUtils;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.base.properties.styleable.StyleableDoubleProperty;
import io.github.palexdev.mfxcore.builders.bindings.DoubleBindingBuilder;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.controls.Text;
import io.github.palexdev.mfxcore.observables.OnInvalidated;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxcore.utils.fx.StyleUtils;
import io.github.palexdev.mfxcore.utils.fx.TextMeasurementCache;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.cells.CellBaseBehavior;
import io.github.palexdev.virtualizedfx.cells.VFXCellBase;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class RecentCell extends VFXCellBase<Recent> {

    //================================================================================
    // Constructors
    //================================================================================
    public RecentCell(Recent item) {
        super(item);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new RecentCellSkin(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("cell", "card");
    }

    //================================================================================
    // Styleable Properties
    //================================================================================
    private final StyleableDoubleProperty clipRadius = new StyleableDoubleProperty(
        StyleableProperties.CLIP_RADIUS,
        this,
        "clipRadius",
        0.0
    );

    private final StyleableDoubleProperty hGap = new StyleableDoubleProperty(
        StyleableProperties.H_GAP,
        this,
        "hGap",
        0.0
    );

    private final StyleableDoubleProperty vGap = new StyleableDoubleProperty(
        StyleableProperties.V_GAP,
        this,
        "vGap",
        0.0
    );

    public double getClipRadius() {
        return clipRadius.get();
    }

    public StyleableDoubleProperty clipRadiusProperty() {
        return clipRadius;
    }

    public void setClipRadius(double clipRadius) {
        this.clipRadius.set(clipRadius);
    }

    public double getHGap() {
        return hGap.get();
    }

    public StyleableDoubleProperty hGapProperty() {
        return hGap;
    }

    public void setHGap(double hGap) {
        this.hGap.set(hGap);
    }

    public double getVGap() {
        return vGap.get();
    }

    public StyleableDoubleProperty vGapProperty() {
        return vGap;
    }

    public void setVGap(double vGap) {
        this.vGap.set(vGap);
    }

    //================================================================================
    // CssMetaData
    //================================================================================
    private static class StyleableProperties {
        private static final StyleablePropertyFactory<RecentCell> FACTORY = new StyleablePropertyFactory<>(VFXCellBase.getClassCssMetaData());
        private static final List<CssMetaData<? extends Styleable, ?>> cssMetaDataList;

        private static final CssMetaData<RecentCell, Number> CLIP_RADIUS =
            FACTORY.createSizeCssMetaData(
                "-fx-clip-radius",
                RecentCell::clipRadiusProperty,
                0.0
            );

        private static final CssMetaData<RecentCell, Number> H_GAP =
            FACTORY.createSizeCssMetaData(
                "-fx-hgap",
                RecentCell::hGapProperty,
                0.0
            );

        private static final CssMetaData<RecentCell, Number> V_GAP =
            FACTORY.createSizeCssMetaData(
                "-fx-vgap",
                RecentCell::vGapProperty,
                0.0
            );

        static {
            cssMetaDataList = StyleUtils.cssMetaDataList(
                VFXCellBase.getClassCssMetaData(),
                CLIP_RADIUS, H_GAP, V_GAP
            );
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.cssMetaDataList;
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }


    //================================================================================
    // Inner Classes
    //================================================================================
    protected static class RecentCellSkin extends MFXSkinBase<VFXCellBase<Recent>, CellBaseBehavior<Recent>> {
        private final ImageView iv;
        private final StackPane ivContainer;

        private final Text header;
        private final Text subHeader;

        private final MFXIconButton previewBtn;
        private final MFXIconButton fileMangerBtn;
        private final MFXIconButton removeBtn;

        // Text caches
        private final TextMeasurementCache hCache;
        private final TextMeasurementCache shCache;

        public RecentCellSkin(RecentCell cell) {
            super(cell);

            // Image
            ivContainer = new StackPane();
            iv = new ImageView();
            iv.setPreserveRatio(true);
            iv.fitWidthProperty().bind(ivContainer.widthProperty());
            iv.imageProperty().bind(cell.itemProperty().flatMap(Recent::previewProperty));
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(DoubleBindingBuilder.build()
                .setMapper(() -> ivContainer.getWidth() - ivContainer.snappedLeftInset() * 2 - ivContainer.snappedRightInset())
                .addSources(ivContainer.widthProperty())
                .get()
            );
            clip.translateXProperty().bind(ivContainer.insetsProperty().map(Insets::getLeft));
            clip.heightProperty().bind(iv.layoutBoundsProperty().map(Bounds::getHeight));
            clip.arcWidthProperty().bind(cell.clipRadiusProperty());
            clip.arcHeightProperty().bind(cell.clipRadiusProperty());
            iv.setClip(clip);
            ivContainer.getChildren().add(iv);

            // Info
            header = new Text();
            header.textProperty().bind(cell.itemProperty().map(r -> {
                String fullName = r.file().getFileName().toString();
                int extensionIndex = fullName.lastIndexOf('.');
                return extensionIndex > 0 ? fullName.substring(0, extensionIndex) : fullName;
            }));
            header.getStyleClass().add("header");
            hCache = createTextMeasurementCache(header.textProperty(), header.fontProperty());

            subHeader = new Text();
            subHeader.textProperty().bind(cell.itemProperty().map(r -> DateTimeUtils.modifiedToHuman(r.lastModified())));
            subHeader.getStyleClass().add("sub-header");
            shCache = createTextMeasurementCache(subHeader.textProperty(), subHeader.fontProperty());

            // Actions
            previewBtn = new MFXIconButton();
            previewBtn.setOnAction(e -> RecentCellEvent.fire(RecentCellEvent.LIVE_PREVIEW_EVENT, cell));
            previewBtn.getStyleClass().add("preview");
            UIUtils.installTooltip(previewBtn, "Live Preview");

            fileMangerBtn = new MFXIconButton();
            fileMangerBtn.setOnAction(e -> RecentCellEvent.fire(RecentCellEvent.FILE_EXPLORER_EVENT, cell));
            fileMangerBtn.getStyleClass().add("show");
            UIUtils.installTooltip(fileMangerBtn, "Open in explorer");

            removeBtn = new MFXIconButton();
            removeBtn.setOnAction(e -> RecentCellEvent.fire(RecentCellEvent.REMOVE_EVENT, cell));
            removeBtn.getStyleClass().add("remove");
            UIUtils.installTooltip(removeBtn, "Remove");

            addListeners();
            getChildren().setAll(ivContainer, header, subHeader, previewBtn, fileMangerBtn, removeBtn);
        }

        private void addListeners() {
            RecentCell cell = getCell();
            InvalidationListener layoutListener = i -> cell.requestLayout();
            listeners(
                OnInvalidated.withListener(cell.hGapProperty(), layoutListener),
                OnInvalidated.withListener(cell.vGapProperty(), layoutListener)
            );
        }

        protected TextMeasurementCache createTextMeasurementCache(ObservableValue<String> textProperty, ObservableValue<Font> fontProperty) {
            TextMeasurementCache cache = new TextMeasurementCache(textProperty, fontProperty);
            cache.setXSnappingFunction(this::snapSizeX);
            cache.setYSnappingFunction(this::snapSizeY);
            return cache;
        }

        protected RecentCell getCell() {
            return (RecentCell) getSkinnable();
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            RecentCell cell = getCell();
            Rect area = Rect.of(0, 0, cell.getWidth(), cell.getHeight())
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset(),
                });

            // Image
            area.withVSpacing(cell.getVGap() * 2.5) // * 2.5 just for the image
                .cutTop(LayoutUtils.snappedBoundHeight(ivContainer))
                .layout(ivContainer::resizeRelocate);

            // Texts
            area.withVSpacing(cell.getVGap());
            area.cutTop(hCache.getSnappedHeight())
                .layout(header::resizeRelocate);
            area.cutTop(shCache.getSnappedHeight())
                .layout(subHeader::resizeRelocate);

            // Actions
            double pvW = LayoutUtils.snappedBoundWidth(previewBtn);
            double pvH = LayoutUtils.snappedBoundHeight(previewBtn);
            double fmW = LayoutUtils.snappedBoundWidth(fileMangerBtn);
            double fmH = LayoutUtils.snappedBoundHeight(fileMangerBtn);
            double rmW = LayoutUtils.snappedBoundWidth(removeBtn);
            double rmH = LayoutUtils.snappedBoundHeight(removeBtn);
            double hGap = cell.getHGap();
            Rect bottom = area.cutBottom(Math.max(rmH, pvH))
                .withHSpacing(hGap);
            bottom.cutLeft(pvW)
                .resize((bw, bh) -> previewBtn.autosize())
                .position(previewBtn::relocate);
            bottom.cutLeft(fmW)
                .resize((bw, bh) -> fileMangerBtn.autosize())
                .position(fileMangerBtn::relocate);
            bottom.cutRight(rmW)
                .resize((bw, bh) -> removeBtn.autosize())
                .position(removeBtn::relocate);
        }

        @Override
        public void dispose() {
            hCache.dispose();
            shCache.dispose();
            super.dispose();
        }
    }

    //================================================================================
    // Custom Events
    //================================================================================
    public static class RecentCellEvent extends Event {

        public static final EventType<RecentCellEvent> ANY = new EventType<>("CELL_ACTION");
        public static final EventType<RecentCellEvent> LIVE_PREVIEW_EVENT = new EventType<>(ANY, "LIVE_PREVIEW_EVENT");
        public static final EventType<RecentCellEvent> FILE_EXPLORER_EVENT = new EventType<>(ANY, "FILE_EXPLORER_EVENT");
        public static final EventType<RecentCellEvent> REMOVE_EVENT = new EventType<>(ANY, "REMOVE_EVENT");

        protected static void fire(EventType<RecentCellEvent> type, RecentCell cell) {
            if (cell.getItem() == null) return;
            fireEvent(cell, new RecentCellEvent(type, cell.getItem()));
        }

        private final Recent recent;

        public RecentCellEvent(EventType<RecentCellEvent> eventType, Recent recent) {
            super(eventType);
            this.recent = recent;
        }

        public Recent getRecent() {
            return recent;
        }
    }
}
