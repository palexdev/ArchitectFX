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


import java.util.Optional;

import io.github.palexdev.architectfx.backend.enums.CollectionHandleStrategy;
import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.jui.JUIParser.PropertyContext;
import io.github.palexdev.architectfx.backend.model.CollectionProperty;
import io.github.palexdev.architectfx.backend.model.ObjProperty;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.model.types.Value;
import io.github.palexdev.architectfx.backend.utils.Tuple2;

/// This visitor is responsible for parsing [UIObj]'s properties in _JUI._
/// Currently there are two kinds of properties with slightly different handling:
/// 1) Standard properties resolve to [ObjProperty], a name and a value parsed by [TypesVisitor]
/// 2) Collection properties resolve to [CollectionProperty], a name, a value and a handle strategy [CollectionHandleStrategy]
/// which tells the system how to treat the collection initialization
public class PropertyVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final PropertyVisitor INSTANCE = new PropertyVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public ObjProperty visit(PropertyContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (isCollection(ctx)) {
            CollectionHandleStrategy strategy = CollectionHandleStrategy.fromString(assignSymbol(ctx));
            Tuple2<CollectionType, Value<?>[]> tuple = ctx.collection() != null ?
                CollectionsVisitor.INSTANCE.visit(ctx.collection()) :
                CollectionsVisitor.INSTANCE.visit(ctx.type().collection());
            Value.CollectionValue value = new Value.CollectionValue(tuple.b(), tuple.a());
            return new CollectionProperty(name, value, strategy);
        }
        Value<?> value = TypesVisitor.INSTANCE.visit(ctx.type());
        return new ObjProperty(name, value);
    }

    protected boolean isCollection(PropertyContext ctx) {
        return ctx.collection() != null || (ctx.type() != null && ctx.type().collection() != null);
    }

    protected String assignSymbol(PropertyContext ctx) {
        return Optional.ofNullable(ctx.COLON())
            .or(() -> Optional.ofNullable(ctx.EQUALS()))
            .orElse(ctx.PLUSEQUALS())
            .getText();
    }
}
