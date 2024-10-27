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

package io.github.palexdev.architectfx.frontend.components.selection;

import java.util.List;
import java.util.Optional;

import io.github.palexdev.mfxcore.base.beans.range.IntegerRange;
import javafx.beans.property.MapProperty;

@SuppressWarnings("unchecked")
public interface ISelectionModel<T> {

    boolean contains(int index);

    boolean contains(T element);

    void clearSelection();

    void deselectIndex(int index);

    void deselectItem(T item);

    void deselectIndexes(int... indexes);

    void deselectIndexes(IntegerRange range);

    void deselectItems(T... items);

    void selectIndex(int index);

    void selectItem(T item);

    void selectIndexes(Integer... indexes);

    void selectIndexes(IntegerRange range);

    void selectItems(T... items);

    void expandSelection(int index, boolean fromLast);

    void replaceSelection(Integer... indexes);

    void replaceSelection(IntegerRange range);

    void replaceSelection(T... items);

    MapProperty<Integer, T> selection();

    List<T> getSelectedItems();

    default int size() {
        return selection().size();
    }

    default boolean isEmpty() {
        return selection().isEmpty();
    }

    default T getSelectedItem() {
        return (size() == 0) ? null : getSelectedItems().getFirst();
    }

    default Optional<T> getSelectedItemOpt() {
        return Optional.ofNullable(getSelectedItem());
    }

    default T getLastSelectedItem() {
        int size = size();
        return (size == 0) ? null : getSelectedItems().get(size - 1);
    }

    default Optional<T> getLastSelectedItemOpt() {
        return Optional.ofNullable(getLastSelectedItem());
    }

    boolean allowsMultipleSelection();

    void setAllowsMultipleSelection(boolean allowsMultipleSelection);

    void dispose();
}
