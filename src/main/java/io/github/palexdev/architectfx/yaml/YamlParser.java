package io.github.palexdev.architectfx.yaml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.utils.CastUtils;
import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.Tuple3;
import io.github.palexdev.architectfx.utils.VarArgsHandler;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner.ScanScope;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;
import static io.github.palexdev.architectfx.yaml.Tags.*;

/// Core part of the system specifically designed for translating YAML to code or objects.
///
/// The parsed has the following dependencies:
/// - The [YamlDeserializer] because some operations are delegated to it, or some specific data is needed from it
/// - The [ClassScanner] used to find classes and resources
/// - The [Reflector] which handles any reflection related task
///
/// I already said this, but the parser truly is the core of this system. Basically any feature, from primitive types support,
/// to collections, objects and configs are possible thanks to the work done by this guy here.
public class YamlParser {
    //================================================================================
    // Properties
    //================================================================================
    private YamlDeserializer deserializer;
    private ClassScanner scanner;
    private Reflector reflector;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlParser(YamlDeserializer deserializer, ClassScanner scanner, Reflector reflector) {
        this.deserializer = deserializer;
        this.scanner = scanner;
        this.reflector = reflector;
    }

    //================================================================================
    // Methods
    //================================================================================

    /// This method is responsible for parsing a generic map of properties from YAML to a map of [Property].
    ///
    /// Here's the order in which YAML data is handled:
    /// 1) First it handles metadata by checking the property's name with [Type#isMetadata(String)].
    /// In such case, args and varargs are parsed by [#parseList(Object)], while configs are parsed by [#parseConfigs(Object)].
    /// For any other type of metadata, simply returns the entry's value. Of course the property's type is set to [Type#METADATA].
    /// 2) Then it checks for static fields by using [#parseStaticField(Object, boolean)] (enums allowed).
    /// In such case, the type is given by the first element in the [Tuple2], and the value by the second element.
    /// 3) In any other case the value is the entry's value and the type is determined by [Property#getPropertyType(String, Object)]
    /// on the value.
    ///
    /// In case either the type or value are null, issues a warning and skips the property.
    @SuppressWarnings("unchecked")
    public SequencedMap<String, Property> parseProperties(SequencedMap<String, Object> map) {
        if (map == null || map.isEmpty()) return new LinkedHashMap<>();

        Logger.info("Parsing properties...");
        SequencedMap<String, Property> properties = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String name = e.getKey();
            Type type;
            Object value;

            // Handle metadata
            if (Type.isMetadata(name)) {
                value = switch (name) {
                    case ARGS_TAG, VARARGS_TAG -> parseList(e.getValue()).toArray();
                    case CONFIG_TAG -> parseConfigs(e.getValue());
                    default -> e.getValue();
                };
                type = Type.METADATA;
            } else if ((value = parseStaticField(e.getValue(), true)) != null) {
                Tuple2<Type, Object> tuple = (Tuple2<Type, Object>) value;
                type = tuple.a();
                value = tuple.b();
            } else {
                value = e.getValue();
                type = Property.getPropertyType(name, value);
            }

            if (type == null || value == null) {
                Logger.warn("Skipping property: {}:{}:{}", name, type, value);
                continue;
            }

            Property property = Property.of(name, type, value);
            properties.put(name, property);
            Logger.debug("Parsed property {}", property);
        }
        return properties;
    }

    /// This method is responsible for parsing each value in the given YAML list.
    /// If the `obj` parameter is not a list, issues an error and returns an empty list.
    ///
    /// Since elements in the YAML list can be of any type they need to be parsed. For example, if the list contains
    /// static variables or complex types, the need to be resolved/instantiated. The parsing of each element is delegated
    /// to [#parseValue(Object)]. For now, `null` elements are skipped.
    ///
    /// @return a list of parsed values
    @SuppressWarnings("unchecked")
    public <T> List<T> parseList(Object obj) {
        if (!(obj instanceof List<?>)) {
            Logger.error("Expected a list but got {}", obj.getClass());
            return List.of();
        }

        List<Object> list = asGenericList(obj);
        List<Object> parsed = new ArrayList<>();
        Logger.debug("Parsing each element of list:\n{}", list);
        for (int i = 0; i < list.size(); i++) {
            Logger.trace("Parsing element at index {}", i);
            Tuple2<Type, Object> val = parseValue(list.get(i));
            // TODO may be good to introduce null support
            if (val == null) {
                Logger.warn("Skipping null element...");
                continue;
            }
            parsed.add(val.b());
        }
        return (List<T>) parsed;
    }

    /// Core method responsible for parsing a generic YAML `obj` to a known type. Here's the parsing order:
    /// 1) Keywords have the priority over everything else, they are parsed by [#parseKeyword(Object)]
    /// 2) Complex types (see [Type#COMPLEX]) are parsed by [#parseComplexValue(SequencedMap)]
    /// 3) String are special because they could indicate resources if they start by [ClassScanner#RESOURCES_PREFIX].
    /// In such case [ClassScanner#findResource(String, ScanScope)] is used to find the right URL resource.
    /// Otherwise, the string value is returned
    /// 4) Primitives and wrappers, there are returned as they are
    /// 5) Collections, which are parsed by [#parseList(Object)]
    ///
    /// @return `null` for unknown types or a [Tuple2] wrapping the element type and value
    @SuppressWarnings("unchecked")
    public Tuple2<Type, Object> parseValue(Object obj) {
        Object value;
        if ((value = parseStaticField(obj, true)) != null) {
            Tuple2<Type, Object> tuple = (Tuple2<Type, Object>) value;
            Logger.debug("Parsed static value {}:\n{}", obj, tuple);
            if (Type.KEYWORD == tuple.a()) {
                Logger.debug("Value is a keyword...");
                Object parsed = parseKeyword(tuple.b());
                return (parsed != null) ? Tuple2.of(tuple.a(), parsed) : null;
            }
            return tuple;
        }

        Type type = Type.getType(obj);
        value = switch (type) {
            case COMPLEX -> {
                SequencedMap<String, Object> map = asYamlMap(obj);
                Logger.debug("Parsing complex value:\n{}", map);
                yield parseComplexValue(map);
            }
            case STRING -> {
                String s = ((String) obj);
                Logger.debug("Value {} is of type: {}\nChecking whether is is a resource...", s, Type.STRING);
                if (s.startsWith(ClassScanner.RESOURCES_PREFIX)) {
                    yield Optional.ofNullable(scanner.findResource(s.substring(1), ScanScope.DEPS))
                        .flatMap(u -> {
                            try {
                                return Optional.of(u.toURL());
                            } catch (MalformedURLException ex) {
                                Logger.error(ex);
                            }
                            return Optional.empty();
                        })
                        .map(URL::toExternalForm)
                        .orElse(null);
                }
                yield s;
            }
            case PRIMITIVE, WRAPPER -> {
                Logger.debug("Value {} is either of type {} or {} or {}",
                    Objects.toString(obj), Type.PRIMITIVE, Type.WRAPPER, Type.STRING
                );
                yield obj;
            }
            case COLLECTION -> {
                Logger.debug("Value {} is of type: {}", Objects.toString(obj), Type.COLLECTION);
                yield parseList(obj);
            }
            default -> {
                Logger.error("Unsupported value type {}", obj.getClass().getName());
                yield null;
            }
        };
        return (value == null) ? null : new Tuple2<>(type, value);
    }

    /// This is responsible for parsing configurations ([Config]) from a generic YAML `obj` which is expected to be a list
    /// of YAML maps (see [CastUtils#asYamlMap(Object)]).
    ///
    /// Each element in the list is parsed by [Config#parse(YamlParser, Object)] and only if the process was successfull
    /// it is added to the list.
    public List<Config> parseConfigs(Object obj) {
        if (!(obj instanceof List<?>)) {
            Logger.error("Expected a list of configs but got {}", obj.getClass());
        }
        List<Object> list = asGenericList(obj);
        List<Config> parsed = new ArrayList<>();
        Logger.debug("Parsing each config of list:\n{}", list);
        for (int i = 0; i < list.size(); i++) {
            Logger.trace("Parsing config at index {}", i);
            Object cObj = list.get(i);
            Optional<? extends Config> config = Config.parse(this, cObj);
            config.ifPresent(parsed::add);
        }
        return parsed;
    }

    /// In a way this method is similar to [YamlDeserializer#createEntity(Entity, Map.Entry)]. This is responsible for
    /// instantiating and initializing complex properties (see [Type#COMPLEX]) from a generic YAML map (see [CastUtils#asYamlMap(Object)]).
    ///
    /// First it instantiates the object by:
    /// 1) Determining its type by checking the [Tags#TYPE_TAG] tag
    /// 2) Parsing the arguments using [#parseArgs(SequencedMap)]
    /// 3) Invoking the constructor of the factory specified by the [Tags#FACTORY_TAG] tag
    ///
    /// Then:
    /// 1) Parses the properties with [#parseProperties(SequencedMap)]
    /// 2) Initializes the object by calling [YamlDeserializer#initialize(Object, Collection)]
    /// 3) Finally returns the created object
    ///
    /// @see #handleFactory(SequencedMap, Object[])
    protected Object parseComplexValue(SequencedMap<String, Object> map) {
        /*
         * We need to clone the map because of the following remove operations
         * We ideally do not want to alter the original map
         *
         * The removals happen so that we avoid the parseProperties step if not necessary.
         * An example of this would be metadata such as .type and .args which are already used here
         *
         * For metadata like .config we rely on YamlDeserializer.initialize(...)
         */
        SequencedMap<String, Object> copy = new LinkedHashMap<>(map);
        String type = (String) copy.remove(TYPE_TAG);
        if (type == null) {
            Logger.error("Expected {} tag for complex type not found", TYPE_TAG);
            return null;
        }

        // Parse args and create instance
        Object[] args = parseArgs(copy);
        Object instance = copy.containsKey(FACTORY_TAG) ? handleFactory(copy, args) : reflector.create(type, args);

        // Init properties
        if (instance != null) {
            Logger.debug("Parsing properties for complex type...");
            SequencedMap<String, Property> properties = parseProperties(copy);
            Logger.trace("Properties:\n{}", properties);
            deserializer.initialize(instance, properties.values());
        }
        return instance;
    }

    /// This method is responsible for invoking factories or running [Tags#FACTORY_TAG] configs.
    ///
    /// Given a map of YAML properties and the args to use for the call (if present), extract the factory from it and
    /// depending on its type, two things can happen:
    /// 1) The factory is a String, delegates to [Reflector#invokeFactory(String, Object...)] and returns
    /// 2) The factory is a List of configs ([Config]), which in this case is more appropriate to call them steps.
    /// After calling [#parseConfigs(Object)], each is run with the instance returned by the previous.
    /// If the resulting object is `null` issues an error and returns `null`.
    ///
    /// In other words, there are two types of factories:
    /// 1) Simple factories, a single static method with ot without arguments that produce the desired object
    /// 2) Complex factories/builders, which rely on fluent API, method chaining to instantiate and configure the desired
    /// object. This type is supported through configs.
    protected Object handleFactory(SequencedMap<String, Object> map, Object[] args) {
        Object oFactory = map.remove(FACTORY_TAG);
        if (oFactory instanceof String s) {
            return reflector.invokeFactory(s, args);
        }

        if (oFactory instanceof List<?> l) {
            List<Config> steps = parseConfigs(l);
            Optional<Object> instance = Optional.empty();
            for (Config step : steps) {
                instance = step.run(instance.orElse(null));
            }

            if (instance.isEmpty())
                Logger.error("Factory block is probably invalid, no object was created...\n{}", oFactory);
            return instance.orElse(null);
        }

        Logger.error("Factory is of unexpected type:\n{}", oFactory);
        return null;
    }

    /// Given a YAML map, extracts both the args ([Tags#ARGS_TAG]) and varargs ([Tags#VARARGS_TAG]) from it (if present),
    /// parses them with [#parseList(Object)] and finally combines them into a single array thanks to
    /// [VarArgsHandler#combine(Object\[\], Object)].
    ///
    /// Note that to generate the varargs array, [VarArgsHandler#generateArray(List)] is used after the parsing.
    public Object[] parseArgs(SequencedMap<String, Object> map) {
        Object[] args = Optional.ofNullable(map.remove(ARGS_TAG))
            .map(this::parseList)
            .map(List::toArray)
            .orElseGet(() -> new Object[0]);
        Object varArgs = Optional.ofNullable(map.remove(VARARGS_TAG))
            .map(this::parseList)
            .map(VarArgsHandler::generateArray)
            .orElse(null);
        return VarArgsHandler.combine(args, varArgs);
    }

    /// Convenience method which reduces the result of [Reflector#getFieldInfo(Object, boolean)] to a [Tuple2].
    /// The only parameter we need here is the last one, the value. The new [Tuple2] record wraps the value's type,
    /// determined by using [Type#getType(Object)], and the value itself.
    ///
    /// If either the field info or the wrapped class are `null`, returns `null`.
    private Tuple2<Type, Object> parseStaticField(Object obj, boolean allowEnum) {
        Tuple3<Class<?>, String, Object> fInfo = reflector.getFieldInfo(obj, allowEnum);
        if (fInfo == null || fInfo.a() == null) return null;
        return Tuple2.of(
            Type.getType(fInfo.c()),
            fInfo.c()
        );
    }

    /// This method is specifically responsible for transforming [Keyword] objects into something appropriate.
    /// For example, the keyword [Keyword#THIS] tells the system that something needs the reference to the current loading
    /// entity (see [YamlDeserializer#createEntity(Entity, Map.Entry)]).
    private Object parseKeyword(Object obj) {
        if (Keyword.THIS == obj) {
            return deserializer.currentLoading().instance();
        }
        Logger.error("Unknown keyword: {}", Objects.toString(obj));
        return null;
    }

    /// Responsible for collecting the document's dependencies under the [Tags#DEPS_TAG] tag.
    protected List<String> parseDependencies(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(DEPS_TAG))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    /// Responsible for collecting the document's imports under the [Tags#IMPORTS_TAG] tag.
    protected List<String> parseImports(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(IMPORTS_TAG))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    /// Responsible for retrieving the document's controller name under the [Tags#CONTROLLER_TAG] tag, or `null` if absent.
    protected String parseController(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(CONTROLLER_TAG))
            .filter(String.class::isInstance)
            .map(o -> as(o, String.class))
            .orElse(null);
    }

    /// Clears all references to its dependencies.
    public void dispose() {
        deserializer = null;
        scanner = null;
        reflector = null;
    }

    //================================================================================
    // Getters
    //================================================================================

    /// @return the [Reflector] instance used here
    public Reflector reflector() {
        return reflector;
    }
}
