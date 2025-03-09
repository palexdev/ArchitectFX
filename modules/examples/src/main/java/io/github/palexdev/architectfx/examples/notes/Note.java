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

import io.github.palexdev.mfxcore.utils.StringUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import javafx.beans.property.*;

public class Note {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final List<Note> NOTES = List.of(
        new Note(Priority.HIGH, "Important Task!", "This note's priority is high"),
        new Note(Priority.MEDIUM, "Check this out", "This note's priority is medium"),
        new Note(Priority.LOW, "Study", "I'll do that later...\nmaybe"),
        new Note(Priority.STANDARD, "Guess what...", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\nUt enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\nDuis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\nExcepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<Priority> priority = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty text = new SimpleStringProperty("");
    private final BooleanProperty favorite = new SimpleBooleanProperty(false);
    private final long timestamp;

    //================================================================================
    // Constructors
    //================================================================================
    public Note(Priority priority, String title, String text) {
        setPriority(priority);
        setTitle(title);
        setText(text);
        this.timestamp = Instant.now().toEpochMilli();
    }

    //================================================================================
    // Methods
    //================================================================================
    public String getDate() {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate date = instant.atZone(zoneId).toLocalDate();
        return DATE_FORMATTER.format(date);
    }

    public boolean contains(String query) {
        return StringUtils.containsIgnoreCase(getTitle(), query) ||
               StringUtils.containsIgnoreCase(getText(), query);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Priority getPriority() {
        return priority.get();
    }

    public ObjectProperty<Priority> priorityProperty() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority.set(priority);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public boolean isFavorite() {
        return favorite.get();
    }

    public BooleanProperty favoriteProperty() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite.set(favorite);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================


    @Override
    public String toString() {
        return "Note{" +
               "priority=" + priority +
               ", title=" + title +
               ", text=" + text +
               ", favorite=" + favorite +
               ", date=" + getDate() +
               '}';
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum Priority {
        HIGH, MEDIUM, LOW, STANDARD
    }
}
