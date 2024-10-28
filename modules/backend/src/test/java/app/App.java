package app;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.github.palexdev.architectfx.backend.yaml.YamlLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        long start = System.nanoTime();
        URL res = Launcher.class.getClassLoader().getResource("assets/TextFields.jdsl");

        Parent root;
        try (YamlLoader loader = new YamlLoader()) {
            root = loader.setParallel(true)
                .load(res)
                .rootNode();
            long end = System.nanoTime();
            long elapsed = end - start;
            long converted = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
            System.out.println("Elapsed: " + converted + "ms");
        }

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
