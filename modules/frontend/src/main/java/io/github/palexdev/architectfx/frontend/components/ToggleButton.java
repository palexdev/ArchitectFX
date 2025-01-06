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

import java.util.function.Supplier;

import io.github.palexdev.mfxcomponents.behaviors.MFXButtonBehaviorBase;
import io.github.palexdev.mfxcomponents.controls.MaterialSurface;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXButton;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.skins.MFXButtonSkin;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.selection.Selectable;
import io.github.palexdev.mfxcore.selection.SelectionGroup;
import io.github.palexdev.mfxcore.selection.SelectionGroupProperty;
import io.github.palexdev.mfxcore.selection.SelectionProperty;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.scene.input.MouseEvent;

import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

// TODO convenience class, should be implemented properly in MFXComponents one day
public class ToggleButton extends MFXButton implements Selectable {
    //================================================================================
    // Properties
    //================================================================================
    private final MFXIconButton icon;
    private When<?> sWhen;

    //================================================================================
    // Constructors
    //================================================================================
    public ToggleButton(String text) {
        this(text, new MFXFontIcon());
    }

    public ToggleButton(String text, MFXFontIcon icon) {
        super(text);
        this.icon = new MFXIconButton(icon)
            .filled()
            .asToggle();
        this.icon.setMouseTransparent(true);
        this.icon.setFocusTraversable(false);
        onInvalidated(selectedProperty())
            .then(s -> PseudoClasses.SELECTED.setOn(this, s))
            .executeNow()
            .listen();
        setGraphic(this.icon);
        this.icon.setIcon(icon);
    }

    //================================================================================
    // Methods
    //================================================================================
    public void onSelected(Runnable action) {
        if (sWhen != null) sWhen.dispose();
        sWhen = onInvalidated(selectedProperty())
            .condition(s -> s)
            .then(s -> action.run())
            .listen();
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public ToggleButton elevated() {
        super.elevated();
        return this;
    }

    @Override
    public ToggleButton filled() {
        super.filled();
        return this;
    }

    @Override
    public ToggleButton outlined() {
        super.outlined();
        return this;
    }

    @Override
    public ToggleButton text() {
        super.text();
        return this;
    }

    @Override
    public ToggleButton tonal() {
        super.tonal();
        return this;
    }

    @Override
    public Supplier<MFXButtonBehaviorBase<MFXButton>> defaultBehaviorProvider() {
        return () -> new MFXButtonBehaviorBase<>(this) {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setSelected(true);
            }
        };
    }

    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new MFXButtonSkin<>(this) {
            {
                surface.getStates().add(new MaterialSurface.State(
                    -1,
                    n -> isSelected(),
                    MaterialSurface::getFocusedOpacity
                ));
            }
        };
    }

    @Override
    public boolean isSelected() {
        return icon.isSelected();
    }

    @Override
    public SelectionProperty selectedProperty() {
        return icon.selectedProperty();
    }

    @Override
    public void setSelected(boolean selected) {
        icon.setSelected(selected);
    }

    @Override
    public SelectionGroup getSelectionGroup() {
        return icon.getSelectionGroup();
    }

    @Override
    public SelectionGroupProperty selectionGroupProperty() {
        return icon.selectionGroupProperty();
    }

    @Override
    public void setSelectionGroup(SelectionGroup group) {
        icon.setSelectionGroup(group);
    }
}
