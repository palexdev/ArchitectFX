package misc;

import com.sun.javafx.application.PlatformImpl;
import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.backend.yaml.YamlParser;
import org.joor.Reflect;

public class TestUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private TestUtils() {
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static void forceInitFX() {
        PlatformImpl.startup(() -> {
        });
    }

    public static YamlParser parser() {
        DependencyManager dm = new DependencyManager();
        ClassScanner scanner = new ClassScanner(dm);
        return new YamlParser(
            new YamlDeserializer(false),
            scanner,
            new Reflector(dm, scanner)
        );
    }

    public static <T> T getProperty(Object obj, String property) {
        String getter = Reflector.resolveGetter(property);
        return Reflect.on(obj).call(getter).get();
    }
}