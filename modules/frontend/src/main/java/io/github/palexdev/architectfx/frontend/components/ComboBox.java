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
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.components.ComboBox.ComboBoxBehavior;
import io.github.palexdev.architectfx.frontend.components.base.ComboCell;
import io.github.palexdev.architectfx.frontend.components.selection.ISelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.SelectionModel;
import io.github.palexdev.architectfx.frontend.components.vfx.SelectableList;
import io.github.palexdev.architectfx.frontend.components.vfx.cells.SelectableCell;
import io.github.palexdev.mfxcomponents.controls.base.MFXControl;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.window.popups.MFXPopup;
import io.github.palexdev.mfxcore.base.properties.functional.FunctionProperty;
import io.github.palexdev.mfxcore.behavior.BehaviorBase;
import io.github.palexdev.mfxcore.builders.bindings.DoubleBindingBuilder;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import javafx.beans.property.MapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

public class ComboBox<T> extends MFXControl<ComboBoxBehavior<T>> {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<T> items;
    private final FunctionProperty<T, ComboCell<T>> cellFactory = new FunctionProperty<>();
    private final ISelectionModel<T> selectionModel;

    //================================================================================
    // Constructors
    //================================================================================
    public ComboBox(Function<T, ComboCell<T>> cellFactory) {
        this(FXCollections.observableArrayList(), cellFactory);
    }

    public ComboBox(ObservableList<T> items, Function<T, ComboCell<T>> cellFactory) {
        this.items = items;
        this.selectionModel = new SelectionModel<>(items);
        selectionModel.setAllowsMultipleSelection(false);
        setCellFactory(cellFactory);

        getStyleClass().setAll(defaultStyleClasses());
        setDefaultBehaviorProvider();
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new ComboBoxSkin<>(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("combo-box");
    }

    @Override
    public Supplier<ComboBoxBehavior<T>> defaultBehaviorProvider() {
        return () -> new ComboBoxBehavior<>(this);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ObservableList<T> getItems() {
        return items;
    }

    public Function<T, ComboCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public FunctionProperty<T, ComboCell<T>> cellFactoryProperty() {
        return cellFactory;
    }

    public void setCellFactory(Function<T, ComboCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public ISelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public void selectItem(T item) {selectionModel.selectItem(item);}

    public void selectIndex(int index) {selectionModel.selectIndex(index);}

    public MapProperty<Integer, T> selection() {return selectionModel.selection();}

    public T getSelectedItem() {return selectionModel.getSelectedItem();}

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class ComboBoxBehavior<T> extends BehaviorBase<ComboBox<T>> {

        public ComboBoxBehavior(ComboBox<T> combo) {
            super(combo);
        }
    }

    protected static class ComboBoxSkin<T> extends MFXSkinBase<ComboBox<T>, ComboBoxBehavior<T>> {
        private final ComboCell<T> comboCell;
        private final MFXIconButton arrowBtn;
        private final MFXPopup popup;
        private final SelectableList<T, SelectableCell<T>> popupList;

        public ComboBoxSkin(ComboBox<T> combo) {
            super(combo);

            // Popup
            popupList = new SelectableList<>(combo.getItems(), t -> new SelectableCell<>(t) {
                // Disallow empty selection!
                @Override
                protected void onSelectionEvent(ISelectionModel<T> sm) {
                    int index = getIndex();
                    boolean selected = isSelected();
                    if (selected) return;
                    sm.selectIndex(index);
                }
            }, combo.getSelectionModel());
            popup = buildPopup();

            // Children
            comboCell = combo.getCellFactory().apply(null);
            arrowBtn = new MFXIconButton().outlined();
            arrowBtn.getGraphic().rotateProperty().bind(popup.stateProperty().map(s -> s.isOpening() ? 180.0 : 0.0));
            arrowBtn.setOnAction(e -> {
                if (popup.isShowing()) {
                    popup.hide();
                } else {
                    popup.show(combo, Pos.BOTTOM_CENTER);
                }
            });
            getChildren().addAll(comboCell.toNode(), arrowBtn);
            addListeners();
        }

        protected void addListeners() {
            ISelectionModel<T> sm = getSkinnable().getSelectionModel();
            listeners(
                onInvalidated(sm.selection())
                    .then(s -> {
                        popup.hide();
                        T item = sm.getLastSelectedItem();
                        comboCell.updateItem(item);
                    })
                    .executeNow()
            );
        }

        protected MFXPopup buildPopup() {
            ComboBox<T> combo = getSkinnable();
            MFXPopup popup = new MFXPopup();
            VFXScrollPane content = popupList.makeScrollable();
            popup.setContent(content);
            popup.setAnchor(Pos.BOTTOM_CENTER);
            content.setMinHeight(USE_PREF_SIZE);
            content.prefHeightProperty().bind(DoubleBindingBuilder.build()
                .setMapper(() -> combo.getItems().size() * popupList.getCellSize() + snappedTopInset() + snappedBottomInset())
                .addSources(combo.getItems(), popupList.cellSizeProperty())
                .get()
            );
            content.prefWidthProperty().bind(combo.widthProperty().subtract(12));
            popup.setOnShown(e -> popup.reposition());
            return popup;
        }

        @Override
        public double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefWidth(height);
        }

        @Override
        public double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            return getSkinnable().prefHeight(width);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            layoutInArea(
                comboCell.toNode(),
                x, y, w, h, 0,
                HPos.LEFT, VPos.CENTER
            );

            arrowBtn.resize(
                LayoutUtils.boundWidth(arrowBtn),
                h + snappedTopInset() + snappedBottomInset()
            );
            positionInArea(
                arrowBtn,
                x + snappedLeftInset(), y, w, h, 0,
                HPos.RIGHT, VPos.CENTER
            );
        }
    }
}
