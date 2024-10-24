package misc;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TestController {
    //================================================================================
    // Properties
    //================================================================================
    private VBox box;
    private Label label1;
    private Label label2;
    private Button btn;

    private boolean fxText = false;

    //================================================================================
    // Methods
    //================================================================================
    private void initialize() {
        box.setAlignment(Pos.CENTER);
        box.setSpacing(20);

        label1.setText("This is a test to check the controller functionality");
        updateLabel();

        // TODO support event handling in yaml!
        btn.setOnAction(e -> updateLabel());
    }

    private void updateLabel() {
        if (!fxText) {
            String javaVersion = System.getProperty("java.version");
            String javafxVersion = System.getProperty("javafx.version");
            label2.setText("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
            fxText = true;
        } else {
            label2.setText("Hello, world! 😎");
            fxText = false;
        }
    }
}
