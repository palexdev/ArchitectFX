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


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import io.github.palexdev.architectfx.backend.jui.JUIParser.DocumentContext;
import io.github.palexdev.architectfx.backend.model.UIDocument;
import io.github.palexdev.architectfx.backend.model.UIObj;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/// Entry point fot parsing a _JUI_ document. This is responsible for parsing the dependencies, the imports, the controller
/// and the UI tree root (the tree is parsed recursively).
public class JUIVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final JUIVisitor INSTANCE = new JUIVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public UIDocument visit(DocumentContext ctx, URL location) throws IOException {
        try {
            return visit(ctx, location != null ? location.toURI() : null);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    protected UIDocument visit(DocumentContext ctx, URI location) {
        UIObj root = ObjVisitor.INSTANCE.visit(ctx.root);
        UIDocument document = new UIDocument(location, root);
        document.getDependencies().addAll(MetadataVisitor.INSTANCE.visit(ctx.dependencies()));
        document.getImports().addAll(MetadataVisitor.INSTANCE.visit(ctx.imports()));
        document.setController(ControllerVisitor.INSTANCE.visit(ctx.controller()));
        return document;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static String toFQN(List<TerminalNode> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;
        return tokens.stream()
            .map(ParseTree::getText)
            .collect(Collectors.joining("."));
    }
}
