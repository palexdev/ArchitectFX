/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
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

package io.github.palexdev.architectfx.backend.utils;

import io.github.palexdev.architectfx.backend.model.Entity;

/// A simple utility to print an [Entity] and all of its children recursively to the console,
/// useful for debugging.
public class TreePrinter {

    //================================================================================
    // Constructors
    //================================================================================
    private TreePrinter() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static void printEntity(Entity Entity, boolean printTypes) {
        printEntity(Entity, 0, printTypes);
    }

    private static void printEntity(Entity Entity, int indent, boolean printTypes) {
        // Print the current Entity with indentation
        printIndent(indent);
        System.out.println("Entity: " + Entity.type());

        // Print the properties of the Entity
        if (!Entity.properties().isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Properties:");
            Entity.properties().values().forEach(p -> {
                printIndent(indent + 2);
                if (printTypes) {
                    System.out.printf(p.name() + ": " + p.value() + " [%s]%n", p.type());
                } else {
                    System.out.println(p.name() + ": " + p.value());
                }
            });
        }

        // Recursively print children
        if (!Entity.children().isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Children:");
            for (Entity child : Entity.children()) {
                printEntity(child, indent + 2, printTypes);
            }
        }
    }

    private static void printIndent(int indent) {
        // Prints spaces for indentation
        System.out.print(" ".repeat(indent * 2));
    }
}