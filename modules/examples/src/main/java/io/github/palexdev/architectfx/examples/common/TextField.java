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

package io.github.palexdev.architectfx.examples.common;


import io.github.palexdev.mfxcore.base.properties.NodeProperty;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class TextField extends HBox {
    //================================================================================
    // Properties
    //================================================================================
    private final javafx.scene.control.TextField field;

    private final NodeProperty leadingGraphic = new NodeProperty() {
        @Override
        public void set(Node newValue) {
            Node oldValue = get();
            if (oldValue != null) getChildren().remove(oldValue);
            if (newValue != null) {
                newValue.getStyleClass().add("leading");
                getChildren().addFirst(newValue);
            }
            super.set(newValue);
        }
    };
    private final NodeProperty trailingGraphic = new NodeProperty() {
        @Override
        public void set(Node newValue) {
            Node oldValue = get();
            if (oldValue != null) getChildren().remove(oldValue);
            if (newValue != null) {
                newValue.getStyleClass().add("trailing");
                getChildren().addLast(newValue);
            }
            super.set(newValue);
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public TextField() {
        this("");
    }

    public TextField(String text) {
        this(text, null, null);
    }

    public TextField(String text, MFXFontIcon leading, MFXFontIcon trailing) {
        field = new javafx.scene.control.TextField(text);
        setHgrow(field, Priority.ALWAYS);
        getChildren().add(field);
        setLeadingGraphic(leading);
        setTrailingGraphic(trailing);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().setAll("field-box");

        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> field.requestFocus());
    }

    //================================================================================
    // Delegate Methods
    //================================================================================
    public CharSequence getCharacters() {
        return field.getCharacters();
    }

    public boolean isUndoable() {
        return field.isUndoable();
    }

    public void selectPreviousWord() {
        field.selectPreviousWord();
    }

    public void replaceSelection(String replacement) {
        field.replaceSelection(replacement);
    }

    public void commitValue() {
        field.commitValue();
    }

    public boolean deletePreviousChar() {
        return field.deletePreviousChar();
    }

    public boolean isEditable() {
        return field.isEditable();
    }

    public void setPrefColumnCount(int value) {
        field.setPrefColumnCount(value);
    }

    public ObjectProperty<Font> fontProperty() {
        return field.fontProperty();
    }

    public boolean isRedoable() {
        return field.isRedoable();
    }

    public void selectEndOfNextWord() {
        field.selectEndOfNextWord();
    }

    public void forward() {
        field.forward();
    }

    public void positionCaret(int pos) {
        field.positionCaret(pos);
    }

    public BooleanProperty editableProperty() {
        return field.editableProperty();
    }

    public String getText(int start, int end) {
        return field.getText(start, end);
    }

    public void redo() {
        field.redo();
    }

    public EventHandler<ActionEvent> getOnAction() {
        return field.getOnAction();
    }

    public ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return field.selectionProperty();
    }

    public String getSelectedText() {
        return field.getSelectedText();
    }

    public void paste() {
        field.paste();
    }

    public ObjectProperty<TextFormatter<?>> textFormatterProperty() {
        return field.textFormatterProperty();
    }

    public int getAnchor() {
        return field.getAnchor();
    }

    public void selectForward() {
        field.selectForward();
    }

    public void deleteText(IndexRange range) {
        field.deleteText(range);
    }

    public void home() {
        field.home();
    }

    public void setText(String value) {
        field.setText(value);
    }

    public String getPromptText() {
        return field.getPromptText();
    }

    public void selectHome() {
        field.selectHome();
    }

    public int getCaretPosition() {
        return field.getCaretPosition();
    }

    public void nextWord() {
        field.nextWord();
    }

    public void replaceText(IndexRange range, String text) {
        field.replaceText(range, text);
    }

    public IntegerProperty prefColumnCountProperty() {
        return field.prefColumnCountProperty();
    }

    public Font getFont() {
        return field.getFont();
    }

    public int getLength() {
        return field.getLength();
    }

    public void clear() {
        field.clear();
    }

    public boolean deleteNextChar() {
        return field.deleteNextChar();
    }

    public ReadOnlyIntegerProperty lengthProperty() {
        return field.lengthProperty();
    }

    public void deselect() {
        field.deselect();
    }

    public void setFont(Font value) {
        field.setFont(value);
    }

    public void replaceText(int start, int end, String text) {
        field.replaceText(start, end, text);
    }

    public int getPrefColumnCount() {
        return field.getPrefColumnCount();
    }

    public ReadOnlyBooleanProperty undoableProperty() {
        return field.undoableProperty();
    }

    public void undo() {
        field.undo();
    }

    public void selectNextWord() {
        field.selectNextWord();
    }

    public void backward() {
        field.backward();
    }

    public void cancelEdit() {
        field.cancelEdit();
    }

    public void setEditable(boolean value) {
        field.setEditable(value);
    }

    public ReadOnlyBooleanProperty redoableProperty() {
        return field.redoableProperty();
    }

    public String getText() {
        return field.getText();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return field.onActionProperty();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        field.setOnAction(value);
    }

    public IndexRange getSelection() {
        return field.getSelection();
    }

    public void cut() {
        field.cut();
    }

    public void setTextFormatter(TextFormatter<?> value) {
        field.setTextFormatter(value);
    }

    public void appendText(String text) {
        field.appendText(text);
    }

    public void selectPositionCaret(int pos) {
        field.selectPositionCaret(pos);
    }

    public void copy() {
        field.copy();
    }

    public TextFormatter<?> getTextFormatter() {
        return field.getTextFormatter();
    }

    public void insertText(int index, String text) {
        field.insertText(index, text);
    }

    public void selectAll() {
        field.selectAll();
    }

    public void selectRange(int anchor, int caretPosition) {
        field.selectRange(anchor, caretPosition);
    }

    public ReadOnlyStringProperty selectedTextProperty() {
        return field.selectedTextProperty();
    }

    public void selectBackward() {
        field.selectBackward();
    }

    public void setPromptText(String value) {
        field.setPromptText(value);
    }

    public void extendSelection(int pos) {
        field.extendSelection(pos);
    }

    public ReadOnlyIntegerProperty anchorProperty() {
        return field.anchorProperty();
    }

    public void end() {
        field.end();
    }

    public void deleteText(int start, int end) {
        field.deleteText(start, end);
    }

    public void previousWord() {
        field.previousWord();
    }

    public void selectEnd() {
        field.selectEnd();
    }

    public StringProperty promptTextProperty() {
        return field.promptTextProperty();
    }

    public StringProperty textProperty() {
        return field.textProperty();
    }

    public ReadOnlyIntegerProperty caretPositionProperty() {
        return field.caretPositionProperty();
    }

    public void endOfNextWord() {
        field.endOfNextWord();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Node getLeadingGraphic() {
        return leadingGraphic.get();
    }

    public NodeProperty leadingGraphicProperty() {
        return leadingGraphic;
    }

    public void setLeadingGraphic(Node node) {
        leadingGraphic.set(node);
    }

    public Node getTrailingGraphic() {
        return trailingGraphic.get();
    }

    public NodeProperty trailingGraphicProperty() {
        return trailingGraphic;
    }

    public void setTrailingGraphic(Node node) {
        trailingGraphic.set(node);
    }
}
