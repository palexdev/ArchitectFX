package app;

import java.net.URL;

import io.github.palexdev.architectfx.yaml.YamlLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL res = Launcher.class.getClassLoader().getResource("assets/TextFields.jdsl");
        Parent root = new YamlLoader().load(res).rootNode();

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
