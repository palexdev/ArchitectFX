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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.enums.Keyword;
import io.github.palexdev.architectfx.backend.jui.JUIParser.*;
import io.github.palexdev.architectfx.backend.model.types.Value;
import io.github.palexdev.architectfx.backend.model.types.Value.*;
import io.github.palexdev.architectfx.backend.utils.CastUtils;
import io.github.palexdev.architectfx.backend.utils.Tuple2;
import org.antlr.v4.runtime.ParserRuleContext;

/// This visitor is responsible for parsing any type of value specified by the `type` rule into the appropriate model object.
/// Types can be grouped into two major categories:
/// 1) Special types: nodes, keywords, fields, methods, arrays and collections. Typically, the parsing in this category
/// is delegated to a specific visitor
/// 2) Basic types: bool, char, string, numbers and urls
///
/// Arrays of values are also resolved by this visitor by iterating on each item. Items for arrays and collections, and
/// arguments for calls and such, are grouped under the same name. They all are considered as arguments.
public class TypesVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final TypesVisitor INSTANCE = new TypesVisitor();

    //================================================================================
    // Properties
    //================================================================================
    private static final Map<Integer, Function<String, Value<?>>> basicTypeHandlers = new HashMap<>() {
        {
            put(JUIParser.URL, INSTANCE::parseURL);
            put(JUIParser.BOOLEAN, s -> new Value.BooleanValue(Boolean.parseBoolean(s)));
            put(JUIParser.CHAR, s -> new Value.CharValue(s.charAt(1)));
            put(JUIParser.STRING, s -> new Value.StringValue(s.replaceAll("^['\"]|['\"]$", "")));
            put(JUIParser.INTEGER, s -> new Value.NumberValue(Integer.parseInt(s)));
            put(JUIParser.HEXADECIMAL, s -> new Value.NumberValue(Integer.parseInt(s.substring(2), 16)));
            put(JUIParser.BINARY, s -> new Value.NumberValue(Integer.parseInt(s.substring(2), 2)));
            put(JUIParser.OCTAL, s -> new Value.NumberValue(Integer.parseInt(s.substring(1), 8)));
            put(JUIParser.FLOAT, s -> new Value.NumberValue(Float.parseFloat(s)));
            put(JUIParser.DOUBLE, s -> new Value.NumberValue(Double.parseDouble(s)));
            put(JUIParser.INFINITY, s -> new Value.NumberValue(Double.parseDouble(s)));
            put(JUIParser.NAN, s -> new Value.NumberValue(Double.parseDouble(s)));
        }
    };

    //================================================================================
    // Methods
    //================================================================================
    public Value<?> visit(TypeContext ctx) {
        // Special types (wrote this way to slightly improve performance)
        ParserRuleContext rule;
        rule = ctx.uiObj();
        if (rule != null) return new UIObjValue(ObjVisitor.INSTANCE.visit(
            CastUtils.as(rule, JUIParser.UiObjContext.class))
        );

        rule = ctx.keywords();
        if (rule != null) {
            Tuple2<Keyword, Object[]> tuple = KeywordsVisitor.INSTANCE.visit(
                CastUtils.as(rule, KeywordsContext.class)
            );

            return new KeywordValue(tuple.a(), tuple.b());
        }

        rule = ctx.field();
        if (rule != null) return new FieldValue(MembersVisitor.INSTANCE.visit(
            CastUtils.as(rule, FieldContext.class)
        ));

        rule = ctx.methodsChain();
        if (rule != null) return new MethodsValue(MembersVisitor.INSTANCE.visit(
            CastUtils.as(rule, MethodsChainContext.class)
        ));

        rule = ctx.array();
        if (rule != null) {
            Tuple2<String, Value<?>[]> tuple = CollectionsVisitor.INSTANCE.visit(
                CastUtils.as(rule, ArrayContext.class)
            );
            return new ArrayValue(tuple.b(), tuple.a());
        }

        rule = ctx.collection();
        if (rule != null) {
            Tuple2<CollectionType, Value<?>[]> tuple = CollectionsVisitor.INSTANCE.visit(
                CastUtils.as(rule, CollectionContext.class)
            );
            return new CollectionValue(tuple.b(), tuple.a());
        }

        // Base/standard types
        int type = ctx.getStart().getType();
        Function<String, Value<?>> fn = basicTypeHandlers.get(type);
        if (fn == null) throw new IllegalArgumentException("Unsupported type: " + ctx.getText());
        return fn.apply(ctx.getText());
    }

    public Value<?>[] visit(ArgsContext ctx) {
        if (ctx == null) return new Value[0];
        List<TypeContext> types = ctx.type();
        Value<?>[] values = new Value[types.size()];
        for (int i = 0; i < types.size(); i++) {
            values[i] = visit(types.get(i));
        }
        return values;
    }

    private Value<?> parseURL(String url) {
        String sub = url.substring(5, url.length() - 2);
        return new URLValue(sub);
    }
}
