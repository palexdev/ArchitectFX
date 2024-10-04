package misc;

import com.sun.javafx.application.PlatformImpl;
import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.yaml.YamlParser;
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
            new YamlDeserializer(),
            new Reflector(dm, scanner)
        );
    }

    public static <T> T getProperty(Object obj, String property) {
        String getter = Reflector.resolveGetter(property);
        return Reflect.on(obj).call(getter).get();
    }
}