package io.github.palexdev.architectfx.utils;

import io.github.palexdev.architectfx.model.Node;


public class TreePrinter {

    public static void printNode(Node node, boolean printTypes) {
        printNode(node, 0, printTypes);
    }

    private static void printNode(Node node, int indent, boolean printTypes) {
        // Print the current node with indentation
        printIndent(indent);
        System.out.println("Node: " + node.getType());

        // Print the properties of the node
        if (!node.getProperties().isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Properties:");
            node.getProperties().forEach(p -> {
                printIndent(indent + 2);
                if (printTypes) {
                    System.out.printf(p.getName() + ": " + p.getValue() + " [%s]%n", p.getValue().getClass().getSimpleName());
                } else {
                    System.out.println(p.getName() + ": " + p.getValue());
                }
            });
        }

        // Recursively print children
        if (!node.getChildren().isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Children:");
            for (Node child : node.getChildren()) {
                printNode(child, indent + 2, printTypes);
            }
        }
    }

    private static void printIndent(int indent) {
        // Prints spaces for indentation
        System.out.print(" ".repeat(indent * 2));
    }
}