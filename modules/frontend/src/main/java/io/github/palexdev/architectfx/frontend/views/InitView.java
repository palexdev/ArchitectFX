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

package io.github.palexdev.architectfx.frontend.views;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.architectfx.frontend.components.ComboBox;
import io.github.palexdev.architectfx.frontend.components.FileInput;
import io.github.palexdev.architectfx.frontend.components.base.ComboCell.SimpleComboCell;
import io.github.palexdev.architectfx.frontend.components.selection.ISelectionModel;
import io.github.palexdev.architectfx.frontend.components.vfx.RecentsTable;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.views.InitView.InitPane;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.bindings.BooleanBindingBuilder;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.inverno.core.annotation.Bean;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Bean
public class InitView extends View<InitPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final AppModel model;
    private final AppSettings settings;

    //================================================================================
    // Constructors
    //================================================================================
    public InitView(IEventBus events, AppModel model, AppSettings settings) {
        super(events);
        this.model = model;
        this.settings = settings;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected InitPane build() {
        return new InitPane();
    }

    @Override
    protected void onAppReady() {
        super.root = build();
    }

    @Override
    public String title() {
        return "Project Hub";
    }

    //================================================================================
    // View Class
    //================================================================================
    protected class InitPane extends VBox {
        private static final String HEADER = "Welcome to Your Project Hub!";
        private static final String SUB_HEADER = "Start by selecting a file or continue where you left off.\n Choose your preferred mode to begin!";

        private final Label header;
        private final Label subHeader;

        private final RecentsTable recentsTable;
        private final FileInput input;

        private final ComboBox<Tool> toolCombo;
        private final MFXIconButton removeBtn;
        private final MFXIconButton loadBtn;

        protected InitPane() {
            // Combo, needs to be instantiated early, belongs to actions
            toolCombo = new ComboBox<>(
                FXCollections.observableArrayList(Tool.values()),
                SimpleComboCell::new
            );

            // Header
            header = new Label(HEADER);
            header.getStyleClass().add("header");
            subHeader = new Label(SUB_HEADER);
            subHeader.getStyleClass().add("sub-header");

            // Table & DnD
            recentsTable = new RecentsTable(model.recents());
            VFXScrollPane vsp = recentsTable.makeScrollable();
            input = new FileInput(f -> model.run(toolCombo.getSelectedItem(), f), settings.lastDir().get());

            // Split
            HBox split = new HBox(vsp, input);
            split.getStyleClass().add("split");
            HBox.setHgrow(vsp, Priority.ALWAYS);
            HBox.setHgrow(input, Priority.ALWAYS);
            VBox.setVgrow(split, Priority.ALWAYS);
            vsp.maxWidthProperty().bind(split.widthProperty().divide(2.0));
            input.maxWidthProperty().bind(split.widthProperty().divide(2.0));

            // Actions
            Tool lastTool;
            try {
                lastTool = Tool.valueOf(settings.lastTool().get());
            } catch (Exception ex) {
                lastTool = Tool.PREVIEW;
            }
            toolCombo.selectItem(lastTool);

            removeBtn = new MFXIconButton().outlined();
            removeBtn.disableProperty().bind(recentsTable.getSelectionModel().selection().emptyProperty());
            removeBtn.setOnAction(e -> model.recents().remove(recentsTable.getSelectionModel().getSelectedItem()));
            removeBtn.getStyleClass().add("warning");

            loadBtn = new MFXIconButton().outlined();
            loadBtn.disableProperty().bind(BooleanBindingBuilder.build()
                .setMapper(() -> {
                    ISelectionModel<Recent> sm = recentsTable.getSelectionModel();
                    return sm.isEmpty() || toolCombo.getSelectedItem() == Tool.EDIT;
                })
                .addSources(recentsTable.getSelectionModel().selection(), toolCombo.selection())
                .get()
            );
            loadBtn.getStyleClass().add("success");

            HBox actionsBox = new HBox(removeBtn, toolCombo, loadBtn);
            actionsBox.getStyleClass().add("actions");

            getChildren().addAll(header, subHeader, split, actionsBox);
            getStyleClass().add("init-view");

            // TODO for debugging
            CSSFX.start(this);
            getStylesheets().add(Resources.load("css/views/InitView.css"));
        }
    }
}
