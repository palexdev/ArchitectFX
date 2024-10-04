package io.github.palexdev.architectfx.yaml;

import java.util.*;

import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.Tuple3;
import io.github.palexdev.architectfx.utils.VarArgsHandler;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;
import static io.github.palexdev.architectfx.yaml.Tags.*;

public class YamlParser {
    //================================================================================
    // Properties
    //================================================================================
    private YamlDeserializer deserializer;
    private Reflector reflector;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlParser(YamlDeserializer deserializer, Reflector reflector) {
        this.deserializer = deserializer;
        this.reflector = reflector;
    }

    //================================================================================
    // Methods
    //================================================================================

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
            case PRIMITIVE, WRAPPER, STRING -> {
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

    protected Object parseComplexValue(SequencedMap<String, Object> map) {
        // We need to clone the map because of the following remove operations
        // We ideally do not want to alter the original map
        //
        // The removals happen so that we avoid the parseProperties step if not necessary.
        // An example of this would be metadata such as .type and .args which are already used here
        //
        // For metadata like .config we rely on YamlDeserializer.initialize(...)
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

    protected Object handleFactory(SequencedMap<String, Object> map, Object[] args) {
        Object oFactory = map.remove(FACTORY_TAG);
        if (oFactory instanceof String s) {
            return reflector.invokeFactory(s, args);
        } else if (oFactory instanceof List<?> l) {
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


    private Tuple2<Type, Object> parseStaticField(Object obj, boolean allowEnum) {
        Tuple3<Class<?>, String, Object> fInfo = reflector.getFieldInfo(obj, allowEnum);
        if (fInfo == null || fInfo.a() == null) return null;
        return Tuple2.of(
            Type.getType(fInfo.c()),
            fInfo.c()
        );
    }

    private Object parseKeyword(Object obj) {
        if (Keyword.THIS == obj) {
            return deserializer.currentLoading().instance();
        }
        Logger.error("Unknown keyword: {}", Objects.toString(obj));
        return null;
    }

    protected List<String> parseDependencies(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(DEPS_TAG))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    protected List<String> parseImports(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(IMPORTS_TAG))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    protected String parseController(SequencedMap<String, Object> map) {
        return Optional.ofNullable(map.remove(CONTROLLER_TAG))
            .filter(String.class::isInstance)
            .map(o -> as(o, String.class))
            .orElse(null);
    }

    public void dispose() {
        deserializer = null;
        reflector = null;
    }

    //================================================================================
    // Getters
    //================================================================================
    public Reflector reflector() {
        return reflector;
    }
}
