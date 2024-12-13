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

package io.github.palexdev.architectfx.backend.loaders.jui;


import java.io.IOException;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/// Concrete implementation of [JUIBaseLoader] to work with the JavaFX framework.\
/// Although it allows to work on a generic [Node], the parent in [#attachChildren(Node, List)] is expected to have
/// a children list field, otherwise ends with an [IOException].
public class JUIFXLoader extends JUIBaseLoader<Node> {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void attachChildren(Node parent, List<Node> children) throws IOException {
        if (parent instanceof Pane p) {
            p.getChildren().addAll(children);
            return;
        }

        if (parent instanceof Parent p) {
            List<Node> list = resolver.context().getReflector().get(p, "children");
            list.addAll(children);
            return;
        }

        throw new IOException("Cannot attach children to node of type: " + parent.getClass().getName());
    }
}
