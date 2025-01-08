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

package io.github.palexdev.architectfx.frontend.utils.ui;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.file.Paths;

import fr.brouillard.oss.cssfx.CSSFX;
import fr.brouillard.oss.cssfx.api.URIToPathConverter;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.mfxcomponents.window.MFXPlainContent;
import io.github.palexdev.mfxcomponents.window.popups.MFXTooltip;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.utils.fx.NodeUtils;
import io.github.palexdev.mfxcore.utils.fx.SwingFXUtils;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.util.Duration;

public class UIUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final URIToPathConverter CSSFX_URI_CONVERTER = uri -> {
        try {
            return Paths.get(URI.create(uri));
        } catch (Exception ignored) {}
        return null;
    };

    //================================================================================
    // Constructors
    //================================================================================
    private UIUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static Size getWindowSize(double minW, double minH, double prefW, double prefH) {
        Size clampedMin = clampWindowSizes(Size.of(minW, minH));
        Size clampedPref = clampWindowSizes(Size.of(prefW, prefH));
        return Size.of(
            Math.max(clampedMin.getWidth(), clampedPref.getWidth()),
            Math.max(clampedMin.getHeight(), clampedPref.getHeight())
        );
    }

    public static Size clampWindowSizes(Size size) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        return Size.of(
            Math.min(size.getWidth(), bounds.getWidth() - 50),
            Math.min(size.getHeight(), bounds.getHeight() - 50)
        );
    }

    public static MFXTooltip installTooltip(Node owner, String text) {
        return installTooltip(owner, text, Pos.BOTTOM_CENTER);
    }

    public static MFXTooltip installTooltip(Node owner, String text, Pos anchor) {
        MFXTooltip tooltip = new MFXTooltip(owner);
        tooltip.setContent(new MFXPlainContent(text));
        tooltip.setInDelay(M3Motion.LONG2);
        tooltip.setOutDelay(Duration.ZERO);
        tooltip.setAnchor(anchor);
        return tooltip.install();
    }

    public static WritableImage snapshot(Node node, double w, double h, SnapshotParameters parameters) {
        Screen screen = NodeUtils.getScreenFor(node);
        if (screen.getOutputScaleX() != 1.0) {
            double scale = screen.getOutputScaleX();
            int scaledW = (int) (w * scale);
            int scaledH = (int) (h * scale);
            WritableImage snapshot = new WritableImage(scaledW, scaledH);
            parameters.setTransform(Transform.scale(scale, scale));
            node.snapshot(parameters, snapshot);
            return snapshot;
        }
        return node.snapshot(parameters, null);
    }

    public static Image transform(Image src, io.github.palexdev.imcache.transforms.Transform... transforms) {
        if (transforms.length == 0) return src;

        BufferedImage out = SwingFXUtils.fromFXImage(src, null);
        for (io.github.palexdev.imcache.transforms.Transform transform : transforms) {
            out = transform.transform(out);
        }
        return SwingFXUtils.toFXImage(out, null);
    }

    public static Animation delayAction(Duration delay, Runnable action) {
        PauseTransition animation = PauseBuilder.build()
            .setDuration(delay)
            .setOnFinished(e -> action.run())
            .getAnimation();
        animation.play();
        return animation;
    }

    public static void debugTheme(Parent parent, String theme) {
        CSSFX.start(parent);
        parent.getStylesheets().add(Resources.load(theme));
    }
}
