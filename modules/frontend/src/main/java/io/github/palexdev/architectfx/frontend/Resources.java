package io.github.palexdev.architectfx.frontend;

import java.io.InputStream;
import java.net.URL;

public class Resources {

    //================================================================================
    // Constructors
    //================================================================================
    private Resources() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static URL loadURL(String path) {
        return Resources.class.getResource(path);
    }

    public static String load(String path) {
        return loadURL(path).toString();
    }

    public static InputStream loadStream(String name) {
        return Resources.class.getResourceAsStream(name);
    }
}
