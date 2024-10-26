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

package io.github.palexdev.architectfx.backend.utils;

import java.lang.reflect.Array;
import java.util.List;

/// Utilities strictly related to Java's varargs feature.
public class VarArgsHandler {

    //================================================================================
    // Constructors
    //================================================================================
    private VarArgsHandler() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Turns out that handling varargs is quite hard, especially when you want to try invoking a method which needs
    /// standard parameters and varargs too. In reality, they are just syntactic sugar for arrays, but the issue still
    /// remains. In cases such as this, you want to build an array of args which inside has another array which contains
    /// the varargs.
    ///
    /// This utility method combines the objects in the given list into an array of "unknown" type. I say this because
    /// as of now the method determines the type by looking only at the first element. Then creates the array with
    /// [Array#newInstance(Class,int)] and copies the elements from the list in it with
    /// [System#arraycopy(Object,int,Object,int,int)].
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static Object generateArray(List<?> varargs) {
        if (varargs.isEmpty()) return null;
        // TODO For now, we only support uniform varargs
        Class<?> type = varargs.getFirst().getClass();
        Object vArr = Array.newInstance(type, varargs.size());
        System.arraycopy(varargs.toArray(), 0, vArr, 0, varargs.size());
        return vArr;
    }

    /// This method is intended as a support to [#generateArray(List)].
    ///
    /// This is very useful when you want to invoke a method with both standard parameters and varargs. This will simply
    /// build the args array which also contains the varargs. Java is going to be happy. You are going to be happy.
    /// Everyone is going to be happy :)
    public static Object[] combine(Object[] args, Object varargs) {
        if (varargs == null) return args;
        Object[] newArr = new Object[args.length + 1];
        System.arraycopy(args, 0, newArr, 0, args.length);
        newArr[args.length] = varargs;
        return newArr;
    }
}
