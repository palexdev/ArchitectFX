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

package io.github.palexdev.architectfx.backend.jui;


import java.util.Collection;
import java.util.List;

import io.github.palexdev.architectfx.backend.jui.JUIParser.DependenciesContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.ImportsContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/// This visitor is responsible for parsing metadata information in a _JUI_ document.
public class MetadataVisitor {
    //================================================================================
    // Singleton
    //================================================================================
    public static final MetadataVisitor INSTANCE = new MetadataVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public Collection<String> visit(DependenciesContext ctx) {
        return (ctx == null) ? List.of() : fromStrings(ctx.STRING());
    }

    public Collection<String> visit(ImportsContext ctx) {
        return (ctx == null) ? List.of() : fromStrings(ctx.STRING());
    }

    private Collection<String> fromStrings(List<TerminalNode> nodes) {
        return nodes.stream()
            .map(ParseTree::getText)
            .map(s -> s.replaceAll("^['\"]|['\"]$", ""))
            .toList();
    }
}
