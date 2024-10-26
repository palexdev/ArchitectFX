package io.github.palexdev.architectfx.frontend.settings;

import java.util.Map;

import io.github.palexdev.architectfx.frontend.model.Recents;
import io.github.palexdev.mfxcore.settings.NumberSetting;
import io.github.palexdev.mfxcore.settings.Settings;
import io.github.palexdev.mfxcore.settings.StringSetting;
import io.inverno.core.annotation.Bean;
import javafx.application.Application;

@Bean
public class AppSettings extends Settings {
    //================================================================================
    // Settings
    //================================================================================
    private final NumberSetting<Double> windowWidth = registerDouble("window.width", "", 1024.0);
    private final NumberSetting<Double> windowHeight = registerDouble("window.height", "", 768.0);
    private final StringSetting recents = registerString("recents", "YAML string that contains recently opened documents", "");

    private final Application.Parameters parameters;
    private Boolean resetSettings = null;

    //================================================================================
    // Constructors
    //================================================================================
    public AppSettings(Application.Parameters parameters) {
        this.parameters = parameters;
    }

    //================================================================================
    // Methods
    //================================================================================
    public Recents loadRecents() {
        String yaml = recents.get();
        return Recents.load(yaml);
    }

    public void saveRecents(Recents recents) {
        String toYaml = recents.save();
        this.recents.set(toYaml);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected String node() {
        return "/io/github/palexdev/architectfx";
    }

    //================================================================================
    // Getters
    //================================================================================
    public NumberSetting<Double> windowWidth() {
        return windowWidth;
    }

    public NumberSetting<Double> windowHeight() {
        return windowHeight;
    }

    public boolean isResetSettings() {
        if (resetSettings == null) {
            Map<String, String> named = parameters.getNamed();
            resetSettings = Boolean.parseBoolean(named.getOrDefault("reset-settings", "false"));
        }
        return resetSettings;
    }
}
