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


import io.github.palexdev.architectfx.backend.jui.JUIParser.ControllerContext;
import io.github.palexdev.architectfx.backend.jui.JUIParser.UiObjContext;
import io.github.palexdev.architectfx.backend.model.UIObj;

/// This visitor is responsible for parsing the controller of a _JUI_ document to a [UIObj].
public class ControllerVisitor {
    //================================================================================
    // Singleton
    //================================================================================
    public static final ControllerVisitor INSTANCE = new ControllerVisitor();

    //================================================================================
    // Methods
    //================================================================================
    public UIObj visit(ControllerContext ctx) {
        UiObjContext uiObjCtx;
        return ((ctx == null) || ((uiObjCtx = ctx.uiObj()) == null)) ? null : ObjVisitor.INSTANCE.visit(uiObjCtx);
    }
}
