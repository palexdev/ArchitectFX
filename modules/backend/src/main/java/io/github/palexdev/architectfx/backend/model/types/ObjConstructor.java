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

package io.github.palexdev.architectfx.backend.model.types;

import java.util.Arrays;

/// Merely a tagging interface. There are two ways to instantiate an object:
/// 1) Standard way, through a constructor with or without arguments
/// 2) With a builder/factory of some sorts, which is basically a chain of methods. The terminal method is expected
/// to return an object of the correct type. Can be expressed through [MethodsChain].
///
/// @see Simple
/// @see Factory
public interface ObjConstructor {

    //================================================================================
    // Inner Classes
    //================================================================================

    /// Specifies that an object should be constructed by invoking its constructor with a series of arguments expressed
    /// by an array of [Value] objects.
    class Simple implements ObjConstructor {
        private final Value<?>[] args;

        public Simple(Value<?>... args) {
            this.args = args;
        }

        @Override
        public String toString() {
            return "Constructor{" +
                   "args=" + Arrays.toString(args) +
                   '}';
        }

        public Value<?>[] args() {
            return args;
        }
    }

    /// Specifies that an object should be constructed by a factory/builder class. The creation process is done by a
    /// series of methods expressed through the [MethodsChain] class.
    class Factory implements ObjConstructor {
        private final MethodsChain methods;

        public Factory(MethodsChain methods) {
            this.methods = methods;
        }

        @Override
        public String toString() {
            return "Factory{" +
                   "methods=" + methods +
                   '}';
        }

        public MethodsChain methods() {
            return methods;
        }
    }
}
