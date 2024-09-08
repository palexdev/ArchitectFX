package io.github.palexdev.architectfx.model;

import java.util.HashSet;
import java.util.Set;

public class Document {
    //================================================================================
    // Properties
    //================================================================================
    private final Node root;
    private final String controller;
    private final Set<String> dependencies = new HashSet<>();
    private final Set<String> imports = new HashSet<>();

    //================================================================================
    // Constructors
    //================================================================================
    public Document(Node root, String controller) {
        this.root = root;
        this.controller = controller;
    }

    //================================================================================
    // Getters
    //================================================================================
    public Node getRoot() {
        return root;
    }

    public String getController() {
        return controller;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getImports() {
        return imports;
    }
}
