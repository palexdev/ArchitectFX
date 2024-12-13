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


import java.util.Arrays;

import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.jui.JUIParser.ArrayContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.CollectionContext;
import io.github.palexdev.architectfx.backend.model.types.Value;
import io.github.palexdev.architectfx.backend.utils.Tuple2;
import org.antlr.v4.runtime.RecognitionException;

/// This visitor is responsible for parsing arrays and collections in _JUI_.
/// - Arrays are composed of a string which indicates the type of values in it, and the actual items which are parsed
/// by [TypesVisitor]
/// - Collections in _JUI_ are created by special keywords (such as listOf, mapOf,...). The keyword determines the
/// collection's type [CollectionType]. The items are parsed by [TypesVisitor]
public class CollectionsVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final CollectionsVisitor INSTANCE = new CollectionsVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public Tuple2<String, Value<?>[]> visit(ArrayContext ctx) {
        String type = JUIVisitor.toFQN(ctx.IDENTIFIER());
        Value<?>[] values = TypesVisitor.INSTANCE.visit(ctx.args());
        return Tuple2.of(type, values);
    }

    public Tuple2<CollectionType, Value<?>[]> visit(CollectionContext ctx) {
        CollectionType type = CollectionType.fromString(ctx.ctype.getText());
        if (type == null)
            throw new RecognitionException(
                "Expected type for collection. " + Arrays.toString(CollectionType.values()),
                null,
                null,
                ctx
            );
        Value<?>[] values = TypesVisitor.INSTANCE.visit(ctx.args());
        return Tuple2.of(type, values);
    }
}
