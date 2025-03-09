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


import io.github.palexdev.architectfx.backend.enums.CollectionHandleStrategy;
import io.github.palexdev.architectfx.backend.model.CollectionProperty;
import io.github.palexdev.architectfx.backend.model.ObjProperty;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.model.types.*;
import io.github.palexdev.architectfx.backend.model.types.Value.*;
import io.github.palexdev.architectfx.backend.utils.CastUtils;
import io.github.palexdev.architectfx.backend.utils.reflection.ArrayUtils;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.tinylog.Logger;

public class DefaultResolver implements Resolver {
    //================================================================================
    // Properties
    //================================================================================
    private final Context context;

    //================================================================================
    // Constructors
    //================================================================================

    /// This constructor creates a [Context] with `null` location, which makes the _resources' resolution_ mechanism unavailable.
    public DefaultResolver() {
        this((URI) null);
    }

    public DefaultResolver(URI location) {
        this.context = new Context(location);
    }

    public DefaultResolver(Context context) {
        this.context = context;
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void handleCollectionProperty(Object target, CollectionProperty property) {
        Reflector reflector = context().getReflector();
        Object collection = resolveCollection(property.getValue());
        boolean clear = property.getStrategy() == CollectionHandleStrategy.SET;
        reflector.handleCollection(target, property.getName(), property.getValue().getCollectionType(), collection, clear);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public <T> T resolveObj(UIObj obj) {
        Reflector reflector = context.getReflector();
        // 1) Instantiate
        String type = obj.getType();
        T instance = switch (obj.getConstructor()) {
            case ObjConstructor.Simple s -> {
                Object[] args = resolveArgs(s.args());
                yield reflector.instantiate(type, args);
            }
            case ObjConstructor.Factory f -> resolveMethodsChain(f.methods());
            case null, default -> reflector.instantiate(type);
        };
        if (instance == null) return null;
        context.getInstances().put(obj, instance);
        context.pushNode(obj);

        // 2) Initialize
        for (ObjProperty prop : obj.getProperties().values()) {
            if (prop instanceof CollectionProperty cp) {
                handleCollectionProperty(instance, cp);
                continue;
            }
            String name = prop.getName();
            Object val = resolveValue(prop.getValue());
            reflector.set(instance, name, val);
        }

        // 3) Invoke methods
        for (MethodsChain chain : obj.getMethods()) {
            resolveMethodsChain(chain);
        }

        // Handle children
        if (!obj.getChildren().isEmpty()) {
            List<T> children = new ArrayList<>();
            for (UIObj cObj : obj.getChildren()) {
                T cInstance = resolveObj(cObj);
                children.add(cInstance);
            }
            context.attachChildren(instance, children);
        }

        context.popNode();
        return instance;
    }

    @Override
    public <T> T resolveKeyword(KeywordValue value) {
        return switch (value.getValue()) {
            case THIS -> CastUtils.unchecked(context.getCurrentInstance());
            case NULL -> null;
            case INJECTION -> {
                String[] payload = (String[]) value.getPayload();
                if (payload == null || payload.length == 0) {
                    Logger.error("Injection failed because of empty payload");
                    yield null;
                }
                Object obj = context().getInjections().get(payload[0]);
                if (obj == null) {
                    Logger.warn("Injection failed because object {} was not found in the configuration", payload[0]);
                    yield null;
                }
                yield CastUtils.unchecked(obj);
            }
        };
    }

    @Override
    public <T> T resolveField(FieldRef ref) {
        Reflector reflector = context().getReflector();
        Object target = Optional.<Object>ofNullable(ref.getOwner())
            .orElse(context.getCurrentInstance());
        return reflector.get(target, ref.getName());
    }

    @Override
    public <T> T resolveMethodsChain(MethodsChain chain) {
        List<MethodCall> methods = chain.getMethods();
        if (methods.isEmpty()) return null;

        Reflector reflector = context().getReflector();
        Optional<?> target = Optional.<Object>ofNullable(methods.getFirst().getOwner())
            .or(() -> Optional.of(context.getCurrentInstance()));
        for (MethodCall method : methods) {
            if (target.isEmpty())
                throw new IllegalArgumentException("Cannot resolve methods chain further as invocation target is null");
            Object[] args = resolveArgs(method.getArgs());
            target = reflector.invoke(target.get(), method.getName(), args);
        }
        return CastUtils.unchecked(target.orElse(null));
    }

    @Override
    public <T> T resolveArray(ArrayValue value) {
        Class<?> componentType;
        try {
            componentType = context().getScanner().findClass(value.getComponentType());
        } catch (ClassNotFoundException ex) {
            Logger.error("Failed to get array type for {} because:\n{}" + value.getComponentType(), ex);
            return null;
        }
        return ArrayUtils.createArray(componentType, resolveArgs(value.getValue()));
    }

    @Override
    public <T> T resolveCollection(CollectionValue value) {
        Object[] items = resolveArgs(value.getValue());
        return value.getCollectionType().create(items);
    }

    @Override
    public String resolveURL(URLValue value) {
        URI location = context.getLocation();
        if (location == null) {
            Logger.warn("Resources resolution is not available as load location is null");
            return null;
        }

        URI res = value.toURI();
        if (res.isAbsolute()) {
            Logger.debug("Resource location {} is absolute", res);
            return res.toString();
        }

        /* Jar protocol is shit. URI sucks. We'll use URL for now and eventually string manipulation */
        try {
            if (location.getScheme().equals("jar")) {
                Logger.debug("Handling resource resolution for jar protocol...");
                URL base = location.toURL();
                return new URL(base, value.getValue()).toString();
            }
        } catch (Exception ex) {
            Logger.error("Failed to resolve jar resource {} because:\n{}", res, ex);
            return null;
        }

        res = location.resolve(res);
        return res.toString();
    }

    @Override
    public <T> T resolveValue(Value<?> value) {
        return switch (value.getType()) {
            case OBJ -> resolveObj((UIObjValue) value);
            case KEYWORD -> resolveKeyword((KeywordValue) value);
            case FIELD -> resolveField((FieldValue) value);
            case METHODS -> resolveMethodsChain((MethodsValue) value);
            case ARRAY -> resolveArray((ArrayValue) value);
            case COLLECTION -> resolveCollection((CollectionValue) value);
            case BOOLEAN, CHAR, STRING, NUMBER -> CastUtils.unchecked(value.getValue());
            case URL -> CastUtils.unchecked(resolveURL((URLValue) value));
        };
    }

    @Override
    public void injectController(Object controller) {
        Reflector reflector = context().getReflector();
        for (Map.Entry<String, UIObj> e : context.getNodesById().entrySet()) {
            String name = e.getKey();
            UIObj obj = e.getValue();
            Object instance = context.getInstances().get(obj);
            reflector.set(controller, name, instance);
        }
    }

    @Override
    public Context context() {
        return context;
    }
}
