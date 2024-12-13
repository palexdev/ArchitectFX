package misc;

import java.util.List;

import io.github.palexdev.architectfx.backend.loaders.jui.JUIBaseLoader;

public class DummyLoader<T> extends JUIBaseLoader<T> {
    @Override
    public void attachChildren(T parent, List<T> children) {}
}
