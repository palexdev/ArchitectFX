package unit;

import static io.github.palexdev.architectfx.utils.CastUtils.as;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.sun.javafx.application.PlatformImpl;
import org.junit.jupiter.api.Test;

import io.github.palexdev.architectfx.yaml.YamlLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class TestLoader {

    @Test
    void testSimpleLoad() {
        String document = """
                imports: [
                  "javafx.geometry.*",
                  "javafx.scene.layout.*"
                ]

                GridPane:
                  alignment: "CENTER"
                  hgap: 20.0
                  vgap: 20.0
                  padding: { type: "Insets", args: [20.0, 30.0, 20.0, 30.0] }
                  styleClass:
                    - "grid-pane"
                  stylesheets:
                    - "../css/TextFields.css"

                  columnConstraints: [
                    { type: ColumnConstraints, halignment: "CENTER" },
                    { type: ColumnConstraints, halignment: "CENTER" },
                    { type: ColumnConstraints, halignment: "CENTER" },
                    { type: ColumnConstraints, halignment: "CENTER" },
                    { type: ColumnConstraints, halignment: "CENTER" },
                    { type: ColumnConstraints, halignment: "CENTER" }
                  ]
                  rowConstraints: [
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 10.0 },
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                    { type: "RowConstraints", minHeight: 10.0, prefHeight: 150.0, valignment: "BASELINE" }
                  ]
                """;

        try {
            InputStream stream = new ByteArrayInputStream(document.getBytes());
            GridPane root = as(YamlLoader.instance().load(stream), GridPane.class);

            assertEquals(Pos.CENTER, root.getAlignment());
            assertEquals(20.0, root.getHgap());
            assertEquals(20.0, root.getVgap());
            assertEquals(new Insets(20.0, 30.0, 20.0, 30.0), root.getInsets());
            assertEquals(1, root.getStyleClass().size());
            assertTrue(root.getStyleClass().contains("grid-pane"));
            assertEquals(1, root.getStylesheets().size());
            assertTrue(root.getStylesheets().contains("../css/TextFields.css"));

            assertEquals(6, root.getColumnConstraints().size());
            for (ColumnConstraints cc : root.getColumnConstraints()) {
                assertEquals(HPos.CENTER, cc.getHalignment());
            }

            assertEquals(6, root.getRowConstraints().size());
            double[] params = new double[] { 32.0, 64.0, 64.0, 10.0, 32.0, 150.0 };
            for (int i = 6; i < 6; i++) {
                RowConstraints rc = root.getRowConstraints().get(i);
                assertEquals(10.0, rc.getMinHeight());
                assertEquals(params[i], rc.getPrefHeight());
                if (i == 5)
                    assertEquals(VPos.BASELINE, rc.getValignment());
            }
        } catch (AssertionError | Exception ex) {
            ex.printStackTrace();
            fail(ex);
        }
    }

    @Test
    void testLoadWithoutDepsAndImports() {
        String document = """
        GridPane:
          alignment: "CENTER"
          hgap: 20.0
          vgap: 20.0
          padding: { type: "Insets", args: [20.0, 30.0, 20.0, 30.0] }
          styleClass:
            - "grid-pane"
          stylesheets:
            - "../css/TextFields.css"

          columnConstraints: [
            { type: ColumnConstraints, halignment: "CENTER" },
            { type: ColumnConstraints, halignment: "CENTER" },
            { type: ColumnConstraints, halignment: "CENTER" },
            { type: ColumnConstraints, halignment: "CENTER" },
            { type: ColumnConstraints, halignment: "CENTER" },
            { type: ColumnConstraints, halignment: "CENTER" }
          ]
          rowConstraints: [
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 10.0 },
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
            { type: "RowConstraints", minHeight: 10.0, prefHeight: 150.0, valignment: "BASELINE" }
          ]
        """;

        try {
            InputStream stream = new ByteArrayInputStream(document.getBytes());
            GridPane root = as(YamlLoader.instance().load(stream), GridPane.class);
            assertNotNull(root);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex);
        }
    }

    @Test
    void testLoadWithoutImports() {
        // Force initialize JavaFX toolkit
        PlatformImpl.startup(() -> {});

        // TODO implement factories (static method to create objects in YAML format)
        // Test by setting the MFXFontIcon's color
        String document = """
        deps: ["io.github.palexdev:materialfx:11.17.0"]
        MFXButton:
          alignment: "CENTER"
          padding: { type: "Insets", args: [10.0] }
          graphic: { type: "MFXFontIcon", args: ["fas-user"] }
          text: "This is a MaterialFX's Button"
        """;

        try {
            InputStream stream = new ByteArrayInputStream(document.getBytes());
            Object obj = YamlLoader.instance().load(stream);

            // TODO test attributes
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex);
        }
    }
}