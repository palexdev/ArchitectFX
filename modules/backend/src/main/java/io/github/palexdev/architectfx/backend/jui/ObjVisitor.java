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


import io.github.palexdev.architectfx.backend.jui.JUIParser.MethodsChainContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.PropertyContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.UiObjContext;
import io.github.palexdev.architectfx.backend.model.ObjProperty;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.model.types.MethodsChain;
import io.github.palexdev.architectfx.backend.model.types.ObjConstructor;

/// This visitor is responsible for parsing a [UIObj] in _JUI._
///
/// When visiting a node we need to parse several pieces of information:
/// 1) The controller id to allow injection
/// 2) The constructor if the node needs arguments or the user wants to use a factory/builder
/// 3) Extra configuration expressed through method calls
/// 4) Properties
/// 5) Children, which are parsed recursively
///
/// _Note about children_\
/// To make the syntax as pleasant as possible for UIs definition, I made it so any node defined inside another one are
/// automatically considered to be children. Now, the language is so versatile that you could create a parent with one
/// or more children by using a constructor, a factory or any other way (depending on the context). Beware though that
/// while those children are going to be visualized in the UI, they won't be considered as such by the system's model,
/// for obvious reasons.
public class ObjVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final ObjVisitor INSTANCE = new ObjVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public UIObj visit(UiObjContext ctx) {
        String type = JUIVisitor.toFQN(ctx.IDENTIFIER());
        UIObj obj = new UIObj(type);

        // Controller ID
        JUIParser.CidContext cid = ctx.cid();
        if (cid != null)
            obj.setControllerId(cid.STRING().getText());

        // Constructor
        ObjConstructor constructor = ConstructorVisitor.INSTANCE.visit(ctx);
        obj.setConstructor(constructor);

        // Method calls
        for (MethodsChainContext mcCtx : ctx.methodsChain()) {
            MethodsChain chain = MembersVisitor.INSTANCE.visit(mcCtx);
            obj.getMethods().add(chain);
        }

        // Properties
        for (PropertyContext pCtx : ctx.property()) {
            ObjProperty property = PropertyVisitor.INSTANCE.visit(pCtx);
            obj.addProperty(property);
        }

        // Children
        for (UiObjContext childCtx : ctx.uiObj()) {
            UIObj cObj = visit(childCtx);
            obj.addChildren(cObj);
        }

        return obj;
    }
}
