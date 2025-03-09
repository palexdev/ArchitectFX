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

import io.github.palexdev.architectfx.backend.model.Initializable;
import io.github.palexdev.architectfx.examples.common.TextField;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.virtualizedfx.grid.VFXGrid;
import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotesController implements Initializable {
    private Stage mainWindow;
    private NotesAppModel model;

    // Header
    private TextField filterField;
    private MFXIconButton favFilter;

    // Sidebar
    private MFXIconButton addBtn;
    private MFXFontIcon highIcon;
    private MFXFontIcon mediumIcon;
    private MFXFontIcon lowIcon;
    private MFXFontIcon standardIcon;

    // Content
    private VFXGrid<Note, NoteCard> grid;

    private NoteEditPopup editPopup;

    private Animation filterDelay;
    private Animation iconsAnimation;
    private boolean show = false;

    @Override
    public void initialize() {
        createEditPopup();

        animateIcons();
        addBtn.setOnAction(e -> animateIcons());
        addBtn.setViewOrder(-1);

        WhenEvent.intercept(highIcon, MouseEvent.MOUSE_CLICKED)
            .condition(e -> e.getButton() == MouseButton.PRIMARY)
            .process(e -> model.createNote(Note.Priority.HIGH))
            .register();
        WhenEvent.intercept(mediumIcon, MouseEvent.MOUSE_CLICKED)
            .condition(e -> e.getButton() == MouseButton.PRIMARY)
            .process(e -> model.createNote(Note.Priority.MEDIUM))
            .register();
        WhenEvent.intercept(lowIcon, MouseEvent.MOUSE_CLICKED)
            .condition(e -> e.getButton() == MouseButton.PRIMARY)
            .process(e -> model.createNote(Note.Priority.LOW))
            .register();
        WhenEvent.intercept(standardIcon, MouseEvent.MOUSE_CLICKED)
            .condition(e -> e.getButton() == MouseButton.PRIMARY)
            .process(e -> model.createNote(Note.Priority.STANDARD))
            .register();

        filterField.textProperty().addListener(i -> filterNotes());
        favFilter.selectedProperty().addListener(i -> filterNotes());

        grid.setItems(model.getNotesUnmodifiable());
        grid.setCellFactory(NoteCard::new);

        When.onInvalidated(grid.itemsProperty())
            .then(o -> grid.autoArrange(1))
            .invalidating(grid.cellSizeProperty())
            .invalidating(grid.widthProperty())
            .invalidating(grid.hSpacingProperty())
            .listen();

        WhenEvent.intercept(grid, NoteCard.NoteCardEvents.ANY)
            .process(this::onNoteCardEvent)
            .register();

        /* Force remove clip */
        // TODO improve in VirtualizedFX
        When.onInvalidated(grid.clipProperty())
            .condition(Objects::nonNull)
            .then(n -> grid.setClip(null))
            .oneShot()
            .listen();
    }

    protected void onNoteCardEvent(NoteCard.NoteCardEvents nce) {
        if (nce.getEventType() == NoteCard.NoteCardEvents.DELETE_NOTE) {
            model.removeNote(nce.getNote());
            return;
        }

        if (nce.getEventType() == NoteCard.NoteCardEvents.EDIT_NOTE) {
            editPopup.edit(mainWindow, Pos.CENTER, nce.getNote());
        }
    }

    protected void filterNotes() {
        if (Animations.isPlaying(filterDelay))
            filterDelay.stop();

        filterDelay = PauseBuilder.build()
            .setDuration(M3Motion.SHORT2)
            .setOnFinished(e -> model.filterNotes(filterField.getText(), favFilter.isSelected()))
            .getAnimation();
        filterDelay.play();
    }

    protected void animateIcons() {
        if (Animations.isPlaying(iconsAnimation))
            iconsAnimation.stop();

        Duration d = M3Motion.MEDIUM4;
        Interpolator curve = M3Motion.STANDARD;
        double targetOpacity = show ? 0.7 : 0.0;
        if (show) {
            iconsAnimation = TimelineBuilder.build()
                .add(KeyFrames.of(d, addBtn.getIcon().rotateProperty(), 180.0, curve))
                .add(KeyFrames.of(d, highIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, highIcon.translateYProperty(), 0.0, curve))
                .add(KeyFrames.of(d, mediumIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, mediumIcon.translateYProperty(), 0.0, curve))
                .add(KeyFrames.of(d, lowIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, lowIcon.translateYProperty(), 0.0, curve))
                .add(KeyFrames.of(d, standardIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, standardIcon.translateYProperty(), 0.0, curve))
                .getAnimation();
        } else {
            iconsAnimation = TimelineBuilder.build()
                .add(KeyFrames.of(d, addBtn.getIcon().rotateProperty(), 0.0, curve))
                .add(KeyFrames.of(d, highIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, highIcon.translateYProperty(), -52.0, curve))
                .add(KeyFrames.of(d, mediumIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, mediumIcon.translateYProperty(), -84.0, curve))
                .add(KeyFrames.of(d, lowIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, lowIcon.translateYProperty(), -116.0, curve))
                .add(KeyFrames.of(d, standardIcon.opacityProperty(), targetOpacity))
                .add(KeyFrames.of(d, standardIcon.translateYProperty(), -148.0, curve))
                .getAnimation();
        }
        show = !show;
        iconsAnimation.play();
    }

    protected void createEditPopup() {
        editPopup = new NoteEditPopup();
        editPopup.setScrimOwner(true);
        editPopup.setDraggable(true);
    }
}
