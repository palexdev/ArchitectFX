package utils;

import org.joor.Reflect;

import com.sun.javafx.application.PlatformImpl;

import io.github.palexdev.architectfx.utils.ReflectionUtils;

public class TestUtils {

	//================================================================================
	// Constructors
	//================================================================================
	private TestUtils() {}

	//================================================================================
	// Static Methods
	//================================================================================
	public static void forceInitFX() {
		PlatformImpl.startup(() -> {});
	}

	public static <T> T getProperty(Object obj, String property) {
		String getter = ReflectionUtils.resolveGetter(property);
		return Reflect.on(obj).call(getter).get();
	}
}