package io.github.palexdev.architectfx.yaml;

/// In simple terms: special Strings which map to Java keywords.
///
/// For example, suppose we want to invoke a method which needs the current parsing object as one of its inputs.
/// How can we distinguish between the simple String "this" and the Java keyword "this"?
///
/// This is what this class is for. We are basically reserving some special Strings, in the hope this does not limit the
/// user. When you use `Keyword.CONSTANT` in YAML, the system will automatically detect that we are dealing with a static
/// field of this class and handle the keyword appropriately.
public record Keyword(String name) {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Keyword THIS = new Keyword("THIS");
}
