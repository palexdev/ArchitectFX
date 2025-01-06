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

package io.github.palexdev.architectfx.backend.resolver;


import java.net.URI;
import java.util.*;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.model.UIDocument;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.model.types.FieldRef;
import io.github.palexdev.architectfx.backend.model.types.MethodsChain;
import io.github.palexdev.architectfx.backend.model.types.Value;
import io.github.palexdev.architectfx.backend.model.types.Value.*;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner;

/// Core API which is the bridge between the model parsed from the document and the actual UI graph. In other words, the
/// main purpose of a _resolver_ is to convert [Value] into the appropriate object, thus creating and initializing the tree.
///
/// @see DefaultResolver
public interface Resolver {

    <T> T resolveObj(UIObj obj);

    default <T> T resolveObj(UIObjValue value) {
        return resolveObj(value.getValue());
    }

    <T> T resolveKeyword(KeywordValue value);

    <T> T resolveField(FieldRef ref);

    default <T> T resolveField(FieldValue value) {
        return resolveField(value.getValue());
    }

    <T> T resolveMethodsChain(MethodsChain chain);

    default <T> T resolveMethodsChain(MethodsValue value) {
        return resolveMethodsChain(value.getValue());
    }

    <T> T resolveArray(ArrayValue value);

    <T> T resolveCollection(CollectionValue value);

    String resolveURL(URLValue value);

    <T> T resolveValue(Value<?> value);

    default Object[] resolveArgs(Value<?>[] values) {
        Object[] resolved = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            resolved[i] = resolveValue(values[i]);
        }
        return resolved;
    }

    /// Implementations should define how to populate the given controller's instance with the nodes marked and stored
    /// by the [Context]. Ideally, you want to call this just before the loading process ends.
    void injectController(Object controller);

    Context context();

    //================================================================================
    // Inner Classes
    //================================================================================

    /// Support internal class which defines the dependencies needed to turn a [UIDocument] into a UI graph, as well as
    /// keeping track of the process' state.
    ///
    /// _Why do we track the state?_\
    /// The core process of this stage is converting [UIObj] objects to the true objects to be used in the UI tree.
    /// This holds some important pieces of information about each [UIObj] in the document:
    /// 1) Since [UIObj] does not wrap the instance resolved by the process, we store it here in a map `[UIObj -> Object]`
    /// 2) Nodes are visited sequentially in the document, every time one needs to be resolved it's added to a stack.
    /// Once it is instantiated and initialized (including its children if any), it is popped from the stack
    /// 3) Nodes can be marked by ids, [UIObj#getControllerId()]. Those who are marked are stored in a map `[String -> UIObj]`
    /// (automatically handled by [#pushNode(UIObj)]).
    /// By resolving this mapping, and the one in point 1, we can easily inject marked nodes into the controller (if present),
    /// [#injectController(Object)].
    class Context {
        // Deps
        private final Set<String> imports = new HashSet<>();
        private DependencyManager dependencyManager;
        private Scanner scanner;
        private Reflector reflector;
        private final URI location;
        private final Map<String, Object> injections = new HashMap<>();

        // State
        private final Map<String, UIObj> byId = new HashMap<>();
        private final Map<UIObj, Object> instances = new IdentityHashMap<>();
        private final Deque<UIObj> stack = new ArrayDeque<>();

        public Context(URI location) {
            dependencyManager = new DependencyManager();
            scanner = new Scanner(dependencyManager, imports);
            reflector = new Reflector(scanner);
            this.location = location;
        }

        public Context(DependencyManager dependencyManager, Scanner scanner, Reflector reflector, URI location) {
            this.dependencyManager = dependencyManager;
            this.scanner = scanner;
            this.reflector = reflector;
            this.location = location;
            scanner.setImports(imports);
        }

        protected Set<String> getImports() {
            return imports;
        }

        public Context setImports(Set<String> imports) {
            this.imports.clear();
            this.imports.addAll(imports);
            return this;
        }

        public DependencyManager getDependencyManager() {
            return dependencyManager;
        }

        public Context setDependencyManager(DependencyManager dependencyManager) {
            this.dependencyManager = dependencyManager;
            return this;
        }

        public Scanner getScanner() {
            return scanner;
        }

        public Context setScanner(Scanner scanner) {
            this.scanner = scanner;
            return this;
        }

        public Reflector getReflector() {
            return reflector;
        }

        public Context setReflector(Reflector reflector) {
            this.reflector = reflector;
            return this;
        }

        public URI getLocation() {
            return location;
        }

        public Map<String, Object> getInjections() {
            return injections;
        }

        public Context setInjections(Map<String, Object> injections) {
            this.injections.clear();
            this.injections.putAll(injections);
            return this;
        }

        /// Works just like [Map#of()] but on steroids. Although, no type checking is done, it expects argument to be
        /// in pairs of `String, Object`, otherwise it will cause an exception.
        public Context setInjections(Object... injections) {
            Map<String, Object> map = CollectionType.MAP.create(injections);
            this.injections.clear();
            this.injections.putAll(map);
            return this;
        }

        protected Map<String, UIObj> getNodesById() {
            return byId;
        }

        protected Map<UIObj, Object> getInstances() {
            return instances;
        }

        public Object getInstanceByNodeId(String id) {
            UIObj uiObj = byId.get(id);
            return instances.get(uiObj);
        }

        public Object getCurrentInstance() {
            return instances.get(getCurrentNode());
        }

        public UIObj getCurrentNode() {
            return stack.peek();
        }

        protected void pushNode(UIObj uiNode) {
            String sId = uiNode.getControllerId();
            if (sId != null) {
                String id = sId.substring(1, sId.length() - 1);
                byId.put(id, uiNode);
            }
            stack.push(uiNode);
        }

        protected UIObj popNode() {
            return stack.pop();
        }
    }
}
