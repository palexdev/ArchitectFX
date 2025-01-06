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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;

import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.architectfx.frontend.utils.FileObserver;
import io.github.palexdev.architectfx.frontend.utils.FileUtils;
import io.github.palexdev.imcache.cache.DiskCache;
import io.github.palexdev.imcache.cache.Identifiable;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.utils.ImageUtils;
import io.github.palexdev.mfxcore.utils.fx.SwingFXUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import org.tinylog.Logger;

public class Project implements Comparable<Project>, Identifiable {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Image PLACEHOLDER_PREVIEW = new Image(
        Resources.loadURL("assets/placeholder.png").toExternalForm()
    );

    //================================================================================
    // Properties
    //================================================================================
    private String id;
    private final Path file;
    private final ReadOnlyLongWrapper lastModified = new ReadOnlyLongWrapper();
    private final ObjectProperty<Image> preview = new SimpleObjectProperty<>(PLACEHOLDER_PREVIEW) {
        @Override
        protected void invalidated() {
            Image img = get();
            savePreview(img);
        }
    };
    private FileObserver observer;
    private Future<?> saveTask;

    //================================================================================
    // Constructors
    //================================================================================
    static {
        // Configure preview's cache
        ImCache.instance()
            .cacheConfig(() -> new DiskCache()
                .saveTo(ArchitectFX.appCacheDir())
                .setCapacity(Integer.MAX_VALUE)
                .scan()
            );
    }

    public Project(Path file) {
        if (!FileUtils.isValidFile(file))
            throw new IllegalArgumentException("Invalid project file: %s".formatted(file));
        this.file = file;
        observer = FileObserver.observeFile(file)
            .condition(e -> FileObserver.IS_CHILD.apply(e, file))
            .onChanged(f -> updateLastModified())
            .executeNow()
            .listen();

        /* Load preview async */
        Async.run(() -> ImCache.instance()
            .storage()
            .getImage(this)
            .ifPresentOrElse(
                i -> {
                    Image img = new Image(i.asStream());
                    Platform.runLater(() -> setPreview(img));
                },
                () -> Logger.debug("No previous preview was found for project {}", getName())
            ));
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static List<Project> fromString(String s) {
        List<Project> projects = new ArrayList<>();
        try {
            String[] lines = s.split("\n");
            if (lines.length <= 2) return projects;

            for (int i = 1; i < lines.length - 1; i++) {
                String line = lines[i];
                Path path = Paths.get(line.substring(2, line.length() - 1));
                projects.add(new Project(path));
            }
        } catch (Exception ex) {
            Logger.error("Failed to load project:\n{}", ex);
        }
        return projects;
    }

    public static String toString(Collection<Project> projects) {
        if (projects.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (Project project : projects) {
            if (!project.isValid()) {
                Logger.warn("Invalid recent:\n- {}\n- {}", project.getFile(), project.getLastModified());
                continue;
            }
            sb.append("  ").append(project.getFile().toString()).append(",\n");
        }
        return sb.append("]").toString();
    }

    //================================================================================
    // Methods
    //================================================================================

    public void updateLastModified() {
        lastModified.set(file.toFile().lastModified());
    }

    protected void savePreview(Image img) {
        if (saveTask != null) saveTask.cancel(true);
        saveTask = Async.run(() -> {
            try {
                byte[] toBytes = ImageUtils.toBytes("png", SwingFXUtils.fromFXImage(img, null));
                ImImage wrap = new ImImage(file.toUri().toURL(), toBytes);
                ImCache.instance()
                    .storage()
                    .store(this, wrap);
            } catch (Exception ex) {
                Logger.error("Failed to save preview for project {} because:\n{}", getName(), ex);
            }
        });
    }

    public boolean isValid() {
        return FileUtils.isValidFile(file);
    }

    public void dispose() {
        if (observer != null) {
            observer.dispose();
            observer = null;
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public int compareTo(Project o) {
        return Long.compare(getLastModified(), o.getLastModified());
    }

    @Override
    public String id() {
        if (id == null) {
            try {
                id = UUID.nameUUIDFromBytes(file.toString().getBytes()).toString();
            } catch (Exception ex) {
                Logger.error("Failed to generate id for recent {} because:\n{}", this, ex);
            }
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(file, project.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }


    @Override
    public String toString() {
        return "Project{" +
               "file=" + file +
               ", lastModified=" + getLastModified() +
               '}';
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    public String getName() {
        String name = getFile().getFileName().toString();
        return name.substring(0, name.indexOf("."));
    }

    public Path getFile() {
        return file;
    }

    public long getLastModified() {
        return lastModified.get();
    }

    public ReadOnlyLongProperty lastModifiedProperty() {
        return lastModified.getReadOnlyProperty();
    }

    public Image getPreview() {
        return preview.get();
    }

    public ObjectProperty<Image> previewProperty() {
        return preview;
    }

    public void setPreview(Image preview) {
        this.preview.set(preview);
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum SortBy {
        NAME(Comparator.comparing(Project::getName)),
        DATE(Comparator.comparingLong(Project::getLastModified).reversed()),
        ;

        final Comparator<Project> comparator;

        SortBy(Comparator<Project> comparator) {this.comparator = comparator;}

        public Comparator<Project> getComparator() {
            return comparator;
        }
    }

    public enum SortMode {
        ASCENDING,
        DESCENDING
    }
}
