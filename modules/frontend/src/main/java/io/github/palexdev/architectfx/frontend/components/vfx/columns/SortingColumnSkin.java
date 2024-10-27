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

import java.util.Objects;

import io.github.palexdev.architectfx.frontend.components.base.Sortable;
import io.github.palexdev.architectfx.frontend.components.vfx.SortState;
import io.github.palexdev.architectfx.frontend.enums.SortType;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.virtualizedfx.cells.base.VFXTableCell;
import io.github.palexdev.virtualizedfx.table.VFXTable;
import io.github.palexdev.virtualizedfx.table.VFXTableColumn;
import io.github.palexdev.virtualizedfx.table.defaults.VFXDefaultTableColumn;
import io.github.palexdev.virtualizedfx.table.defaults.VFXDefaultTableColumnSkin;
import io.github.palexdev.virtualizedfx.table.defaults.VFXTableColumnBehavior;
import javafx.animation.Animation;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;
import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

public class SortingColumnSkin<T, C extends VFXTableCell<T>> extends VFXDefaultTableColumnSkin<T, C> {
    //================================================================================
    // Properties
    //================================================================================
    private Animation iconAnimation;

    //================================================================================
    // Constructors
    //================================================================================
    public SortingColumnSkin(VFXDefaultTableColumn<T, C> column) {
        super(column);
        addListeners();
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void addListeners() {
        VFXTableColumn<T, C> column = getSkinnable();
        VFXTable<T> table = column.getTable();
        if (table instanceof Sortable<?> sortable) {
            listeners(
                onInvalidated(sortable.sortStateProperty())
                    .condition(Objects::nonNull)
                    .then(s -> {
                        Node icon = column.getGraphic();
                        if (icon == null) return;
                        handleIcon(s);
                    })
            );

            SortState<?> initialState = sortable.sortStateProperty().get();
            if (initialState != null) {
                onInvalidated(table.itemsProperty())
                    .condition(i -> !i.isEmpty())
                    .then(i -> {
                        if (initialState.id() == column.hashCode()) {
                            Node icon = column.getGraphic();
                            if (icon == null) return;
                            handleIcon(initialState);
                        }
                    })
                    .oneShot()
                    .listen();
            }
        }
    }

    protected void handleIcon(SortState<?> state) {
        VFXTableColumn<T, C> column = getSkinnable();
        Node icon = column.getGraphic();
        double targetOpacity = (state.id() == column.hashCode() && state.sortType() != SortType.NONE) ? 1.0 : 0.0;
        double targetRotate = state.typeToRotation();
        if (targetOpacity != 0.0) {
            icon.setRotate(targetRotate);
        }
        TimelineBuilder.build()
            .add(KeyFrames.of(M3Motion.SHORT2, icon.opacityProperty(), targetOpacity, M3Motion.STANDARD))
            .getAnimation()
            .play();
    }

    @Override
    protected void handleIcon(Node oldIcon, Node newIcon) {
        if (oldIcon != null) getChildren().remove(oldIcon);
        if (newIcon != null) {
            getChildren().addFirst(newIcon);
            newIcon.setOpacity(0.0);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected void initBehavior(VFXTableColumnBehavior<T, C> behavior) {
        super.initBehavior(behavior);
        VFXTableColumn<T, C> column = getSkinnable();
        events(
            intercept(column, MouseEvent.MOUSE_CLICKED)
                .process(behavior::mouseClicked)
        );
    }
}
