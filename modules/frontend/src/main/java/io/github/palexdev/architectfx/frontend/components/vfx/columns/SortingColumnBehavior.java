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

package io.github.palexdev.architectfx.frontend.components.vfx.columns;

import io.github.palexdev.architectfx.frontend.components.base.Sortable;
import io.github.palexdev.architectfx.frontend.components.vfx.SortState;
import io.github.palexdev.architectfx.frontend.enums.SortType;
import io.github.palexdev.mfxcore.utils.EnumUtils;
import io.github.palexdev.virtualizedfx.cells.base.VFXTableCell;
import io.github.palexdev.virtualizedfx.table.VFXTable;
import io.github.palexdev.virtualizedfx.table.defaults.VFXTableColumnBehavior;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class SortingColumnBehavior<T, C extends VFXTableCell<T>> extends VFXTableColumnBehavior<T, C> {
    //================================================================================
    // Properties
    //================================================================================
    private boolean wasResized = false;

    //================================================================================
    // Constructors
    //================================================================================
    public SortingColumnBehavior(SortingColumn<T, C> column) {
        super(column);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public void init() {
        super.init();
        resizer.setResizeHandler((node, x, y, w, h) -> {
            getNode().resize(w);
            wasResized = true;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(MouseEvent e) {
        SortingColumn<T, C> column = getNode();
        VFXTable<T> table = column.getTable();
        if (table.isEmpty() ||
            column.getComparator() == null ||
            e.getButton() != MouseButton.PRIMARY ||
            wasResized
        ) {
            wasResized = false;
            return;
        }

        if (table instanceof Sortable<?>) {
            int id = column.hashCode();
            Sortable<T> sortable = (Sortable<T>) table;
            SortState<T> sortState = sortable.sortStateProperty().get();
            SortType lastSortType = sortable.lastSortType(id);
            SortType newType;
            if (sortState != null && sortState.id() != id && lastSortType != SortType.NONE) {
                newType = lastSortType;
            } else {
                newType = EnumUtils.next(SortType.class, lastSortType);
            }
            SortState<T> newState = new SortState<>(id, newType, column.getComparator());
            sortable.sort(newState);
        }
    }

    @Override
    public SortingColumn<T, C> getNode() {
        return (SortingColumn<T, C>) super.getNode();
    }
}
