/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/MaterialFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

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
            node.getProperties().values().forEach(p -> {
                printIndent(indent + 2);
                if (printTypes) {
                    System.out.printf(p.name() + ": " + p.value() + " [%s]%n", p.type());
                } else {
                    System.out.println(p.name() + ": " + p.value());
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