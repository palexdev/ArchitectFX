package app;

import java.util.concurrent.TimeUnit;

import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        long start = System.nanoTime();

        Parent root = (Parent) new JUIFXLoader().load(
            App.class.getClassLoader().getResource("assets/TextFields.jui")
        ).root();

        long end = System.nanoTime();
        long elapsed = end - start;
        System.out.println("Elapsed: " + TimeUnit.NANOSECONDS.toMillis(elapsed) + "ms");

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
