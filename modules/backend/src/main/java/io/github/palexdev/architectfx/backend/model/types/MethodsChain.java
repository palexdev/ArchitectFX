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


import java.util.ArrayList;
import java.util.List;

/// Represents a chain of methods, each should produce a result on which the next is invoked.
/// Example:
/// ```java
/// class User {
///   String firstName;
///   String lastName;
///
///   Builder {
///     User u = new User();
///
///     public static Builder build() {
///         return new Builder();
///     }
///
///     public Builder withFirstName(String fn) {
///         u.firstName = fn;
///         return this;
///     }
///
///     public Builder withLastName(String ln) {
///         u.lastName = ln;
///         return this;
///     }
///
///     public User get() {
///         return u;
///     }
///   }
/// }
///
/// /*
///  * A chain of methods could look like this:
///  * User.Builder.build()
///  *      .withFirstName(...)
///  *      .withLastName(...)
///  *      .get()
///  *
///  * As you can see, every method in the sequence produces a result. build(), withFirstName(), withLastName() return
///  * the builder instance on which the 'with' methods can be invoked to configure the user object.
///  * The last (build) is the terminal operation, which returns the overall result of the chain invocation
///  */
/// ```
///
/// @see MethodCall
public class MethodsChain {
    //================================================================================
    // Properties
    //================================================================================
    private final List<MethodCall> methods = new ArrayList<>();

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        return "MethodChain{" +
               "methods=" + methods +
               '}';
    }

    //================================================================================
    // Getters
    //================================================================================
    public List<MethodCall> getMethods() {
        return methods;
    }
}
