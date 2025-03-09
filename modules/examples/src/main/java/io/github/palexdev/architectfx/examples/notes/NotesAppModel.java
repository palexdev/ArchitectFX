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

import io.github.palexdev.architectfx.examples.utils.RefineList;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotesAppModel {
    //================================================================================
    // Properties
    //================================================================================
    private final RefineList<Note> notes = new RefineList<>(
        FXCollections.observableArrayList(Note.NOTES)
    );

    //================================================================================
    // Methods
    //================================================================================
    public void createNote(Note.Priority priority) {
        Note note = new Note(
            priority,
            "New note",
            ""
        );
        notes.add(note);
    }

    public void removeNote(Note note) {
        notes.remove(note);
    }

    public void filterNotes(String query, boolean onlyFavorites) {
        Predicate<Note> byQuery = (query != null && !query.isEmpty())
            ? n -> n.contains(query)
            : n -> true;
        Predicate<Note> byFav = onlyFavorites
            ? Note::isFavorite
            : n -> true;
        notes.setPredicate(byQuery.and(byFav));
    }

    //================================================================================
    // Getters
    //================================================================================
    public ObservableList<Note> getNotesUnmodifiable() {
        return notes.getView();
    }

}
