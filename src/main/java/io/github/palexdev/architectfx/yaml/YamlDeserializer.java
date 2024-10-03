package io.github.palexdev.architectfx.yaml;

import java.io.IOException;
import java.util.*;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.ReflectionUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;
import static io.github.palexdev.architectfx.yaml.Tags.CONFIG_TAG;

public class YamlDeserializer {
    //================================================================================
    // Properties
    //================================================================================
    private final YamlParser parser = new YamlParser(this);
    private final List<Entity> loadQueue = new ArrayList<>();
    private final Map<Entity, SequencedMap<String, Object>> propertiesMap = new HashMap<>();

    private Entity current;

    //================================================================================
    // Methods
    //================================================================================
    public Document parseDocument(SequencedMap<String, Object> map) throws IOException {
        if (map.isEmpty())
            throw new IOException("Failed to parse document because it appears to be empty");

        // Handle dependencies if present
        List<String> dependencies = parser.parseDependencies(map);
        if (!dependencies.isEmpty()) {
            Logger.info("Found {} dependencies:\n{}", dependencies.size(), dependencies);
            DependencyManager.instance()
                .addDeps(dependencies.toArray(String[]::new))
                .refresh();
        }

        // Handle imports if present
        List<String> imports = parser.parseImports(map);
        if (!imports.isEmpty()) {
            // FIXME ClassScanner should not be static, imports are specific per document
            ClassScanner.setImports(imports);
            Logger.info("Found {} imports:\n{}", imports.size(), imports);
        }

        // Handle the controller if present
        Object controller = null;
        try {
            String name = parser.parseController(map);
            if (name != null) {
                Logger.debug("Trying to instantiate the controller {}", name);
                Class<?> klass = ClassScanner.findClass(name);
                controller = ReflectionUtils.create(klass);
            }
        } catch (ClassNotFoundException ex) {
            Logger.error("Failed to instantiate the controller because:\n{}", ex);
        }

        if (map.size() > 1)
            Logger.warn(
                "Document is probably malformed. {} root nodes detected, trying to parse anyway...",
                map.size()
            );

        // Create entities recursive
        Entity root = createEntity(null, map.firstEntry());
        Document document = new Document(root, controller);
        document.dependencies().addAll(dependencies);
        document.imports().addAll(imports);
        return document;
    }

    public void initializeTree() {
        // Initialize all nodes
        for (Entity entity : loadQueue) {
            current = entity;
            SequencedMap<String, Property> properties = parser.parseProperties(propertiesMap.get(entity));
            if (properties != null) entity.properties().putAll(properties);
            initialize(entity.instance(), entity.properties().values());
        }
        current = null;
    }

    public Parent buildSceneGraph(Document document) throws IOException {
        // The queue is top-down (flattened tree structure)
        for (Entity entity : queue()) {
            Object currentInstance = entity.instance();
            if (entity.parent() != null) {
                Object parentInstance = entity.parent().instance();
                attachToParent(parentInstance, currentInstance);
            }
        }
        loadQueue.clear();
        return (Parent) document.root().instance();
    }

    protected void initialize(Object instance, Collection<Property> properties) {
        Logger.debug("Initializing object {}", Objects.toString(instance));
        for (Property p : properties) {
            String name = p.name();
            Logger.trace("Handling property: {}", p);

            // Handle metadata (for now only .config is relevant)
            if (name.equals(CONFIG_TAG)) {
                List<Config> configs = asList(p.value(), Config.class);
                Optional<Object> res = Optional.of(instance);
                for (Config config : configs) {
                    if (res.isEmpty())
                        throw new IllegalStateException("Something went wrong, cannot run config on null instance");
                    res = config.run(res.get());
                }
                continue;
            }

            switch (p.type()) {
                case METADATA -> Logger.trace("Skipping metadata...");
                case PRIMITIVE, WRAPPER, STRING, ENUM -> ReflectionUtils.setProperty(instance, p);
                case COMPLEX -> {
                    Logger.trace("Parsing property's complex value...");
                    Object value = parser.parseComplexValue(asYamlMap(p.value()));
                    ReflectionUtils.setProperty(instance, Property.of(p.name(), p.type(), value));
                }
                case COLLECTION -> {
                    Logger.trace("Parsing property's collection value...");
                    List<Object> list = parser.parseList(asGenericList(p.value()));
                    ReflectionUtils.addToCollection(instance, p.name(), list);
                }
            }
        }
    }

    private Entity createEntity(Entity parent, Map.Entry<String, Object> entry) throws IOException {
        // 1.Create the object and add entity to load queue (TODO factories/builders support)
        String type = entry.getKey();
        SequencedMap<String, Object> map = asYamlMap(entry.getValue());
        Object[] args = parser.parseArgs(map);
        Logger.debug("Creating object of type {} with args:\n{}", type, Arrays.toString(args));
        Object instance = ReflectionUtils.create(type, args);
        if (instance == null) {
            throw new IOException("Failed to instantiate object for type " + type);
        }

        Entity entity = new Entity(parent, type, instance);
        loadQueue.add(entity);

        // 2.Handle children recursive
        List<?> children = Optional.ofNullable(map.remove("children"))
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .orElseGet(List::of);
        Logger.info("Found {} children, parsing...", children.size());
        for (Object child : children) {
            SequencedMap<String, Object> childMap = asYamlMap(child);
            if (childMap.size() != 1) {
                Logger.warn("Expected size 1 for child map, found {}", childMap.size());
            }

            Entity childEntity = createEntity(entity, childMap.firstEntry());
            entity.children().add(childEntity);
        }

        // 3.Save properties for later
        if (!map.isEmpty()) {
            Logger.trace("Saving properties for later:\n{}", map);
            propertiesMap.put(entity, map);
        }

        return entity;
    }

    private void attachToParent(Object pInstance, Object cInstance) throws IOException {
        try {
            Pane pane = (Pane) pInstance;
            Node node = (Node) cInstance;
            pane.getChildren().add(node);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    //================================================================================
    // Getters
    //================================================================================
    public List<Entity> queue() {
        return Collections.unmodifiableList(loadQueue);
    }

    public Entity currentLoading() {
        return current;
    }
}
