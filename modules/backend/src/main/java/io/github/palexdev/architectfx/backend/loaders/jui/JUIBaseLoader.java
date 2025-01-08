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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.jui.JUILexer;
import io.github.palexdev.architectfx.backend.jui.JUIParser;
import io.github.palexdev.architectfx.backend.jui.JUIVisitor;
import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.model.UIDocument;
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
    public static final String EXTENSION = "jui";
    public static final String NEW_TEMPLATE = """
        .deps {}
        
        .imports {
          'javafx.scene.control.Label',
          'javafx.scene.layout.Pane',
          'javafx.scene.layout.StackPane',
          'javafx.scene.paint.Color',
          'javafx.scene.shape.FillRule',
          'javafx.scene.shape.SVGPath'
        }
        
        # .controller: AControllerClass {}
        
        StackPane {
          Label::('Have fun 🙋‍♂️') {
            graphic: Pane {
              minWidth: 64.0
              prefWidth: 64.0
              maxWidth: 64.0
              minHeight: 64.0
              prefHeight: 64.0
              maxHeight: 64.0
        
              SVGPath {
                content: 'M245.173 56.026c-6.857.942-10.682 1.984-19.173 5.22-5.32 2.027-111.992 62.947-120.904 69.047-13.38 9.159-24.169 24.226-30.223 42.207l-2.357 7-.307 72c-.211 49.628.04 74.003.806 78.447 2.757 15.985 11.682 31.921 24.67 44.053 7.262 6.783 13.375 10.576 63.922 39.659 30.741 17.687 57.693 33.049 59.893 34.137 9.134 4.518 17.648 6.422 31.043 6.943 14.732.574 24.484-1.224 36.076-6.648 6.855-3.208 110.296-62.658 117.285-67.407 10.678-7.255 22.059-21.468 27.371-34.184 6.195-14.828 6.225-15.272 6.225-92 0-79.57.353-75.661-8.489-94-5.384-11.166-13.406-21.391-22.011-28.054-5.671-4.391-111.518-65.923-120.59-70.101-11.639-5.362-30.327-8.093-43.237-6.319m-.81 12.999c-10.668 1.923-19.64 6.507-74.506 38.062-55.224 31.761-56.53 32.584-64.974 40.913-7.357 7.257-9.281 9.89-13.154 18-7.498 15.704-7.229 12.368-7.229 89.5 0 77.846-.191 75.6 7.723 90.812 4.428 8.511 13.341 18.928 20.223 23.634 5.315 3.635 106.279 61.886 114.054 65.803 7.358 3.707 11.817 5.011 21.46 6.276 9.179 1.205 21.575-.163 30.339-3.346 3.41-1.239 31.61-16.861 62.665-34.716 56.426-32.441 56.47-32.469 65.539-41.463 7.048-6.99 9.895-10.675 12.746-16.5 7.505-15.336 7.251-12.127 7.251-91.5v-71l-2.715-8c-4.503-13.272-12.458-24.919-22.101-32.358-2.862-2.208-30.624-18.682-61.694-36.609-43.173-24.911-58.358-33.186-64.412-35.102-8.925-2.825-22.913-3.903-31.215-2.406m3.655 71.893c-6.901 2.432-13.37 8.96-15.046 15.186-2.792 10.37-4.597 6.44 50.934 110.917 28.204 53.064 52.423 97.99 53.82 99.837 5.3 7.006 18.407 10.199 27.056 6.591 10.327-4.307 17.015-17.063 14.277-27.231-1.348-5.003-103.169-196.641-106.187-199.854-5.732-6.102-16.374-8.434-24.854-5.446m-42.652 75.587c-2.765 1.378-5.971 3.515-7.125 4.75-3.847 4.116-65.338 120.894-66.476 126.245-2.103 9.894 3.513 21.036 12.894 25.577 8.895 4.306 21.073 2.204 27.605-4.766 4.225-4.509 65.583-120.498 66.838-126.349 1.63-7.6-.905-15.13-7.119-21.145-7.582-7.34-17.362-8.924-26.617-4.312m40.093 77.862c-8.818 4.106-13.466 11.484-13.449 21.351.017 10.092 5.88 19.234 14.708 22.934 5.115 2.143 14.014 1.686 19.823-1.019 8.818-4.106 13.466-11.484 13.449-21.351-.017-10.092-5.88-19.234-14.708-22.934-5.115-2.143-14.014-1.686-19.823 1.019'
                fill: Color.web('#36618E')
                fillRule: FillRule.EVEN_ODD
                scaleX: 0.15
                scaleY: 0.15
              }
            }
        
            style: ""\"
              -fx-border-color: #36618E;
              -fx-border-radius: 12px;
              -fx-font-size: 24px;
              -fx-font-weight: bold;
              -fx-text-fill: #36618E;
              -fx-padding: 8px;
            ""\"
          }
        }
        """;
    protected Config config = new Config();
    protected Resolver resolver;

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public Loaded<T> load(UIDocument document) throws IOException {
        try {
            resolver = config().resolver(document.getLocation());
            resolver.context().setChildrenHandler(this::attachChildren);

            // 1) Handle dependencies
            onProgress("Adding dependencies", 0.0);
            resolver.context().getDependencyManager().addDeps(
                document.getDependencies().toArray(String[]::new)
            );

            // 2) Handle imports
            onProgress("Adding imports", 0.2);
            resolver.context().setImports(document.getImports());

            // 3) Handle controller
            onProgress("Handling controller", 0.3);
            Optional<Object> controller = Optional.empty();
            if (config.getControllerFactory() != null) {
                controller = Optional.ofNullable(config.getControllerFactory().get());
            } else if (document.getController() != null) {
                controller = Optional.ofNullable(resolver.resolveObj(document.getController()));
            }

            // 4) Instantiate UI graph
            onProgress("Loading UI", 0.3);
            T root = resolver.resolveObj(document.getRoot());

            // 5) Inject controller
            onProgress("Injecting controller", 0.9);
            controller.ifPresent(resolver::injectController);

            // 6) Finally return result
            onProgress("Loaded!", 1.0);
            return new Loaded<>(document, root, controller.orElse(null));
        } finally {
            config.setControllerFactory(null);
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
