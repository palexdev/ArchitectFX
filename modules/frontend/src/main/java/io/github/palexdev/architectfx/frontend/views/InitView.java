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

import io.github.palexdev.architectfx.frontend.components.ComboBox;
import io.github.palexdev.architectfx.frontend.components.FileInput;
import io.github.palexdev.architectfx.frontend.components.base.ComboCell.SimpleComboCell;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.components.vfx.RecentsTable;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.theming.ThemeMode;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.InitView.InitPane;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.inverno.core.annotation.Bean;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.frontend.theming.ThemeEngine.DARK_PSEUDO_CLASS;

@Bean
public class InitView extends View<InitPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final ThemeEngine themeEngine;
    private final AppModel model;
    private final AppSettings settings;
    private final HostServices hostServices;

    //================================================================================
    // Constructors
    //================================================================================
    public InitView(IEventBus events, ThemeEngine themeEngine, AppModel model, AppSettings settings, HostServices hostServices) {
        super(events);
        this.themeEngine = themeEngine;
        this.model = model;
        this.settings = settings;
        this.hostServices = hostServices;
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

        // TODO extract create buttons method from LivePreview (?)
        private final ComboBox<Tool> toolCombo;
        private final MFXIconButton themeBtn;
        private final MFXIconButton removeBtn;
        private final MFXIconButton showBtn;
        private final MFXIconButton loadBtn;

        protected InitPane() {
            // Combo, needs to be instantiated early, belongs to actions
            toolCombo = new ComboBox<>(
                FXCollections.observableArrayList(Tool.PREVIEW),
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

            themeBtn = new MFXIconButton().tonal();
            themeBtn.setOnAction(e -> {
                themeEngine.nextMode();
                themeBtn.pseudoClassStateChanged(DARK_PSEUDO_CLASS, themeEngine.getThemeMode() == ThemeMode.DARK);
            });
            themeBtn.getStyleClass().add("theme-mode");
            UIUtils.installTooltip(themeBtn, "Light/Dark Mode");

            removeBtn = new MFXIconButton().outlined();
            removeBtn.disableProperty().bind(recentsTable.getSelectionModel().selection().emptyProperty());
            removeBtn.setOnAction(e -> model.recents().remove(recentsTable.getSelectionModel().getSelectedItem()));
            removeBtn.getStyleClass().add("warning");
            UIUtils.installTooltip(removeBtn, "Delete Entry");

            showBtn = new MFXIconButton().outlined();
            showBtn.disableProperty().bind(recentsTable.getSelectionModel().selection().emptyProperty());
            showBtn.setOnAction(e -> openInFileManager());
            showBtn.getStyleClass().add("show");
            UIUtils.installTooltip(showBtn, "Show in File Manager");

            loadBtn = new MFXIconButton().outlined();
            loadBtn.disableProperty().bind(recentsTable.getSelectionModel().selection().emptyProperty());
            loadBtn.setOnAction(e -> model.run(toolCombo.getSelectedItem(), getSelectedItem().file().toFile()));
            loadBtn.getStyleClass().add("success");
            UIUtils.installTooltip(loadBtn, "Load with selected tool");

            Box actionsBox = new Box(
                Box.Direction.ROW,
                themeBtn,
                Box.separator(),
                removeBtn, showBtn, toolCombo, loadBtn,
                Box.separator()
            );
            actionsBox.getStyleClass().add("actions");

            getChildren().addAll(header, subHeader, split, actionsBox);
            getStyleClass().add("init-view");
        }

        protected void openInFileManager() {
            try {
                Recent item = getSelectedItem();
                hostServices.showDocument(item.file().getParent().toUri().toString());
            } catch (Exception ex) {
                Logger.warn("Could not show file in file manager because:\n{}", ex);
            }
        }

        protected Recent getSelectedItem() {
            return recentsTable.getSelectionModel().getSelectedItem();
        }
    }
}
