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


import io.github.palexdev.architectfx.backend.enums.Keyword;
import io.github.palexdev.architectfx.backend.jui.JUIParser.KeywordsContext;
import io.github.palexdev.architectfx.backend.utils.Tuple2;
import org.antlr.v4.runtime.tree.TerminalNode;

/// This visitor is responsible for parsing special/reserved words.
///
/// Most of them can directly be converted to a constant in [Keyword], but some of them may bring a payload that is
/// needed during the load process,
public class KeywordsVisitor {
    //================================================================================
    // Default
    //================================================================================
    public static final KeywordsVisitor INSTANCE = new KeywordsVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public Tuple2<Keyword, Object[]> visit(KeywordsContext ctx) {
        TerminalNode injNode = ctx.INJECTION();
        if (injNode != null) {
            String injection = injNode.getText();
            String name = injection.substring(1, injection.length() - 1);
            String[] payload = new String[]{name};
            return Tuple2.of(Keyword.INJECTION, payload);
        }
        String toUpper = ctx.getText().toUpperCase();
        Keyword keyword = Keyword.valueOf(toUpper);
        return Tuple2.of(keyword, null);
    }
}
