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


import java.util.ArrayList;
import java.util.List;

import io.github.palexdev.architectfx.backend.jui.JUIParser.FieldContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.MethodContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.MethodsChainContext;
import io.github.palexdev.architectfx.backend.model.types.FieldRef;
import io.github.palexdev.architectfx.backend.model.types.MethodCall;
import io.github.palexdev.architectfx.backend.model.types.MethodsChain;
import io.github.palexdev.architectfx.backend.model.types.Value;
import org.antlr.v4.runtime.tree.TerminalNode;

/// This visitor is responsible for parsing both field references and method calls in _JUI._
/// - Methods are resolved to [MethodsChain]. The arguments of each call in the chain are parsed by [TypesVisitor]
/// - Fields are resolved to [FieldRef]. The last identifier is expected to be the name of the field
public class MembersVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final MembersVisitor INSTANCE = new MembersVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public MethodsChain visit(MethodsChainContext ctx) {
        MethodsChain mc = new MethodsChain();
        if (ctx == null) return mc;

        // Detect whether it's a static or instance method
        String owner = JUIVisitor.toFQN(ctx.IDENTIFIER());

        // Parse calls
        List<MethodContext> chain = ctx.method();
        List<MethodCall> calls = new ArrayList<>();
        for (MethodContext mCtx : chain) {
            String name = mCtx.IDENTIFIER().getText();
            Value<?>[] args = TypesVisitor.INSTANCE.visit(mCtx.args());
            calls.add(new MethodCall(owner, name, args));
            owner = null; // Only first method needs to know about owner
        }
        mc.getMethods().addAll(calls);
        return mc;
    }

    public FieldRef visit(FieldContext ctx) {
        // Last ID is expected to be the field name!
        List<TerminalNode> ids = ctx.IDENTIFIER();
        String name = ids.removeLast().getText();
        String owner = JUIVisitor.toFQN(ids);
        return new FieldRef(owner, name);
    }
}
