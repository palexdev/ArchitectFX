package misc;

import java.io.IOException;
import java.io.InputStream;

import com.sun.javafx.application.PlatformImpl;
import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.backend.utils.CastUtils;
import io.github.palexdev.architectfx.backend.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.backend.yaml.YamlLoader;
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

    public static <T> T load(InputStream stream, Class<T> klass) throws IOException {
        try (YamlLoader loader = new YamlLoader()) {
            return CastUtils.as(loader.load(stream).rootNode(), klass);
        }
    }

    public static Document load(InputStream stream) throws IOException {
        try (YamlLoader loader = new YamlLoader()) {
            return loader.load(stream);
        }
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