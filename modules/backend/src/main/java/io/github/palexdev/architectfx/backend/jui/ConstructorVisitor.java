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


import io.github.palexdev.architectfx.backend.jui.JUIParser.ConstructorContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.FactoryContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.UiObjContext;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.model.types.MethodsChain;
import io.github.palexdev.architectfx.backend.model.types.ObjConstructor;
import io.github.palexdev.architectfx.backend.model.types.Value;

/// This visitor is responsible for parsing a [UIObj]'s constructor starting from a [UiObjContext]. The constructor can
/// be any implementation of [ObjConstructor].
public class ConstructorVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final ConstructorVisitor INSTANCE = new ConstructorVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public ObjConstructor visit(UiObjContext ctx) {
        ConstructorContext constructor = ctx.constructor();
        if (constructor != null) {
            return visit(constructor);
        }

        FactoryContext factory = ctx.factory();
        if (factory != null) {
            return visit(factory);
        }

        return null;
    }

    protected ObjConstructor visit(ConstructorContext ctx) {
        Value<?>[] args = TypesVisitor.INSTANCE.visit(ctx.args());
        return new ObjConstructor.Simple(args);
    }

    private ObjConstructor visit(FactoryContext ctx) {
        MethodsChain chain = MembersVisitor.INSTANCE.visit(ctx.methodsChain());
        return new ObjConstructor.Factory(chain);
    }
}
