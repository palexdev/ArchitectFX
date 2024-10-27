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

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.palexdev.architectfx.frontend.components.base.Sortable;
import io.github.palexdev.architectfx.frontend.components.base.WithSelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.ISelectionModel;
import io.github.palexdev.architectfx.frontend.components.selection.SelectionModel;
import io.github.palexdev.architectfx.frontend.components.vfx.columns.SortingColumn;
import io.github.palexdev.architectfx.frontend.components.vfx.rows.SelectableRow;
import io.github.palexdev.architectfx.frontend.enums.SortType;
import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.utils.DateTimeUtils;
import io.github.palexdev.mfxcore.utils.converters.FunctionalStringConverter;
import io.github.palexdev.virtualizedfx.cells.VFXSimpleTableCell;
import io.github.palexdev.virtualizedfx.cells.base.VFXTableCell;
import io.github.palexdev.virtualizedfx.enums.ColumnsLayoutMode;
import io.github.palexdev.virtualizedfx.table.VFXTable;
import io.github.palexdev.virtualizedfx.table.VFXTableRow;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.input.MouseEvent;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;

public class RecentsTable extends VFXTable<Recent> implements Sortable<Recent>, WithSelectionModel<Recent> {
    //================================================================================
    // Properties
    //================================================================================
    private final SortedList<Recent> backingList;
    private final ISelectionModel<Recent> selectionModel;

    private final ReadOnlyObjectWrapper<SortState<Recent>> sortState = new ReadOnlyObjectWrapper<>();
    private final Map<Integer, SortType> lastSortMap = new HashMap<>();

    //================================================================================
    // Constructors
    //================================================================================
    public RecentsTable(ObservableList<Recent> recents) {
        backingList = new SortedList<>(recents);
        setItems(backingList);

        setColumnsLayoutMode(ColumnsLayoutMode.VARIABLE);
        selectionModel = new SelectionModel<>(recents);
        selectionModel.setAllowsMultipleSelection(false);

        // Path column
        SortingColumn<Recent, VFXTableCell<Recent>> pathColumn = new SortingColumn<>("File");
        pathColumn.setCellFactory(r -> new VFXSimpleTableCell<>(r, Recent::file));
        pathColumn.setComparator(Comparator.comparing(Recent::file));

        // Date column
        SortingColumn<Recent, VFXTableCell<Recent>> dateColumn = new SortingColumn<>("Last Modified");
        dateColumn.setCellFactory(r -> new VFXSimpleTableCell<>(r, Recent::timestamp, FunctionalStringConverter.to(l -> {
            ZonedDateTime zdt = DateTimeUtils.toDateTime(l);
            return DateTimeUtils.DATE_TIME_FORMATTER.format(zdt);
        })));
        dateColumn.setComparator(Comparator.comparingLong(Recent::timestamp));
        sort(new SortState<>(dateColumn.hashCode(), SortType.DESCENDING, dateColumn.getComparator()));

        // Acquire focus on mouse pressed
        intercept(this, MouseEvent.MOUSE_PRESSED)
            .process(e -> requestFocus())
            .register();

        getColumns().addAll(pathColumn, dateColumn);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Function<Recent, VFXTableRow<Recent>> defaultRowFactory() {
        return SelectableRow::new;
    }

    @Override
    public void sort(SortState<Recent> state) {
        Comparator<? super Recent> comparator = state.typeToComparator();
        backingList.setComparator(comparator);
        setSortState(state);
        lastSortMap.put(state.id(), state.sortType());
    }

    @Override
    public SortType lastSortType(int id) {
        return lastSortMap.getOrDefault(id, SortType.NONE);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    @Override
    public ISelectionModel<Recent> getSelectionModel() {
        return selectionModel;
    }

    public SortState<Recent> getSortState() {
        return sortState.get();
    }

    @Override
    public ReadOnlyObjectProperty<SortState<Recent>> sortStateProperty() {
        return sortState.getReadOnlyProperty();
    }

    protected void setSortState(SortState<Recent> sortState) {
        this.sortState.set(sortState);
    }
}
