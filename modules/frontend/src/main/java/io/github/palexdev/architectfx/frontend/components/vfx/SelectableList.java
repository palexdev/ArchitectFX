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

import java.util.function.Function;

import io.github.palexdev.architectfx.frontend.components.base.WithSelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.ISelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.SelectionModel;
import io.github.palexdev.architectfx.frontend.components.vfx.cells.SelectableCell;
import io.github.palexdev.virtualizedfx.list.VFXList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectableList<T, C extends SelectableCell<T>> extends VFXList<T, C> implements WithSelectionModel<T> {
    //================================================================================
    // Properties
    //================================================================================
    private final ISelectionModel<T> selectionModel;

    //================================================================================
    // Constructors
    //================================================================================
    public SelectableList() {
        this(FXCollections.observableArrayList(), null);
    }

    public SelectableList(ObservableList<T> items, Function<T, C> cellFactory) {
        super(items, cellFactory);
        this.selectionModel = new SelectionModel<>(itemsProperty());
    }

    public SelectableList(ObservableList<T> items, Function<T, C> cellFactory, ISelectionModel<T> selectionModel) {
        super(items, cellFactory);
        this.selectionModel = selectionModel;
    }

    //================================================================================
    // Getters
    //================================================================================
    @Override
    public ISelectionModel<T> getSelectionModel() {
        return selectionModel;
    }
}
