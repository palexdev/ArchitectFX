/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.architectfx.backend.yaml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.deps.DynamicClassLoader;
import io.github.palexdev.architectfx.backend.enums.Type;
import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.backend.model.Entity;
import io.github.palexdev.architectfx.backend.model.Property;
import io.github.palexdev.architectfx.backend.model.config.Config;
import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.backend.utils.Tuple2;
import io.github.palexdev.architectfx.backend.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.yaml.YamlTreeIterator.TreeEntry;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.backend.utils.CastUtils.*;
import static io.github.palexdev.architectfx.backend.yaml.Tags.*;

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
    protected DependencyManager dm;
    private ClassScanner scanner;
    private Reflector reflector;
    private YamlParser parser;
    private boolean parallel;

    private final List<Entity> loadQueue = new ArrayList<>();
    private final Map<Entity, SequencedMap<String, Object>> propertiesMap = new IdentityHashMap<>();
    private final Map<String, Object> controllerFields = new HashMap<>();
    private Entity current;

    private Function<Class<?>, Object> controllerFactory;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlDeserializer() {
        this(false);
    }

    public YamlDeserializer(boolean parallel) {
        dm = new DependencyManager();
        scanner = new ClassScanner(dm);
        reflector = new Reflector(dm, scanner);
        parser = new YamlParser(this, scanner, reflector);
        this.parallel = parallel;
    }

    public YamlDeserializer(Function<YamlDeserializer, YamlDeserializerConfig> config) {
        YamlDeserializerConfig c = config.apply(this);
        this.dm = c.dm();
        this.scanner = c.scanner();
        this.reflector = c.reflector();
        this.parser = c.parser();
        this.parallel = c.parallel();
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Entry point and first phase of the deserialization process.
    ///
    /// This is responsible for parsing the dependencies, the imports, global configs and instantiating the controller.
    /// But most importantly, instantiates the tree nodes before returning the document. Depending on the configuration
    /// these action may be executed sequentially or asynchronously.
    ///
    /// If the document appears to be malformed, issues a warning but still tries to parse it.
    ///
    /// @throws IOException if the YAML map is empty
    /// @see #buildTree(Entity, Entry)
    /// @see #buildTreeConcurrent(Entry)
    public Document parseDocument(SequencedMap<String, Object> map) throws Exception {
        if (map.isEmpty())
            throw new IOException("Failed to parse document because it appears to be empty");

        // Handle dependencies if present
        List<String> dependencies = parser.parseDependencies(map);
        if (!dependencies.isEmpty()) {
            Logger.info("Found {} dependencies:\n{}", dependencies.size(), dependencies);
            dm.addDeps(dependencies.toArray(String[]::new));
        }

        // Handle imports if present
        List<String> imports = parser.parseImports(map);
        if (!imports.isEmpty()) {
            scanner.setImports(imports);
            Logger.info("Found {} imports:\n{}", imports.size(), imports);
        }

        // Handle the controller if present
        CompletableFuture<Object> controller = parser.parseController(map)
            .map(name -> {
                Callable<Object> task = () -> {
                    try {
                        Logger.debug("Trying to instantiate the controller {}", name);
                        Class<?> klass = scanner.findClass(name);
                        return Optional.ofNullable(controllerFactory)
                            .map(f -> f.apply(klass))
                            .orElseGet(() -> reflector.create(klass));
                    } catch (IllegalArgumentException | ClassNotFoundException ex) {
                        Logger.error("Failed to instantiate the controller because:\n{}", ex);
                    }
                    return null;
                };
                return parallel ? Async.call(task) : Async.wrap(task);
            })
            .orElse(Async.EMPTY_FUTURE);

        // Handle global configs if present
        Optional<Object> _tags = Optional.ofNullable(map.remove(CONFIG_TAG));
        Runnable configsTask = () -> {
            Optional<Object> cObj = Optional.empty();
            List<Config> globalConfigs = _tags.map(parser::parseConfigs).orElseGet(List::of);
            for (Config c : globalConfigs) {
                cObj = c.run(cObj.orElse(null));
            }
        };
        if (parallel) Async.run(configsTask);
        else configsTask.run();

        if (map.size() > 1)
            Logger.warn(
                "Document is probably malformed. {} root nodes detected, trying to parse anyway...",
                map.size()
            );

        // Create entities recursive
        Entity root = parallel ? buildTreeConcurrent(map.firstEntry()) : buildTree(null, map.firstEntry());
        Document document = new Document(root, controller.get());
        document.dependencies().addAll(dependencies);
        document.imports().addAll(imports);
        return document;
    }

    /// This should be called after [#parseDocument(SequencedMap)], which means after the tree nodes have been
    /// instantiated and queued for initialization by [#buildTree(Entity, Entry)].
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
    /// This is responsible for building the tree structure from the load queue generated during the tree instantiation
    /// phase using the [Entity#parent()] information.
    ///
    /// At the end of this operation the load queue is cleared.
    public void linkTree() throws IOException {
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
            for (Entry<String, Object> e : controllerFields.entrySet()) {
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
    private Entity buildTree(Entity parent, Entry<String, Object> entry) throws IOException {
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

            Entity childEntity = buildTree(entity, childMap.firstEntry());
            entity.children().add(childEntity);
        }

        // 3.Save properties for later
        if (!map.isEmpty()) {
            Logger.trace("Saving properties for later:\n{}", map);
            propertiesMap.put(entity, map);
        }

        return entity;
    }

    /// The most heavy operation when loading a YAML tree is actually instantiating its nodes. Since during these phase
    /// we still do not initialize them with their properties, we can parallelize the job by iterating over all the nodes
    /// and running the instantiation in separate threads.
    ///
    /// An issue in doing so is that unlike the sequential, recursive version [#buildTree(Entity, Entry)], we can't
    /// link the children nodes to the respective parents. For this reason, the methods is organized in three phases:
    /// 1) During the first phase it iterates over the tree in pre-order by using [YamlTreeIterator], builds all the tasks
    /// which will instantiate the nodes and finally sends all of them at once to an executor with [Async#callAll(Collection)].
    /// Before proceeding to the second phase we must wait for all objects to be created, so we also block the thread with
    /// [Async#await(Collection)].
    /// It's worth noting that since [Entity] cannot be created without the parent entity, it wraps the type and the instance
    /// in [Tuple2] objects. These are stored in a map of type `[TreeEntry -> Tuple2]`.
    /// 2) The second phase is responsible for re-linking the tree's nodes. To avoid re-iterating over the entire tree,
    /// the [YamlTreeIterator] has been optimized to register the relationships between parents and their children.
    /// A simple loop on [YamlTreeIterator#relationships()] creates all the entities and links them.
    /// 3) Another issue with this approach is that we can't create the load queue until all nodes have been instantiated.
    /// Before returning the root entity, the load queue is filled with all the entities created during phase two.
    ///
    /// Despite the complexity, this approach can still reveal to be much faster than instantiating objects sequentially.
    private Entity buildTreeConcurrent(Entry<String, Object> root) throws IOException {
        // Instantiates all nodes asynchronously
        YamlTreeIterator it = new YamlTreeIterator(root);
        List<Callable<Void>> tasks = new ArrayList<>();
        Map<TreeEntry, Tuple2<String, Object>> tuples = new HashMap<>();
        while (it.hasNext()) {
            TreeEntry next = it.next();
            tasks.add(() -> {
                String type = next.entry().getKey();
                SequencedMap<String, Object> map = asYamlMap(next.entry().getValue());
                Object[] args = parser.parseArgs(map);
                Logger.debug("Creating object of type {} with args:\n{}", type, Arrays.toString(args));
                Object instance = map.containsKey(FACTORY_TAG) ? parser.handleFactory(map, args) : reflector.create(type, args);
                if (instance == null) {
                    throw new IOException("Failed to instantiate object for type " + type);
                }
                synchronized (tuples) {
                    tuples.put(next, Tuple2.of(type, instance));
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = Async.callAll(tasks);
            Async.await(futures);
        } catch (Exception ex) {
            throw new IOException("Failed to build tree concurrently", ex);
        }

        // Re-link the tree's nodes
        SequencedMap<TreeEntry, Entity> entities = new LinkedHashMap<>();

        // Root handling (needs to be handled here for empty relationships)
        Tuple2<String, Object> rTuple = tuples.get(it.root());
        Entity rEntity = new Entity(null, rTuple.a(), rTuple.b());
        entities.put(it.root(), rEntity);
        propertiesMap.put(rEntity, asYamlMap(it.root().entry().getValue()));

        for (Entry<TreeEntry, List<TreeEntry>> relationship : it.relationships().entrySet()) {
            // Handle parent
            TreeEntry parent = relationship.getKey();
            Entity pEntity = entities.get(parent);
            if (pEntity == null) {
                Tuple2<String, Object> tuple = tuples.get(parent);
                pEntity = new Entity(null, tuple.a(), tuple.b());
                entities.put(parent, pEntity);
                propertiesMap.put(pEntity, asYamlMap(parent.entry().getValue()));
            }

            // Handle children
            for (TreeEntry child : relationship.getValue()) {
                Tuple2<String, Object> tuple = tuples.get(child);
                Entity cEntity = new Entity(pEntity, tuple.a(), tuple.b());
                pEntity.children().add(cEntity);
                entities.put(child, cEntity);
                propertiesMap.put(cEntity, asYamlMap(child.entry().getValue()));
            }
        }

        // Fill the load queue
        loadQueue.addAll(entities.values());

        // Finally return the root node
        return entities.firstEntry().getValue();
    }

    /// Adds the given child instance to the given parent instance.
    ///
    /// This method is specific to JavaFX. Used by [#linkTree()].
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

    /// Delegate for [ClassScanner#addToScanCache(Class\[\])].
    public YamlDeserializer addToScanCache(Class<?>... classes) {
        scanner.addToScanCache(classes);
        return this;
    }

    /// Delegate for [ClassScanner#setDocumentPath(Path)].
    public YamlDeserializer setDocumentPath(Path path) {
        scanner.setDocumentPath(path);
        return this;
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

    /// @return the function used to create the document's controller
    public Function<Class<?>, Object> getControllerFactory() {
        return controllerFactory;
    }

    /// Sets the function used to create the document's controller.
    public YamlDeserializer setControllerFactory(Function<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
        return this;
    }

    /// @return whether the deserialization process is going to be split across multiple threads
    public boolean isParallel() {
        return parallel;
    }

    /// Sets whether the deserialization process is going to be split across multiple threads
    public YamlDeserializer setParallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

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

    //================================================================================
    // Inner Classes
    //================================================================================

    /// This record allows to easily configure the dependencies and settings of a [YamlDeserializer].
    public record YamlDeserializerConfig(
        DependencyManager dm,
        ClassScanner scanner,
        Reflector reflector,
        YamlParser parser,
        boolean parallel
    ) {
        public YamlDeserializerConfig(DependencyManager dm, ClassScanner scanner, Reflector reflector, YamlParser parser) {
            this(dm, scanner, reflector, parser, false);
        }
    }
}
