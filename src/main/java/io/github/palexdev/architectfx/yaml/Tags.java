//@formatter:off
package io.github.palexdev.architectfx.yaml;

/// This class stores all the system's metadata tags.
///
/// These special Strings are crucial, they give valuable information to the system such as
/// what type of data we are dealing with, markers for particular properties, extra steps to run...
///
/// Not only these are a way to standardize the format, but also offer a good improvement in performance
/// (since tags tell the system what and how to do certain things, it doesn't need to figure it out by itself.
/// That would lead to inconsistencies, unmaintainable code and probably bad performance).
public class Tags {
    //================================================================================
    // Static Properties
    //================================================================================

    // Document-specific tags
    public static final String CONTROLLER_TAG =   ".controller";
    public static final String DEPS_TAG =         ".deps";
    public static final String IMPORTS_TAG =      ".imports";

    // Metadata tags
    public static final String ARGS_TAG =         ".args";
    public static final String CONTROLLER_ID =    ".cid";
    public static final String CONFIG_TAG =       ".config";
    public static final String FACTORY_TAG =      ".factory";
    public static final String FIELD_TAG =        ".field";
    public static final String METHOD_TAG =       ".method";
    public static final String TRANSFORM_TAG =    ".transform";
    public static final String TYPE_TAG =         ".type";
    public static final String VALUE_TAG =        ".val";
    public static final String VARARGS_TAG =      ".varargs";

    //================================================================================
    // Constructors
    //================================================================================
    private Tags() {}
}
