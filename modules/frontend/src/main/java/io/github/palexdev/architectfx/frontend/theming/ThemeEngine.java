package io.github.palexdev.architectfx.frontend.theming;

import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import io.github.palexdev.mfxcomponents.theming.UserAgentBuilder;
import io.inverno.core.annotation.Bean;

@Bean
public class ThemeEngine {

    /*
     * TODO implement this, for now it does the bare minimum to debug the UI
     */

    public void loadTheme() {
        UserAgentBuilder.builder()
            .themes(MaterialThemes.INDIGO_LIGHT)
            .themes(VFXTheme.SCROLL_PANE)
            .themes(AppTheme.DEFAULT)
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal();
    }
}
