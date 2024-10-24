package io.github.palexdev.architectfx.backend.utils;

import io.github.palexdev.architectfx.backend.enums.OSType;

public class OSUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    private static OSType osType;

    //================================================================================
    // Constructors
    //================================================================================
    private OSUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// @return whether [#os()] is not [OSType#Other]
    public static boolean isSupportedPlatform() {
        return os() != OSType.Other;
    }

    /// @return the OS on which the app is running represented by the enum [OSType]. The result is cached, subsequent
    /// calls will be faster.
    public static OSType os() {
        if (osType == null) {
            String name = System.getProperty("os.name");
            osType = switch (name) {
                case String s when s.contains("win") || s.contains("Win") -> OSType.Windows;
                case String s when s.contains("nix") || s.contains("nux") -> OSType.Linux;
                case String s when s.contains("mac") || s.contains("Mac") -> OSType.MacOS;
                default -> OSType.Other;
            };
        }
        return osType;
    }
}