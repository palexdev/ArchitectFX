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

package io.github.palexdev.architectfx.backend.loaders.jui;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.jui.JUILexer;
import io.github.palexdev.architectfx.backend.jui.JUIParser;
import io.github.palexdev.architectfx.backend.jui.JUIVisitor;
import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.model.UIDocument;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/// Implementation of [UILoader] which works on _JUI_ documents, still framework-independent as [#attachChildren(Object, List)]
/// is not yet implemented.
public abstract class JUIBaseLoader<T> implements UILoader<T> {
    //================================================================================
    // Properties
    //================================================================================
    protected Config config = new Config();
    protected Resolver resolver;

    //================================================================================
    // Methods
    //================================================================================
    protected T createObj(UIObj obj) throws IOException {
        // Instantiate and handle children
        //
        // The resolver should take care of initialization and method calling
        T instance = resolver.resolveObj(obj);
        if (!obj.getChildren().isEmpty()) {
            List<T> children = new ArrayList<>();
            for (UIObj cObj : obj.getChildren()) {
                T cInstance = createObj(cObj);
                children.add(cInstance);
            }
            attachChildren(instance, children);
        }
        return instance;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public Loaded<T> load(UIDocument document) throws IOException {
        try {
            resolver = config().resolver(document.getLocation());

            // 1) Handle dependencies
            resolver.context().getDependencyManager().addDeps(
                document.getDependencies().toArray(String[]::new)
            );

            // 2) Handle imports
            resolver.context().setImports(document.getImports());

            // 3) Instantiate controller
            Optional<Object> controller = (document.getController() != null) ?
                Optional.ofNullable(createObj(document.getController())) :
                Optional.empty();

            // 4) Instantiate UI graph
            T root = createObj(document.getRoot());

            // 5) Inject controller
            controller.ifPresent(resolver::injectController);

            // 6) Finally return result
            return new Loaded<>(document, root, controller.orElse(null));
        } finally {
            resolver = null;
        }
    }

    @Override
    public Loaded<T> load(InputStream is, URL location) throws IOException {
        CharStream cs = CharStreams.fromStream(is);
        JUILexer lexer = new JUILexer(cs);
        CommonTokenStream cts = new CommonTokenStream(lexer);
        JUIParser parser = new JUIParser(cts);
        return load(JUIVisitor.INSTANCE.visit(parser.document(), location));
    }

    @Override
    public Config config() {
        return config;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Config getConfig() {
        return config;
    }

    public JUIBaseLoader<T> setConfig(Config config) {
        this.config = config;
        return this;
    }

    public JUIBaseLoader<T> setConfig(Supplier<Config> configSupplier) {
        return setConfig(configSupplier.get());
    }
}
