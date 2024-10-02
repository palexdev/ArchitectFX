package unit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.utils.CastUtils;
import io.github.palexdev.architectfx.yaml.YamlLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static io.github.palexdev.architectfx.utils.CastUtils.as;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.TestUtils.getProperty;

public class TestLoader {

    @Test
    void testSimpleLoad() throws IOException {
        String document = """
            .imports: [
              "javafx.geometry.*",
              "javafx.scene.layout.*"
            ]
            
            GridPane:
              alignment: "Pos.CENTER"
              hgap: 20.0
              vgap: 20.0
              padding: { .type: "Insets", .args: [20.0, 30.0, 20.0, 30.0] }
              styleClass:
                - "grid-pane"
              stylesheets:
                - "../css/TextFields.css"
            
              columnConstraints: [
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" }
              ]
              rowConstraints: [
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 10.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 150.0, valignment: "VPos.BASELINE" }
              ]
            """;


        InputStream stream = new ByteArrayInputStream(document.getBytes());
        GridPane root = as(new YamlLoader().load(stream), GridPane.class);

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
        double[] params = new double[]{32.0, 64.0, 64.0, 10.0, 32.0, 150.0};
        for (int i = 0; i < 6; i++) {
            RowConstraints rc = root.getRowConstraints().get(i);
            assertEquals(10.0, rc.getMinHeight());
            assertEquals(params[i], rc.getPrefHeight());
            if (i == 5)
                assertEquals(VPos.BASELINE, rc.getValignment());
        }
    }

    @Test
    void testLoadWithoutDepsAndImports() throws IOException {
        String document = """
            GridPane:
              alignment: "Pos.CENTER"
              hgap: 20.0
              vgap: 20.0
              padding: { .type: "Insets", .args: [20.0, 30.0, 20.0, 30.0] }
              styleClass:
                - "grid-pane"
              stylesheets:
                - "../css/TextFields.css"
            
              columnConstraints: [
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" },
                { .type: ColumnConstraints, halignment: "HPos.CENTER" }
              ]
              rowConstraints: [
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 64.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 10.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 32.0 },
                { .type: "RowConstraints", minHeight: 10.0, prefHeight: 150.0, valignment: "VPos.BASELINE" }
              ]
            """;

        InputStream stream = new ByteArrayInputStream(document.getBytes());
        GridPane root = as(new YamlLoader().load(stream), GridPane.class);

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
        double[] params = new double[]{32.0, 64.0, 64.0, 10.0, 32.0, 150.0};
        for (int i = 0; i < 6; i++) {
            RowConstraints rc = root.getRowConstraints().get(i);
            assertEquals(10.0, rc.getMinHeight());
            assertEquals(params[i], rc.getPrefHeight());
            if (i == 5)
                assertEquals(VPos.BASELINE, rc.getValignment());
        }
    }

    @Test
    void testLoadWithoutImports() throws IOException {
        // We need to clean for this one because MFXComponents clashes with older versions of the MaterialFX
        DependencyManager.instance().cleanDeps();

        // Almost without
        // There are several classes named Color
        TestUtils.forceInitFX();
        String document = """
            .deps: [
              "io.github.palexdev:mfxcomponents:11.26.1",
            ]
            .imports: ["javafx.scene.paint.Color"]
            MFXButton:
              .args: ["This is a MaterialFX's Button"]
              alignment: "Pos.CENTER"
              padding: { .type: "Insets", .args: [10.0] }
              graphic: {
                .type: "MFXFontIcon", .args: ["fas-user"],
                color: { .type: "Color", .factory: "Color.web", .args: ["#845EC2"] }
              }
              .config: [
                { .method: filled }
              ]
            """;

        InputStream stream = new ByteArrayInputStream(document.getBytes());
        Object obj = new YamlLoader().load(stream);
        assertEquals(Pos.CENTER, getProperty(obj, "alignment"));
        assertEquals(new Insets(10.0), getProperty(obj, "padding"));
        assertEquals("This is a MaterialFX's Button", getProperty(obj, "text"));

        // Test graphic
        Object graphic = getProperty(obj, "graphic");
        assertEquals("MFXFontIcon", graphic.getClass().getSimpleName());
        assertEquals("fas-user", getProperty(graphic, "description"));
        assertEquals(Color.web("#845EC2"), getProperty(graphic, "color"));

        // Test variant
        List<String> styleClass = CastUtils.asList(getProperty(obj, "styleClass"), String.class);
        assertTrue(styleClass.contains("filled"));
    }

    @Test
    void testThirdParty() throws IOException {
        // We need to clean for this one because MFXComponents clashes with older versions of the MaterialFX
        DependencyManager.instance().cleanDeps();

        // Almost without
        // There are several classes named Color
        TestUtils.forceInitFX();
        String document = """
            .deps: [
              "io.github.palexdev:mfxcomponents:11.26.1",
            ]
            .imports: ["javafx.scene.paint.Color"]
            MFXButton:
              .args: [
                "This is a MaterialFX's Button",
                {
                  .type: "MFXFontIcon", .args: ["fas-user"],
                  color: {.type: "Color", .factory: "Color.web", .args: ["#845EC2"]}
                }
              ]
              alignment: "Pos.CENTER"
              padding: { .type: "Insets", .args: [10.0] }
              .config: [
                { .method: setVariants, .varargs: [ButtonVariants.FILLED, ButtonVariants.TEXT] }
              ]
            """;

        InputStream stream = new ByteArrayInputStream(document.getBytes());
        Object obj = new YamlLoader().load(stream);
        assertEquals(Pos.CENTER, getProperty(obj, "alignment"));
        assertEquals(new Insets(10.0), getProperty(obj, "padding"));
        assertEquals("This is a MaterialFX's Button", getProperty(obj, "text"));

        // Test graphic
        Object graphic = getProperty(obj, "graphic");
        assertEquals("MFXFontIcon", graphic.getClass().getSimpleName());
        assertEquals("fas-user", getProperty(graphic, "description"));
        assertEquals(Color.web("#845EC2"), getProperty(graphic, "color"));

        // Test variant
        List<String> styleClass = CastUtils.asList(getProperty(obj, "styleClass"), String.class);
        assertTrue(styleClass.contains("filled"));
        assertTrue(styleClass.contains("text"));
    }
}