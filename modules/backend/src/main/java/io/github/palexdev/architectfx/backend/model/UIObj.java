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

package io.github.palexdev.architectfx.backend.model;


import java.util.*;

import io.github.palexdev.architectfx.backend.model.types.MethodsChain;
import io.github.palexdev.architectfx.backend.model.types.ObjConstructor;

/// Core class which represents any kind of object in the UI tree. Objects in the document can appear everywhere:
/// as values for properties, as arguments, as children.
///
/// Syntactically speaking, we do not differentiate between a UI object (Button, Pane, etc...) and any other type mainly
/// for two reasons:
/// 1) I don't really know it we can differentiate on the grammar level. Probably yes, but in my opinion it would just
/// make the grammar more messy, hard to maintain and develop.
/// 2) UI objects are, well, objects. Even though they have some special attributes and treatments, we can still use a
/// single class to represent them all.
///
/// All objects have:
/// 1) A `type`, expressed through a [String] value, and resolved to a class during the load process.
/// 2) A `constructor`, a way to build an instance, expressed through [ObjConstructor]. In case it is `null`,
/// the no-args constructor should be invoked.
/// 3) Chains of `methods`, expressed through the [MethodsChain] class. Objects may need extra/special configuration
/// which can be performed only by invoking methods. [MethodsChain] allows for that.
/// 4) `Properties`. All objects can have fields to be set or retrieved
///
/// _So, what differentiates UI objects from a generic object?_
/// 1) They can have a `controllerId`, which corresponds to the name of a field in the controller object, used to inject
/// this into the controller
/// 2) They have a `parent` UI object, except for the root. About that: the parent property is automatically handled by
/// [UIObj] and is set on the children when invoking [#addChildren(UIObj...)], [#setChildren(UIObj...)] or
/// [#removeChildren(UIObj...)]. Beware that for this reason, the list returned by [#getChildren()] is immutable!
/// 3) `Children`. UI nodes typically form a graph/hierarchy of nodes.
///
///
public class UIObj {
    //================================================================================
    // Properties
    //================================================================================
    private String controllerId;
    private UIObj parent;
    private final String type;
    private ObjConstructor constructor;
    private final List<MethodsChain> methods = new ArrayList<>();
    private final SequencedMap<String, ObjProperty> properties = new LinkedHashMap<>();
    private final List<UIObj> children = new ArrayList<>();

    protected boolean root = false;

    //================================================================================
    // Constructors
    //================================================================================
    public UIObj(String type) {
        this.type = type;
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Responsible for adding children nodes to this [UIObj] because [#getChildren()] returns an immutable list.
    ///
    /// Automatically handles the `parent` property of each child.
    public void addChildren(UIObj... children) {
        for (UIObj child : children) {
            this.children.add(child);
            child.parent = this;
            child.root = false;
        }
    }

    /// Responsible for adding children nodes to this [UIObj] because [#getChildren()] returns an immutable list.
    /// Before that though, the current children, if any is present, are removed from this obj and the `parent` property
    /// is reset.
    ///
    /// Automatically handles the `parent` property of each child.
    public void setChildren(UIObj... children) {
        this.children.forEach(c -> c.parent = null);
        this.children.clear();
        addChildren(children);
    }

    /// Responsible for removing all the given children from this [UIObj] because [#getChildren()] returns an immutable list.
    ///
    /// Automatically handles the `parent` property of each removed child.
    public void removeChildren(UIObj... children) {
        for (UIObj child : children) {
            if (this.children.remove(child)) {
                child.parent = null;
            }
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("UINode{");
        if (parent != null) sb.append("parent='").append(parent.type).append("', ");
        sb.append("type='").append(type).append('\'');
        if (constructor != null) sb.append(", constructor=").append(constructor);
        return sb.append('}').toString();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public UIObj getParent() {
        return parent;
    }

    public String getType() {
        return type;
    }

    public ObjConstructor getConstructor() {
        return constructor;
    }

    public void setConstructor(ObjConstructor constructor) {
        this.constructor = constructor;
    }

    public List<MethodsChain> getMethods() {
        return methods;
    }

    public SequencedMap<String, ObjProperty> getProperties() {
        return properties;
    }

    public void addProperty(ObjProperty property) {
        properties.put(property.getName(), property);
    }

    public Optional<ObjProperty> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    public List<UIObj> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /// @return whether this obj is the root of the loaded document
    public boolean isRoot() {
        return root;
    }
}
