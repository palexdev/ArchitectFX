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

import io.github.palexdev.architectfx.frontend.components.base.WithSelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.ISelectionModel;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.builders.bindings.BooleanBindingBuilder;
import io.github.palexdev.virtualizedfx.base.VFXContainer;
import io.github.palexdev.virtualizedfx.cells.VFXSimpleCell;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;

public class SelectableCell<T> extends VFXSimpleCell<T> {
    //================================================================================
    // Properties
    //================================================================================
    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(false) {
        @Override
        protected void invalidated() {
            PseudoClasses.SELECTED.setOn(SelectableCell.this, get());
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public SelectableCell(T item) {
        super(item);
    }

    public SelectableCell(T item, StringConverter<T> converter) {
        super(item, converter);
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void onSelectionEvent(ISelectionModel<T> sm) {
        int index = getIndex();
        boolean selected = isSelected();
        if (selected) {
            sm.deselectIndex(index);
        } else {
            sm.selectIndex(index);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @SuppressWarnings("unchecked")
    @Override
    public void onCreated(VFXContainer<T> container) {
        super.onCreated(container);
        if (container instanceof WithSelectionModel<?>) {
            ISelectionModel<T> sm = ((WithSelectionModel<T>) container).getSelectionModel();
            selected.bind(BooleanBindingBuilder.build()
                .setMapper(() -> sm.contains(getIndex()))
                .addSources(sm.selection(), indexProperty())
                .get()
            );

            intercept(this, MouseEvent.MOUSE_CLICKED)
                .condition(e -> e.getButton() == MouseButton.PRIMARY)
                .process(e -> onSelectionEvent(sm))
                .asFilter()
                .register();
        }
    }


    //================================================================================
    // Getters/Setters
    //================================================================================

    public boolean isSelected() {
        return selected.get();
    }

    public ReadOnlyBooleanProperty selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    protected void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
