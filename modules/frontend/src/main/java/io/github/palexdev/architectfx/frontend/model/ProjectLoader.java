/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
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

package io.github.palexdev.architectfx.frontend.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner;
import io.github.palexdev.mfxcore.base.properties.resettable.ResettableObjectProperty;
import io.inverno.core.annotation.Bean;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;

@Bean
public class ProjectLoader {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<String> dependencies;
    private final JUIFXLoader loader = new JUIFXLoader();
    private final ResettableObjectProperty<Progress> progress = new ResettableObjectProperty<>(Progress.INDETERMINATE, Progress.INDETERMINATE);

    private DependencyManager dependencyManager;
    private Scanner scanner;
    private Reflector reflector;

    private Map<UIObj, Object> lastResolved;

    //================================================================================
    // Constructors
    //================================================================================
    public ProjectLoader(ObservableList<String> dependencies) {
        this.dependencies = dependencies;
    }

    //================================================================================
    // Methods
    //================================================================================
    public UILoader.Loaded<Node> load(Project project) throws IOException {
        try {
            return loader.load(project.getFile().toFile());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public void init() {
        dependencyManager = new DependencyManager();
        scanner = new Scanner(dependencyManager);
        reflector = new Reflector(scanner);

        UILoader.Config config = new UILoader.Config()
            .setOnProgress(p -> Platform.runLater(() -> progress.set(p)))
            .setResolverFactory(uri -> {
                dependencyManager.cleanDeps();
                dependencyManager.addDeps(
                    new HashSet<>(dependencies).toArray(String[]::new)
                );

                Resolver.Context context = new Resolver.Context(
                    dependencyManager,
                    scanner,
                    reflector,
                    uri
                );
                lastResolved = context.getInstancesUnmodifiable();
                return new DefaultResolver(context);
            });
        loader.setConfig(config);
    }

    public Node resolveObj(UIObj obj) {
        if (lastResolved == null) return null;
        return ((Node) lastResolved.get(obj));
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ObservableList<String> getDependencies() {
        return dependencies;
    }

    public void addDependencies(String... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
    }

    public void addDependencies(Collection<String> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public Progress getProgress() {
        return progress.get();
    }

    public ResettableObjectProperty<Progress> progressProperty() {
        return progress;
    }
}
