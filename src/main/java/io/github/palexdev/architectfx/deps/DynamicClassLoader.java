package io.github.palexdev.architectfx.deps;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.tinylog.Logger;

public class DynamicClassLoader extends URLClassLoader {

	//================================================================================
	// Constructors
	//================================================================================
	public DynamicClassLoader() {
		this(getSystemClassLoader());
	}

	public DynamicClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	public DynamicClassLoader addJars(Collection<File> files) {
		for (File f : files) {
			addJar(f);
		}
		return this;
	}

	public DynamicClassLoader addJars(File... files) {
		for (File f : files) {
			addJar(f);
		}
		return this;
	}

	public DynamicClassLoader addJar(File file) {
		try {
			URL jar = file.toURI().toURL();
			Logger.info("Loading dependency: {}", jar);
			super.addURL(jar);
		} catch (Exception ex) {
			Logger.error(ex, "Failed to add file {} to dynamic class loader", file);
		}
		return this;
	}
}