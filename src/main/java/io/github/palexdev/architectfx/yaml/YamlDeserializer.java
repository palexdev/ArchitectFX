package io.github.palexdev.architectfx.yaml;

import java.io.IOException;
import java.util.*;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.deps.DynamicClassLoader;
import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;
import static io.github.palexdev.architectfx.yaml.Tags.*;

/// Core part of the system specifically designed for the deserialization. The actual parsing of YAML data is delegated
/// to the [YamlParser]. This separation makes the code more readable and maintainable.
///
/// The deserialization process requires many dependencies:
/// - The [DependencyManager] which handles third-party libraries and a way to load their classes thanks to the
/// [DynamicClassLoader]
/// - The [ClassScanner] used to search for classes
/// - The [Reflector] which handles any reflection related task
/// - The [YamlParser]
///
/// This is the class that executes the phases described by [YamlLoader].
public class YamlDeserializer {
    //================================================================================
    // Properties
    //================================================================================
    private DependencyManager dm;
    private ClassScanner scanner;
    private Reflector reflector;
    private YamlParser parser;

    private final List<Entity> loadQueue = new ArrayList<>();
    private final Map<Entity, SequencedMap<String, Object>> propertiesMap = new HashMap<>();
    private final Map<String, Object> controllerFields = new HashMap<>();
    private Entity current;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlDeserializer() {
        dm = new DependencyManager();
        scanner = new ClassScanner(dm);
        reflector = new Reflector(dm, scanner);
        parser = new YamlParser(this, scanner, reflector);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Entry point and first phase of the deserialization process.
    ///
    /// This is responsible for parsing the dependencies, the imports, global configs and instantiating the controller.
    /// But most importantly, calls [#createEntity(Entity, Map.Entry)] to recursively instantiate the tree nodes before
    /// returning the document.
    ///
    /// @throws IOException if the YAML map is empty
    /// @implNote issues a warning if the document seems malformed, but still tries to parse it
    public Document parseDocument(SequencedMap<String, Object> map) throws IOException {
        if (map.isEmpty())
            throw new IOException("Failed to parse document because it appears to be empty");

        // Handle dependencies if present
        List<String> dependencies = parser.parseDependencies(map);
        if (!dependencies.isEmpty()) {
            Logger.info("Found {} dependencies:\n{}", dependencies.size(), dependencies);
            dm.addDeps(dependencies.toArray(String[]::new));
            dm.refresh(false);
        }

        // Handle imports if present
        List<String> imports = parser.parseImports(map);
        if (!imports.isEmpty()) {
            scanner.setImports(imports);
            Logger.info("Found {} imports:\n{}", imports.size(), imports);
        }

        // Handle the controller if present
        Object controller = null;
        try {
            String name = parser.parseController(map);
            if (name != null) {
                Logger.debug("Trying to instantiate the controller {}", name);
                Class<?> klass = scanner.findClass(name);
                controller = reflector.create(klass);
            }
        } catch (IllegalArgumentException | ClassNotFoundException ex) {
            Logger.error("Failed to instantiate the controller because:\n{}", ex);
        }

        // Handle global configs if present
        Optional<Object> cObj = Optional.empty();
        List<Config> globalConfigs = Optional.ofNullable(map.remove(CONFIG_TAG))
            .map(parser::parseConfigs)
            .orElseGet(List::of);
        for (Config c : globalConfigs) {
            cObj = c.run(cObj.orElse(null));
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

    /// This should be called after [#parseDocument(SequencedMap)], which means after the tree nodes have been
    /// instantiated and queued for initialization by [#createEntity(Entity, Map.Entry)].
    ///
    /// This method is the second phase described here [YamlLoader]. All the properties previously collected for an
    /// [Entity] in the queue are parsed by [YamlParser#parseProperties(SequencedMap)] and then set on the entity's instance
    /// by [#initialize(Object, Collection)].
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

    /// This method should be called after [#initializeTree()]. It's the third phase described here [YamlLoader].
    ///
    /// This is responsible for building the tree structure from the load queue generated by [#createEntity(Entity, Map.Entry)]
    /// using the [Entity#parent()] information.
    ///
    /// At the end of this operation the load queue is cleared.
    public void buildTree() throws IOException {
        // The queue is top-down (flattened tree structure)
        for (Entity entity : loadQueue) {
            Object currentInstance = entity.instance();
            if (entity.parent() != null) {
                Object parentInstance = entity.parent().instance();
                attachToParent(parentInstance, currentInstance);
            }
        }
        loadQueue.clear();
    }

    /// This method is the forth phase described here [YamlLoader]. It's responsible for injecting fields into the
    /// controller instance and for calling `initialize()` on it.
    ///
    /// @see Reflect#set(String, Object)
    /// @see Reflect#call(String)
    public void handleController(Document document) {
        Object controller = document.controller();
        if (controller != null) {
            // Populate controller
            for (Map.Entry<String, Object> e : controllerFields.entrySet()) {
                String name = e.getKey();
                Object value = e.getValue();
                try {
                    Reflect.on(controller).set(name, value);
                } catch (ReflectException ex) {
                    Logger.error("Failed to inject ID {} into controller", name);
                }
            }

            // Call initialize if present
            try {
                Reflect.on(controller).call("initialize");
            } catch (ReflectException ex) {
                Logger.warn("Controller initialization failed because:\n{}", ex);
            }
        }
    }

    /// Given an [Entity]'s instance and its properties, sets the latter on the first as follows:
    /// - Metadata: parsed configs ([Config]) are run on the instance; controller ids are saved for later; other kinds of
    /// metadata are skipped
    /// - Primitives, wrappers, strings and enums are set by [Reflector#setProperty(Object, Property)]
    /// - Complex types ([Type#COMPLEX]) are first parsed by [YamlParser#parseComplexValue(SequencedMap)] and then set
    /// by [Reflector#setProperty(Object, Property)]
    /// - Collections are first parsed by [YamlParser#parseList(Object)] and then set by [Reflector#addToCollection(Object, String, List)]
    protected void initialize(Object instance, Collection<Property> properties) {
        Logger.debug("Initializing object {}", Objects.toString(instance));
        for (Property p : properties) {
            String name = p.name();
            Logger.trace("Handling property: {}", p);

            // Handle metadata
            switch (name) {
                case CONFIG_TAG -> {
                    List<Config> configs = asList(p.value(), Config.class);
                    Optional<Object> res = Optional.of(instance);
                    for (Config config : configs) {
                        if (res.isEmpty())
                            throw new IllegalStateException("Something went wrong, cannot run config on null instance");
                        res = config.run(res.get());
                    }
                    continue;
                }
                case CONTROLLER_ID -> {
                    controllerFields.put((String) p.value(), instance);
                    continue;
                }
            }

            switch (p.type()) {
                case METADATA -> Logger.trace("Skipping metadata {}...", name);
                case PRIMITIVE, WRAPPER, STRING, ENUM -> Reflector.setProperty(instance, p);
                case COMPLEX -> {
                    Logger.trace("Parsing property's complex value...");
                    Object value = parser.parseComplexValue(asYamlMap(p.value()));
                    Reflector.setProperty(instance, Property.of(p.name(), p.type(), value));
                }
                case COLLECTION -> {
                    Logger.trace("Parsing property's collection value...");
                    List<Object> list = parser.parseList(asGenericList(p.value()));
                    Reflector.addToCollection(instance, p.name(), list);
                }
            }
        }
    }

    /// This method is responsible for creating a new [Entity] given its parent, its name and properties (these last two
    /// are contained in the entry's value which is expected to be a YAML map). The parent is null for the root node.
    ///
    /// Before creating the entity object, first it creates the relative type object by using the constructor or factory
    /// (if the [Tags#FACTORY_TAG] tag is specified) and the arguments parsed by [YamlParser#parseArgs(SequencedMap)].
    /// Once instantiated, the entity is added to the load queue.
    ///
    /// If the entity has children (among its properties) this method is invoked recursively on each child and added to
    /// [Entity#children()].
    ///
    /// At the end, the remaining properties are saved for later in a map `[Entity -> Properties]`. These will be handled
    /// at initialization time ([#initializeTree()]).
    /// 
    /// @see YamlParser#handleFactory(SequencedMap, Object[]) 
    private Entity createEntity(Entity parent, Map.Entry<String, Object> entry) throws IOException {
        // 1.Create the object and add entity to load queue
        String type = entry.getKey();
        SequencedMap<String, Object> map = asYamlMap(entry.getValue());
        Object[] args = parser.parseArgs(map);
        Logger.debug("Creating object of type {} with args:\n{}", type, Arrays.toString(args));
        Object instance = map.containsKey(FACTORY_TAG) ? parser.handleFactory(map, args) : reflector.create(type, args);
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

    /// Adds the given child instance to the given parent instance.
    ///
    /// This method is specific to JavaFX. Used by [#buildTree()].
    private void attachToParent(Object pInstance, Object cInstance) throws IOException {
        // TODO this may need to be improved by allowing to add nodes in Parent classes too
        try {
            Pane pane = (Pane) pInstance;
            Node node = (Node) cInstance;
            pane.getChildren().add(node);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /// Cleans all references and data structures used by the deserializer.
    public void dispose() {
        scanner.dispose();
        reflector.dispose();
        parser.dispose();

        dm = null;
        scanner = null;
        reflector = null;
        parser = null;

        loadQueue.clear();
        propertiesMap.clear();
        controllerFields.clear();

        current = null;
    }

    //================================================================================
    // Getters
    //================================================================================

    /// @return the [ClassScanner] instance used by the deserializer
    public ClassScanner getScanner() {
        return scanner;
    }

    /// @return the loading queue as an unmodifiable list
    public List<Entity> queue() {
        return Collections.unmodifiableList(loadQueue);
    }

    /// @return the current [Entity] being initialized
    public Entity currentLoading() {
        return current;
    }
}
