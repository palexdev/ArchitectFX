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

package io.github.palexdev.architectfx.frontend.views.content;

import io.github.palexdev.architectfx.frontend.views.View;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.utils.fx.CSSFragment;
import io.inverno.core.annotation.Bean;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

@Bean
public class PlaceholderView extends View<Pane, Void> {

    //================================================================================
    // Constructors
    //================================================================================
    public PlaceholderView(IEventBus events) {
        super(events);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Pane build() {
        Label label = new Label("Not implemented yet...\nStay tuned!");
        label.getStyleClass().add("placeholder");
        StackPane pane = new StackPane(label);

        // Inline styles because this is temporary
        CSSFragment.Builder.build()
            .select("> .label")
            .fontFamily("Montserrat Bold")
            .style("-fx-font-style: italic")
            .fontSize(57.0)
            .textAlignment(TextAlignment.CENTER)
            .textFill("-md-sys-color-on-surface")
            .applyOn(pane);

        return pane;
    }
}
