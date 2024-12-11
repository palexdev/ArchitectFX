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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.backend.utils.CastUtils;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.imcache.cache.DiskCache;
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
import org.yaml.snakeyaml.Yaml;

public class Recent implements Comparable<Recent> {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final Image PLACEHOLDER = new Image(
        Resources.loadURL("assets/placeholder.png").toExternalForm()
    );

    private static final Path CACHE_PATH = Path.of(System.getProperty("java.io.tmpdir"), "ArchitectFX");
    private static final DiskCache DISK_CACHE = new DiskCache().saveTo(CACHE_PATH);

    static {
        DISK_CACHE.setCapacity(Integer.MAX_VALUE);
        try {
            Files.createDirectories(CACHE_PATH);
            DISK_CACHE.scan();
        } catch (IOException ex) {
            Logger.error("Failed to create previews cache directory because:\n{}", ex);
        }
    }

    //================================================================================
    // Properties
    //================================================================================
    private String id = null;
    private final Path file;
    private final ReadOnlyLongWrapper lastModified = new ReadOnlyLongWrapper();
    private final ObjectProperty<Image> preview = new SimpleObjectProperty<>() {
        @Override
        public void set(Image newValue) {
            Image oldValue = get();
            if (oldValue != null && newValue != null && newValue != PLACEHOLDER) {
                Async.run(() -> {
                    Logger.info("Saving preview for recent {}", Recent.this);
                    ImImage wrapped = ImImage.wrap(
                        file.toString(),
                        ImageUtils.toBytes("png", SwingFXUtils.fromFXImage(newValue, null))
                    );
                    DISK_CACHE.store(id(), wrapped);
                });
            }
            super.set(newValue);
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public Recent(Path file) {
        if (!isValid(file))
            throw new IllegalArgumentException("Invalid recent file: %s".formatted(file));
        this.file = file;
        updateLastModified();

        Async.run(() -> {
            Logger.info("Loading preview for recent {}", Recent.this);
            DISK_CACHE.get(id())
                .ifPresentOrElse(
                    f -> {
                        try {
                            ImImage imImage = ImageUtils.deserialize(f);
                            Image fxImage = new Image(imImage.asStream());
                            Platform.runLater(() -> setPreview(fxImage));
                        } catch (Exception ex) {
                            Logger.error("Failed to load preview from disk:\n{}", ex);
                            Platform.runLater(() -> setPreview(PLACEHOLDER));
                        }
                    },
                    () -> Platform.runLater(() -> setPreview(PLACEHOLDER))
                );
        });
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static boolean isValid(Path file) {
        return file != null && Files.exists(file) && !Files.isDirectory(file);
    }

    public static List<Recent> load(String yaml) {
        List<Recent> recents = new ArrayList<>();
        try {
            SequencedMap<String, Object> map = new Yaml().load(yaml);
            List<Object> list = CastUtils.asGenericList(map.get("recents"));
            for (Object obj : list) {
                try {
                    Path file = Path.of(((String) obj));
                    Recent recent = new Recent(file);
                    recents.add(recent);
                } catch (Exception ex) {
                    Logger.warn(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Logger.error("Failed to load recents:\n{}", ex);
        }
        Collections.sort(recents);
        return recents;
    }

    public static String save(Collection<Recent> recents) {
        StringBuilder sb = new StringBuilder("recents: ");
        if (recents.isEmpty()) {
            sb.append("[]");
            return sb.toString();
        }

        sb.append("\n");
        for (Recent recent : recents) {
            if (!recent.isValid()) {
                Logger.warn("Invalid recent:\n- {}\n- {}", recent.file(), recent.lastModified());
                continue;
            }
            String path = recent.file().toString().replace("\\", "\\\\");
            sb.append("  - \"").append(path).append("\"\n");
        }
        return sb.toString();
    }

    //================================================================================
    // Methods
    //================================================================================
    public void updateLastModified() {
        lastModified.set(file.toFile().lastModified());
    }

    public boolean isValid() {
        return isValid(file);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public int compareTo(Recent o) {
        return Long.compare(lastModified(), o.lastModified());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recent recent = (Recent) o;
        return Objects.equals(file, recent.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }

    @Override
    public String toString() {
        return "Recent{" +
               "file=" + file +
               ", lastModified=" + lastModified +
               '}';
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
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

    public Path file() {
        return file;
    }

    public long lastModified() {
        return lastModified.get();
    }

    public ReadOnlyLongProperty lastModifiedProperty() {
        return lastModified.getReadOnlyProperty();
    }

    public Image preview() {
        return preview.get();
    }

    public ObjectProperty<Image> previewProperty() {
        return preview;
    }

    public void setPreview(Image preview) {
        this.preview.set(preview);
    }
}
