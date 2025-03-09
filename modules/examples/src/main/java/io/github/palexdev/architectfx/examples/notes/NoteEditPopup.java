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

package io.github.palexdev.architectfx.examples.notes;

import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.model.Initializable;
import io.github.palexdev.architectfx.examples.Launcher;
import io.github.palexdev.architectfx.examples.common.MFXDialog;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXButton;
import io.github.palexdev.mfxcore.enums.Zone;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.utils.resize.RegionDragResizer;
import java.io.IOException;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.tinylog.Logger;

public class NoteEditPopup extends MFXDialog<Void> {
    //================================================================================
    // Properties
    //================================================================================
    private Note note;

    //================================================================================
    // Constructors
    //================================================================================
    public NoteEditPopup() {
        loadContent();
    }

    //================================================================================
    // Methods
    //================================================================================
    public void edit(Window owner, Pos anchor, Note note) {
        this.note = note;
        if (note != null) showAndWait(owner, anchor);
    }

    protected void loadContent() {
        try {
            JUIFXLoader loader = new JUIFXLoader();
            loader.config().setControllerFactory(Controller::new);
            UILoader.Loaded<Node> res = loader.load(Launcher.load("notes/EditPopup.jui"));
            setContent(res.root());
        } catch (IOException ex) {
            Logger.error("Failed to load edit dialog UI because:\n{}", ex);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Void getResult() {
        return null;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected class Controller implements Initializable {
        private VBox container;

        private TextField titleField;
        private TextArea bodyArea;

        private MFXButton saveBtn;
        private MFXButton cancelBtn;

        @Override
        public void initialize() {
            new RegionDragResizer(container) {
                @Override
                protected void consume(MouseEvent event) {
                }
            }.setAllowedZones(
                Zone.CENTER_RIGHT,
                Zone.BOTTOM_RIGHT,
                Zone.BOTTOM_CENTER
            ).makeResizable();

            WhenEvent.intercept(NoteEditPopup.this, WindowEvent.WINDOW_SHOWING)
                .process(e -> {
                    titleField.setText(Optional.ofNullable(note).map(Note::getTitle).orElse(""));
                    bodyArea.setText(Optional.ofNullable(note).map(Note::getText).orElse(""));
                })
                .register();

            saveBtn.setOnAction(e -> {
                note.setTitle(titleField.getText());
                note.setText(bodyArea.getText());
                hide();
            });
            cancelBtn.setOnAction(e -> hide());
        }
    }
}
