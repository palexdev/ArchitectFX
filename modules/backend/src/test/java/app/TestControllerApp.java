package app;

import java.io.ByteArrayInputStream;

import io.github.palexdev.architectfx.backend.yaml.YamlLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestControllerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String document = """
            .imports: [
              "javafx.scene.control.Label"
            ]
            
            .controller: TestController
            
            VBox:
              .cid: "box"
              children:
                - Label:
                    .cid: "label1"
                - Label:
                    .cid: "label2"
                - Button:
                    .cid: "btn"
                    .args: ["Change Text"]
            """;

        Parent root = new YamlLoader().load(new ByteArrayInputStream(document.getBytes())).rootNode();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
