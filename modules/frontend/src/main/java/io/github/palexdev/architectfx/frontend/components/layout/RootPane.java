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

package io.github.palexdev.architectfx.frontend.components.layout;


import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.architectfx.frontend.components.Scrimmable;
import io.github.palexdev.architectfx.frontend.components.layout.Box.Direction;
import io.github.palexdev.architectfx.frontend.theming.ThemeMode;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.transforms.Resize;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcomponents.window.popups.MFXTooltip;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxcore.utils.fx.StageUtils;
import io.github.palexdev.mfxcore.utils.resize.StageResizer;
import io.github.palexdev.mfxeffects.animations.ConsumerTransition;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.rectcut.Rect;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.tinylog.Logger;

public class RootPane extends Region implements Scrimmable {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage stage;
    private final ImageView logoView;
    private final Box header;

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>() {
        @Override
        public void set(Node newValue) {
            Node oldValue = get();
            onContentChanged(oldValue, newValue);
            super.set(newValue);
        }
    };
    private final ObjectProperty<ThemeMode> themeMode = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            PseudoClasses.setOn(RootPane.this, "dark", get() == ThemeMode.DARK);
            String asset = (get() == ThemeMode.LIGHT) ?
                "assets/logo-on-light.png" :
                "assets/logo-on-dark.png";
            ImCache.instance()
                .request(Resources.loadURL(asset))
                .transform(new Resize(64.0, 64.0))
                .onStateChanged(res -> {
                    switch (res.state()) {
                        case CACHE_HIT, SUCCEEDED -> {
                            ImImage out = res.unwrapOut();
                            Image img = new Image(out.asStream());
                            Platform.runLater(() -> logoView.setImage(img));
                        }
                        case FAILED -> Logger.error("Failed to load logo image because:\n{}", res.unwrapError());
                    }
                })
                .executeAsync();

            if (getScene() != null) {
                onThemeChanged();
            }
        }
    };

    private ImageView transitionView;

    private Size prevWindowSize = Size.empty();

    /*
     * This is used as a workaround for this glorious piece of shit that is JavaFX.
     * I tried literally EVERYTHING to make a window always have the minimum size specified by the root pane: bindings,
     * direct set and delayed set (with a PauseTransition) NOTHING would work, I repeat, NOTHING.
     * Those guys are embarrassing, developers my ass. The only good thing of JavaFX is that you can render things to the
     * screen very easily, nothing more. Everything, from its foundations to the controls, to the styling is pure utter
     * garbage. Bless third-party libraries.
     *
     * Anyway, the solution is to execute an action on every layout that checks if the window's sizes are at least the
     * sizes specified by minWidth(-1) and minHeight(-1), and if not we programmatically size the stage.
     * The action is set in the ViewManager.
     */
    private Runnable onLayout;

    //================================================================================
    // Constructors
    //================================================================================
    public RootPane(Stage stage) {
        this.stage = stage;

        // Header
        logoView = new ImageView();
        Label title = new Label("ArchitectFX", logoView);
        title.getStyleClass().add("title");

        MFXFontIcon closeIcon = new MFXFontIcon("fas-circle");
        closeIcon.getStyleClass().add("close-icon");
        MFXTooltip et = UIUtils.installTooltip(closeIcon, "Exit", Pos.BOTTOM_CENTER);
        WhenEvent.intercept(closeIcon, MouseEvent.MOUSE_CLICKED)
            .process(e -> {
                et.close();
                stage.hide();
            })
            .asFilter()
            .register();

        MFXFontIcon maximizeIcon = new MFXFontIcon("fas-circle");
        maximizeIcon.getStyleClass().add("maximize-icon");
        UIUtils.installTooltip(maximizeIcon, "Maximize", Pos.BOTTOM_CENTER);
        WhenEvent.intercept(maximizeIcon, MouseEvent.MOUSE_CLICKED)
            .process(e -> {
                if (!stage.isMaximized()) {
                    prevWindowSize = Size.of(
                        getWidth(),
                        getHeight()
                    );
                    stage.setMaximized(true);
                } else {
                    stage.setMaximized(false);
                    stage.setWidth(prevWindowSize.getWidth());
                    stage.setHeight(prevWindowSize.getHeight());
                    stage.centerOnScreen();
                }
            })
            .register();

        MFXFontIcon minimizeIcon = new MFXFontIcon("fas-circle");
        minimizeIcon.getStyleClass().add("minimize-icon");
        UIUtils.installTooltip(minimizeIcon, "Minimize", Pos.BOTTOM_CENTER);
        WhenEvent.intercept(minimizeIcon, MouseEvent.MOUSE_CLICKED)
            .process(e -> stage.setIconified(true))
            .register();

        MFXFontIcon aotIcon = new MFXFontIcon("fas-circle");
        aotIcon.getStyleClass().add("aot-icon");
        UIUtils.installTooltip(aotIcon, "Always on Top", Pos.BOTTOM_CENTER);
        WhenEvent.intercept(aotIcon, MouseEvent.MOUSE_CLICKED)
            .process(e -> stage.setAlwaysOnTop(!stage.isAlwaysOnTop()))
            .register();
        When.onInvalidated(stage.alwaysOnTopProperty())
            .then(v -> PseudoClasses.setOn(aotIcon, "aot", v))
            .executeNow()
            .listen();

        header = new Box(
            Direction.ROW,
            title,
            Box.separator(),
            aotIcon, minimizeIcon, maximizeIcon, closeIcon
        ).addStyleClass("header");
        StageUtils.makeDraggable(stage, header.getContainerChildren().get(1)); // Install on the separator
        StageResizer resizer = new StageResizer(this, stage);
        resizer.setMinWidthFunction(r -> r.minWidth(-1));
        resizer.setMinHeightFunction(r -> r.minHeight(-1));
        resizer.makeResizable();

        getStyleClass().add("app-root");
        getChildren().add(header);
    }

    //================================================================================
    // Methods
    //================================================================================

    public void onLayout(Runnable action) {
        this.onLayout = action;
    }

    protected void onContentChanged(Node oldValue, Node newValue) {
        if (oldValue != null) getChildren().remove(oldValue);
        if (newValue != null) getChildren().add(newValue);
    }

    protected void onThemeChanged() {
        // Disable window temporarily
        setMouseTransparent(true);

        // Snapshot window
        double w = getWidth();
        double h = getHeight();
        WritableImage snap = UIUtils.snapshot(
            this,
            w, h,
            new SnapshotParameters()
        );
        transitionView = new ImageView(snap);
        transitionView.setSmooth(false);
        transitionView.setFitWidth(w);
        transitionView.setFitHeight(h);
        transitionView.setPreserveRatio(false);
        transitionView.setOpacity(1.0);
        transitionView.setManaged(false);

        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(24.0);
        clip.setArcHeight(24.0);
        transitionView.setClip(clip);

        getChildren().add(transitionView);

        // Fade out and remove
        ConsumerTransition.of(
            frac -> transitionView.setOpacity(1.0 - (1.0 * frac)),
            M3Motion.EXTRA_LONG4,
            M3Motion.STANDARD
        ).setOnFinishedFluent(f -> {
            getChildren().remove(transitionView);
            setMouseTransparent(false);
        }).play();
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void addScrim(Rectangle scrim) {
        scrim.setViewOrder(-1);
        getChildren().add(scrim);
    }

    @Override
    public void removeScrim(Rectangle scrim) {
        getChildren().remove(scrim);
    }

    @Override
    protected double computeMinWidth(double height) {
        Node content = getContent();
        double cW = content != null ? LayoutUtils.boundWidth(content) : 0;
        double hW = LayoutUtils.boundWidth(header);
        return snappedLeftInset() + snapSizeX(Math.max(cW, hW)) + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        Node content = getContent();
        double cH = content != null ? LayoutUtils.boundHeight(content) : 0;
        double hH = LayoutUtils.boundHeight(header);
        return snappedTopInset() + snapSizeY(cH + hH) + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
        if (transitionView != null && transitionView.getImage() != null) {
            transitionView.autosize();
            transitionView.relocate(0, 0);
        }

        // Layout within bounds
        double w = Math.max(computeMinWidth(-1), getWidth());
        double h = Math.max(computeMinHeight(-1), getHeight());
        Rect area = Rect.of(0, 0, w, h)
            .withInsets(new double[] {
                snappedTopInset(),
                snappedRightInset(),
                snappedBottomInset(),
                snappedLeftInset(),
            });

        area.cutTop(LayoutUtils.snappedBoundHeight(header))
            .layout(header::resizeRelocate);

        Node content = getContent();
        if (content != null) {
            area.layout(content::resizeRelocate);
        }

        if (onLayout != null)
            onLayout.run();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Stage getWindow() {
        return stage;
    }

    public ThemeMode getThemeMode() {
        return themeMode.get();
    }

    public ReadOnlyObjectProperty<ThemeMode> themeModeProperty() {
        return themeMode;
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }
}
