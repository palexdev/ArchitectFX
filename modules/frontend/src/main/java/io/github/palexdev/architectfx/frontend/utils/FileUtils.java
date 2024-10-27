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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {
    //================================================================================
    // Properties
    //================================================================================
    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    //================================================================================
    // Constructors
    //================================================================================
    private FileUtils() {
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static void copy(Path source, Path target) throws IOException {
        if (!Files.exists(target)) Files.createDirectories(target);
        if (Files.isDirectory(source)) {
            copyDirectory(source, target);
        } else {
            Files.copy(source, target, REPLACE_EXISTING, COPY_ATTRIBUTES);
        }
    }

    public static void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                deleteDirectory(dir);
                return CONTINUE;
            }
        });
    }

    private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(targetDir)) Files.createDirectory(targetDir);
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(sourceDir)) {
            for (Path path : paths) {
                Path targetFile = targetDir.resolve(path.getFileName());
                if (Files.isDirectory(path)) {
                    copyDirectory(path, targetFile);
                } else {
                    Files.copy(path, targetFile, REPLACE_EXISTING, COPY_ATTRIBUTES);
                }
            }
        }
    }

    private static void deleteDirectory(Path path) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            for (Path in : paths) {
                if (Files.isDirectory(in)) {
                    deleteDirectory(in);
                    Files.delete(in);
                } else {
                    Files.delete(in);
                }
            }
        }
    }

    public static String sizeToString(File file) {
        List<SizeUnit> units = SizeUnit.unitsInDescending();
        long size = file.length();

        if (size < 0)
            throw new IllegalArgumentException("Invalid file size: " + size);

        for (SizeUnit unit : units) {
            if (size >= unit.getUnitBase()) {
                return formatSize(size, unit.getUnitBase(), unit.name());
            }
        }
        return formatSize(size, SizeUnit.Bytes.getUnitBase(), SizeUnit.Bytes.name());
    }

    public static String getExtension(File file) {
        return Optional.ofNullable(file)
            .map(File::getName)
            .filter(n -> n.contains("."))
            .map(n -> n.substring(n.lastIndexOf(".") + 1))
            .orElse("");
    }

    private static String formatSize(long size, long divider, String unit) {
        return DEC_FORMAT.format((double) size / divider) + " " + unit;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum SizeUnit {
        Bytes(1L),
        KB(Bytes.unitBase * 1000),
        MB(KB.unitBase * 1000),
        GB(MB.unitBase * 1000),
        TB(GB.unitBase * 1000),
        PB(TB.unitBase * 1000),
        EB(PB.unitBase * 1000);

        private final Long unitBase;

        SizeUnit(Long unitBase) {
            this.unitBase = unitBase;
        }

        public Long getUnitBase() {
            return unitBase;
        }

        public static List<SizeUnit> unitsInDescending() {
            List<SizeUnit> list = Arrays.asList(values());
            Collections.reverse(list);
            return list;
        }
    }
}