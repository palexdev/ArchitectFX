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

package io.github.palexdev.architectfx.frontend.utils;

import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Pair;

public class KeyValueProperty<K, V> extends ReadOnlyObjectWrapper<Pair<K, V>> {

    //================================================================================
    // Constructors
    //================================================================================
    public KeyValueProperty() {}

    public KeyValueProperty(Pair<K, V> initialValue) {
        super(initialValue);
    }

    public KeyValueProperty(Object bean, String name) {
        super(bean, name);
    }

    public KeyValueProperty(Object bean, String name, Pair<K, V> initialValue) {
        super(bean, name, initialValue);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public K getKey() {
        return Optional.ofNullable(get()).map(Pair::getKey).orElse(null);
    }

    public V getVal() {
        return Optional.ofNullable(get()).map(Pair::getValue).orElse(null);
    }

    public void setPair(K key, V value) {
        set(new Pair<>(key, value));
    }
}
