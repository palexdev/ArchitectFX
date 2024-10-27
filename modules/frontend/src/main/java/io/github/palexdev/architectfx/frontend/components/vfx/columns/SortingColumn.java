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

import java.util.Comparator;
import java.util.function.Supplier;

import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.virtualizedfx.cells.base.VFXTableCell;
import io.github.palexdev.virtualizedfx.table.defaults.VFXDefaultTableColumn;
import io.github.palexdev.virtualizedfx.table.defaults.VFXTableColumnBehavior;

public class SortingColumn<T, C extends VFXTableCell<T>> extends VFXDefaultTableColumn<T, C> {
    //================================================================================
    // Properties
    //================================================================================
    private int hash;
    private Comparator<? super T> comparator;

    //================================================================================
    // Constructors
    //================================================================================
    public SortingColumn(String text) {
        super(text, new MFXFontIcon());
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new SortingColumnSkin<>(this);
    }

    @Override
    public Supplier<VFXTableColumnBehavior<T, C>> defaultBehaviorProvider() {
        return () -> new SortingColumnBehavior<>(this);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = super.hashCode();
        }
        return hash;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Comparator<? super T> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }
}