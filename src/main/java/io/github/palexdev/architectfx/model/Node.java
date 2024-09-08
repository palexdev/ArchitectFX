package io.github.palexdev.architectfx.model;

import java.util.*;

// TODO think about another name
public class Node {
    //================================================================================
    // Properties
    //================================================================================
    private final String type;
    private final SequencedSet<Property> properties = new LinkedHashSet<>();
    private final List<Node> children = new ArrayList<>();

    //================================================================================
    // Constructors
    //================================================================================
    public Node(String type) {
        this.type = type;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================


    @Override
    public String toString() {
        return "Node{" +
            "type='" + type + '\'' +
            ", n.properties=" + properties.size() +
            ", n.children=" + children.size() +
            '}';
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getType() {
        return type;
    }

    public List<Node> getChildren() {
        return children;
    }

    public SequencedSet<Property> getProperties() {
        return properties;
    }
}
