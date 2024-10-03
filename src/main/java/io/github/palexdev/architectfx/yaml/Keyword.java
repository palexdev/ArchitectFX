package io.github.palexdev.architectfx.yaml;

public record Keyword(String name) {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Keyword THIS = new Keyword("THIS");
}
