package io.github.palexdev.architectfx.frontend.utils.ui;

import io.github.palexdev.mfxcore.base.beans.Size;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class WindowUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private WindowUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static void clampWindowSizes(Size defaultValue) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        defaultValue.setWidth(Math.min(defaultValue.getWidth(), bounds.getWidth() - 50));
        defaultValue.setHeight(Math.min(defaultValue.getHeight(), bounds.getHeight() - 50));
    }
}
