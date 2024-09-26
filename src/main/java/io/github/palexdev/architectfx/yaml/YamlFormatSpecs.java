package io.github.palexdev.architectfx.yaml;

public class YamlFormatSpecs {
    //================================================================================
    // Static Properties
    //================================================================================

    // Tags
    public static final String CONTROLLER_TAG =   ".controller";
    public static final String DEPS_TAG =         ".deps";
    public static final String DEPENDENCIES_TAG = ".dependencies";
    public static final String IMPORTS_TAG =      ".imports";

    public static final String ARGS_TAG =         ".args";
    public static final String FACTORY_TAG =      ".factory";
    // TODO for now some tags are declared but unused because not strictly necessary. Maybe use for consistency!
    public static final String NAME_TAG =         ".name";
    public static final String STEPS_TAG =        ".steps";
    public static final String TRANSFORM_TAG =    ".transform";
    public static final String TYPE_TAG =         ".type";

    //================================================================================
    // Constructors
    //================================================================================
    private YamlFormatSpecs() {}
}

