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

package io.github.palexdev.architectfx.frontend.model;

import java.util.Comparator;

import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.ui.RefineList;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.utils.StringUtils;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.BeanSocket;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

@Bean
public class AppModel {
    //================================================================================
    // Properties
    //================================================================================
    // Dependencies
    private final IEventBus events;
    private final AppSettings settings;

    // State
    private final RefineList<Project> projects;
    private final StringProperty filter = new SimpleStringProperty() {
        @Override
        protected void invalidated() {
            filterProjects();
        }
    };
    private final ObjectProperty<Project.SortBy> projectsSortBy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            sortProjects();
        }
    };
    private final ObjectProperty<Project.SortMode> projectsSortMode = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            sortProjects();
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public AppModel(IEventBus events, AppSettings settings, ObservableList<Project> projectsList) {
        this.events = events;
        this.settings = settings;

        /* Projects */
        this.projects = new RefineList<>(projectsList);
        Project.SortMode mode;
        Project.SortBy sortBy;
        try {
            mode = Project.SortMode.valueOf(settings.getProjectsSortMode().get());
            sortBy = Project.SortBy.valueOf(settings.getProjectsSort().get());
        } catch (Exception ex) {
            mode = Project.SortMode.valueOf(settings.getProjectsSortMode().defValue());
            sortBy = Project.SortBy.valueOf(settings.getProjectsSort().defValue());
        }
        setProjectsSortMode(mode);
        setProjectsSortBy(sortBy);

        /* Events Handling */
        events.subscribe(AppEvent.AppCloseEvent.class, e -> {
            settings.saveProjects(projects.getSrc());
            settings.getProjectsSortMode().set(getProjectsSortMode().name());
            settings.getProjectsSort().set(getProjectsSortBy().name());
        });
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void filterProjects() {
        String filter = getFilter();
        if (filter == null || filter.isEmpty()) {
            projects.setPredicate(null);
            return;
        }
        projects.setPredicate(p -> StringUtils.containsIgnoreCase(p.getName(), filter));
    }

    protected void sortProjects() {
        Project.SortBy sortBy = getProjectsSortBy();
        if (sortBy == null) {
            projects.setComparator(null);
            return;
        }
        Project.SortMode mode = getProjectsSortMode();
        Comparator<Project> comparator = (mode == Project.SortMode.ASCENDING) ?
            sortBy.getComparator() :
            sortBy.getComparator().reversed();
        projects.setComparator(comparator);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public RefineList<Project> getProjects() {
        return projects;
    }

    public String getFilter() {
        return filter.get();
    }

    public StringProperty filterProperty() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter.set(filter);
    }

    public Project.SortBy getProjectsSortBy() {
        return projectsSortBy.get();
    }

    public ObjectProperty<Project.SortBy> projectsSortByProperty() {
        return projectsSortBy;
    }

    public void setProjectsSortBy(Project.SortBy projectsSortBy) {
        this.projectsSortBy.set(projectsSortBy);
    }

    public Project.SortMode getProjectsSortMode() {
        return projectsSortMode.get();
    }

    public ObjectProperty<Project.SortMode> projectsSortModeProperty() {
        return projectsSortMode;
    }

    @BeanSocket(enabled = false)
    public void setProjectsSortMode(Project.SortMode projectsSortMode) {
        this.projectsSortMode.set(projectsSortMode);
    }
}
