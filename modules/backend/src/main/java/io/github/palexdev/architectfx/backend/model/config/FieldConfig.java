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

package io.github.palexdev.architectfx.backend.model.config;

import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.backend.utils.Tuple3;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.yaml.Tags;
import io.github.palexdev.architectfx.backend.yaml.YamlParser;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.backend.yaml.Tags.FIELD_TAG;
import static io.github.palexdev.architectfx.backend.yaml.Tags.VALUE_TAG;

/// Concrete implementation of [Config] which is specifically designed to set static fields. This means that if the
/// [#owner()] is `null`, [#run(Object)] won't even execute.
///
/// At the time of writing this, I see no point in having support for instance fields since using properties is more immediate.
///
/// Besides the common properties inherited by [Config], this also stores the `value` to which set the static field.
///
/// The parsing of a `FieldConfig` from YAML is done by [#parse(YamlParser, SequencedMap)]
// TODO allow instance fields eventually
public class FieldConfig extends Config {
    //================================================================================
    // Properties
    //================================================================================
    private final Object value;

    //================================================================================
    // Constructors
    //================================================================================
    public FieldConfig(Class<?> owner, String field, Object value) {
        super(owner, field);
        this.value = value;
    }

    //================================================================================
    // Static Methods
    //================================================================================

    /// This method is responsible for parsing a `FieldConfig` from YAML given:
    /// 1) The [YamlParser] which, as the name suggests, is the core class responsible for parsing YAML
    /// 2) The `map` containing the config's parameters
    ///
    /// - The owner and field name are extracted from the [Tags#FIELD_TAG] and parsed using
    /// [Reflector#getFieldInfo(Object, boolean)] (enums not allowed)
    /// - The value is extracted from the [Tags#VALUE_TAG] and parsed using [YamlParser#parseValue(Object)]
    ///
    /// In case anything goes wrong with the parsing, returns an empty [Optional].
    protected static Optional<FieldConfig> parse(YamlParser parser, SequencedMap<String, Object> map) {
        Object value = map.get(VALUE_TAG);
        if (value == null) {
            Logger.warn("Field config does not specify the {} tag\n{}", VALUE_TAG, map);
            return Optional.empty();
        }

        Object fObj = map.get(FIELD_TAG);
        Tuple3<Class<?>, String, Object> fInfo = parser.reflector().getFieldInfo(fObj, false);
        if (fInfo == null || fInfo.a() == null) {
            Logger.warn(
                "Invalid config, either because field info is null or because field is not static, skipping..."
            );
            return Optional.empty();
        }

        // TODO allow null values eventually
        value = parser.parseValue(value);
        if (value == null) {
            Logger.warn("Config value is null, skipping...");
            return Optional.empty();
        }
        return Optional.of(new FieldConfig(fInfo.a(), fInfo.b(), value));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// With the previously parsed information, sets the static field with name [#member()] on the owner class [#owner()]
    /// and returns an [Optional] which wraps the input.
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> run(Object obj) {
        try {
            Logger.debug("Running config {}. Setting static field to {}", name(), Objects.toString(value));
            Reflect.onClass(owner)
                .set(member, value);
            return Optional.ofNullable((T) obj);
        } catch (ReflectException ex) {
            Logger.error("Failed to execute config:\n{}", ex);
        }
        return Optional.empty();
    }

    //================================================================================
    // Getters
    //================================================================================

    /// @return the parsed value to which set the static field
    public Object value() {
        return value;
    }
}
