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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.palexdev.architectfx.frontend.components.FileInput.FileInputBehavior;
import io.github.palexdev.mfxcomponents.controls.base.MFXControl;
import io.github.palexdev.mfxcomponents.controls.base.MFXSkinBase;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXButton;
import io.github.palexdev.mfxcore.behavior.BehaviorBase;
import io.github.palexdev.mfxcore.controls.Text;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;

public class FileInput extends MFXControl<FileInputBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final Consumer<File> onLoadRequested;
    private Path lastDir;

    //================================================================================
    // Constructors
    //================================================================================
    public FileInput(Consumer<File> onLoadRequested, String lastDir) {
        this.onLoadRequested = onLoadRequested;
        Path toPath = Path.of(lastDir);
        if (!lastDir.isBlank() && Files.isDirectory(toPath)) {
            this.lastDir = toPath;
        }

        setDefaultBehaviorProvider();
        getStyleClass().setAll(defaultStyleClasses());
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void loadFile(File file) {
        if (Files.exists(file.toPath())) {
            onLoadRequested.accept(file);
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected MFXSkinBase<?, ?> buildSkin() {
        return new FileInputSkin(this);
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("file-input");
    }

    @Override
    public Supplier<FileInputBehavior> defaultBehaviorProvider() {
        return () -> new FileInputBehavior(this);
    }

    //================================================================================
    // Getters
    //================================================================================
    public Path getLastDir() {
        return lastDir;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public static class FileInputBehavior extends BehaviorBase<FileInput> {
        private static final PseudoClass DND_PSEUDO_CLASS = PseudoClass.getPseudoClass("dnd");

        public FileInputBehavior(FileInput node) {
            super(node);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            getNode().requestFocus();
        }

        public void dragOver(DragEvent de) {
            FileInput node = getNode();
            Dragboard db = de.getDragboard();
            if (de.getGestureSource() != node &&
                db.hasFiles() &&
                db.getFiles().size() == 1 &&
                !Files.isDirectory(db.getFiles().getFirst().toPath())
                //"jdsl".equals(FileUtils.getExtension(db.getFiles().getFirst()))
            ) {
                node.pseudoClassStateChanged(DND_PSEUDO_CLASS, true);
                de.acceptTransferModes(TransferMode.COPY);
            }
            de.consume();
        }

        public void dragDropped(DragEvent de) {
            FileInput node = getNode();
            Dragboard db = de.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();
                node.loadFile(file);
            }
            de.setDropCompleted(true);
            de.consume();
        }

        public void chooseFile(ActionEvent ae) {
            FileInput input = getNode();
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose a JDSL file");
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JDSL files", "*.jdsl");
            fc.setSelectedExtensionFilter(filter);
            if (input.lastDir != null && Files.isDirectory(input.lastDir)) {
                fc.setInitialDirectory(input.lastDir.toFile());
            }

            Window parent = Optional.ofNullable(input.getScene())
                .map(Scene::getWindow)
                .orElse(null);
            if (parent == null)
                Logger.error("Cannot browse for files because parent window could not be found");
            File file = fc.showOpenDialog(parent);
            if (file != null) {
                input.loadFile(file);
                input.lastDir = file.getParentFile().toPath();
            }
        }

        public void dragExited(DragEvent de) {
            getNode().pseudoClassStateChanged(DND_PSEUDO_CLASS, false);
        }
    }

    protected static class FileInputSkin extends MFXSkinBase<FileInput, FileInputBehavior> {
        private final MFXFontIcon icon;
        private final Text header;
        private final Text orText;
        private final MFXButton browse;

        private static final double ICON_SPACING = 20.0;
        private static final double TEXT_SPACING = 15.0;

        public FileInputSkin(FileInput input) {
            super(input);

            // Init Nodes
            icon = new MFXFontIcon();
            header = new Text("Drag & Drop files here");
            header.getStyleClass().add("header");
            orText = new Text("- OR -");
            orText.getStyleClass().add("or");
            browse = new MFXButton("Browse Files").outlined();

            getChildren().addAll(icon, header, orText, browse);
        }

        @Override
        protected void initBehavior(FileInputBehavior behavior) {
            super.initBehavior(behavior);
            FileInput input = getSkinnable();
            events(
                intercept(input, MouseEvent.MOUSE_PRESSED)
                    .process(behavior::mousePressed),
                intercept(input, DragEvent.DRAG_OVER)
                    .process(behavior::dragOver),
                intercept(input, DragEvent.DRAG_DROPPED)
                    .process(behavior::dragDropped),
                intercept(input, DragEvent.DRAG_EXITED)
                    .process(behavior::dragExited),
                intercept(browse, ActionEvent.ACTION)
                    .process(behavior::chooseFile)
            );
        }

        @Override
        public double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            return snapSizeY(
                LayoutUtils.boundHeight(icon) + ICON_SPACING +
                LayoutUtils.boundHeight(header) + TEXT_SPACING +
                LayoutUtils.boundHeight(orText) + TEXT_SPACING +
                LayoutUtils.boundHeight(browse) +
                topInset + bottomInset
            );
        }

        @Override
        public double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return snapSizeX(
                Stream.of(icon, header, orText, browse)
                    .mapToDouble(LayoutUtils::boundWidth)
                    .max()
                    .orElse(super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset)) +
                leftInset + rightInset
            );
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            double iH = icon.getLayoutBounds().getHeight();
            double hH = header.getLayoutBounds().getHeight();
            double oH = orText.getLayoutBounds().getHeight();
            double bH = browse.getHeight();
            double totalH = iH + hH + oH + bH - snappedTopInset() - snappedBottomInset();
            double area = totalH + ICON_SPACING + TEXT_SPACING * 2; // Include spacings

            double pos = snapPositionY((h - area) / 2.0);
            layoutInArea(
                icon,
                x, pos, w, h, 0,
                HPos.CENTER, VPos.TOP
            );

            pos = snapPositionY(pos + iH + ICON_SPACING);
            layoutInArea(
                header,
                x, pos, w, h, 0,
                HPos.CENTER, VPos.TOP
            );

            pos = snapPositionY(pos + oH + TEXT_SPACING);
            layoutInArea(
                orText,
                x, pos, w, h, 0,
                HPos.CENTER, VPos.TOP
            );

            pos = snapPositionY(pos + oH + TEXT_SPACING);
            layoutInArea(
                browse,
                x, pos, w, h, 0,
                HPos.CENTER, VPos.TOP
            );
        }
    }
}
