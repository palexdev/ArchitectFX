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


import java.util.function.Function;

import io.github.palexdev.architectfx.frontend.components.selection.SelectionModel;
import io.github.palexdev.virtualizedfx.cells.base.VFXCell;
import io.github.palexdev.virtualizedfx.list.VFXList;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;

public class SelectableVFXList<T, C extends VFXCell<T>> extends VFXList<T, C> {
    //================================================================================
    // Properties
    //================================================================================
    private final SelectionModel<T> selectionModel = new SelectionModel<>(itemsProperty());

    //================================================================================
    // Constructors
    //================================================================================
    public SelectableVFXList() {}

    public SelectableVFXList(ObservableList<T> items, Function<T, C> cellFactory) {
        super(items, cellFactory);
    }

    public SelectableVFXList(ObservableList<T> items, Function<T, C> cellFactory, Orientation orientation) {
        super(items, cellFactory, orientation);
    }

    //================================================================================
    // Getters
    //================================================================================
    public SelectionModel<T> getSelectionModel() {
        return selectionModel;
    }
}
