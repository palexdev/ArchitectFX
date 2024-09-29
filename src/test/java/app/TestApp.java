package app;

import java.net.URL;

import io.github.palexdev.architectfx.yaml.YamlLoader;

public class TestApp {

    public static void main(String[] args) throws Exception {
        URL res = TestApp.class.getClassLoader().getResource("assets/TextFields.yaml");
        YamlLoader.instance().load(res);
    }
}
