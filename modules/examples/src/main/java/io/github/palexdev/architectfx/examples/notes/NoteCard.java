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

import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.builders.bindings.StringBindingBuilder;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.cells.CellBaseBehavior;
import io.github.palexdev.virtualizedfx.cells.VFXCellBase;
import io.github.palexdev.virtualizedfx.events.VFXContainerEvent;
import java.util.List;
import java.util.Optional;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventType;

public class NoteCard extends VFXCellBase<Note> {

    //================================================================================
    // Constructors
    //================================================================================
    public NoteCard(Note item) {
        super(item);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public List<String> defaultStyleClasses() {
        return List.of("note-card");
    }

    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new NoteCardSkin(this);
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class NoteCardSkin extends SkinBase<VFXCellBase<Note>, CellBaseBehavior<Note>> {
        // Header
        private Label title;
        private final MFXIconButton favIcon;

        // Body
        private final Label body;

        // Footer
        private final Label date;
        private final MFXIconButton editBtn;
        private final MFXIconButton deleteBtn;

        protected double V_SPACING = 12.0;

        private PseudoClass lastActivePriority;

        public NoteCardSkin(NoteCard cell) {
            super(cell);

            title = new Label();
            title.getStyleClass().add("title");

            favIcon = new MFXIconButton().filled();
            favIcon.getStyleClass().add("fav");
            favIcon.setOnAction(e -> Optional.ofNullable(cell.getItem())
                .ifPresent(n -> n.setFavorite(!n.isFavorite()))
            );

            body = new Label();
            body.getStyleClass().add("body");

            date = new Label();
            date.getStyleClass().add("date");

            editBtn = new MFXIconButton().filled();
            editBtn.getStyleClass().add("edit");
            editBtn.setOnAction(e -> NoteCardEvents.fire(NoteCardEvents.EDIT_NOTE, cell));

            deleteBtn = new MFXIconButton().filled();
            deleteBtn.getStyleClass().add("delete");
            deleteBtn.setOnAction(e -> NoteCardEvents.fire(NoteCardEvents.DELETE_NOTE, cell));

            getChildren().setAll(title, favIcon, body, date, editBtn, deleteBtn);

            listeners(
                When.onInvalidated(cell.itemProperty())
                    .then(n -> update())
                    .executeNow(() -> cell.getItem() != null),
                When.onInvalidated(cell.itemProperty().flatMap(Note::favoriteProperty))
                    .then(v -> PseudoClasses.setOn(cell, "fav", v))
            );
        }

        protected void update() {
            VFXCellBase<Note> cell = getSkinnable();
            Note note = cell.getItem();

            updatePriorityClass();

            title.textProperty().bind(StringBindingBuilder.build()
                .setMapper(() -> {
                    String t = note.getTitle();
                    if (t.isBlank()) {
                        PseudoClasses.setOn(title, "empty", true);
                        return "New Note";
                    }

                    PseudoClasses.setOn(title, "empty", false);
                    return t;
                })
                .addSources(note.titleProperty())
                .get()
            );

            body.textProperty().bind(note.textProperty());

            date.setText(note.getDate());

            PseudoClasses.setOn(cell, "fav", note.isFavorite());
        }

        protected void updatePriorityClass() {
            VFXCellBase<Note> cell = getSkinnable();
            Note note = cell.getItem();
            Note.Priority priority = note.getPriority();
            String toString = priority.name().toLowerCase();

            if (lastActivePriority != null) {
                if (lastActivePriority.getPseudoClassName().equals(toString)) return;
                cell.pseudoClassStateChanged(lastActivePriority, false);
            }

            lastActivePriority = PseudoClass.getPseudoClass(toString);
            pseudoClassStateChanged(lastActivePriority, true);
        }

        @Override
        protected void initBehavior(CellBaseBehavior<Note> noteCellBaseBehavior) {
            VFXCellBase<Note> cell = getSkinnable();
            events(
                WhenEvent.intercept(cell, VFXContainerEvent.UPDATE)
                    .process(e -> update())
            );
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            Rect area = Rect.of(x, y, w, h)
                .withVSpacing(V_SPACING)
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            Rect top = area.cutTop(Math.max(
                LayoutUtils.snappedBoundHeight(title),
                LayoutUtils.snappedBoundHeight(favIcon)
            ));
            top.cutRight(LayoutUtils.snappedBoundWidth(favIcon))
                .layout(favIcon::resizeRelocate);
            top.layout(title::resizeRelocate);

            Rect bottom = area.cutBottom(Math.max(
                LayoutUtils.snappedBoundHeight(date),
                Math.max(
                    LayoutUtils.snappedBoundHeight(editBtn),
                    LayoutUtils.snappedBoundHeight(deleteBtn)
                )
            )).withHSpacing(12.0);
            bottom.cutRight(LayoutUtils.snappedBoundWidth(deleteBtn))
                .layout(deleteBtn::resizeRelocate);
            bottom.cutRight(LayoutUtils.snappedBoundWidth(editBtn))
                .layout(editBtn::resizeRelocate);
            bottom.layout(date::resizeRelocate);

            area.layout(body::resizeRelocate);
        }
    }

    public static class NoteCardEvents extends Event {

        public static final EventType<NoteCardEvents> ANY = new EventType<>(EventType.ROOT, "ANY");
        public static final EventType<NoteCardEvents> EDIT_NOTE = new EventType<>(ANY, "EDIT_NOTE");
        public static final EventType<NoteCardEvents> DELETE_NOTE = new EventType<>(ANY, "DELTE_NOTE");

        protected static void fire(EventType<NoteCardEvents> type, NoteCard cell) {
            Note note = cell.getItem();
            if (note == null) return;
            fireEvent(cell, new NoteCardEvents(type, note));
        }

        private final Note note;

        public NoteCardEvents(EventType<? extends Event> eventType, Note note) {
            super(eventType);
            this.note = note;
        }

        public Note getNote() {
            return note;
        }
    }
}
