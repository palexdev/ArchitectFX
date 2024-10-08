package io.github.palexdev.architectfx.model.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.reflection.Reflector;
import io.github.palexdev.architectfx.yaml.Tags;
import io.github.palexdev.architectfx.yaml.YamlParser;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.Tags.METHOD_TAG;
import static io.github.palexdev.architectfx.yaml.Tags.TRANSFORM_TAG;

/// Concrete implementation of [Config] which is specifically designed to run static and instance methods.
///
/// Besides the common properties inherited by [Config], this also stores the `args` array needed to run a certain method,
/// and a peculiar boolean flag which is further discussed here [#parse(YamlParser, SequencedMap)] and here [#run(Object)].
///
/// The parsing of a `MethodConfig` from YAML is done by [#parse(YamlParser, SequencedMap)].
public class MethodConfig extends Config {
    //================================================================================
    // Properties
    //================================================================================
    private final Object[] args;
    private boolean transform;

    //================================================================================
    // Constructors
    //================================================================================
    public MethodConfig(Class<?> owner, String member, Object... args) {
        super(owner, member);
        this.args = args;
    }

    //================================================================================
    // Static Methods
    //================================================================================

    /// This method is responsible for parsing a `MethodConfig` from YAML given:
    /// 1) The [YamlParser] which, as the name suggests, is the core class responsible for parsing YAML
    /// 2) The `map` containing the config's parameters
    ///
    /// - The owner and method name are extracted from the [Tags#METHOD_TAG] and parsed using [Reflector#getMethodInfo(Object)]
    /// - The args are entirely handled and parsed by [YamlParser#parseArgs(SequencedMap)]
    /// - The `transform` flag is extracted from the [Tags#TRANSFORM_TAG] and parsed by [Boolean#parseBoolean(String)].
    /// To know what this flag does, refer to [#run(Object)]
    ///
    /// If anything goes wrong with the parsing, returns an empty [Optional].
    protected static Optional<MethodConfig> parse(YamlParser parser, SequencedMap<String, Object> map) {
        Tuple2<Class<?>, String> mInfo = parser.reflector().getMethodInfo(map.get(METHOD_TAG));
        if (mInfo == null) {
            Logger.warn("Method config does not specify the {} tag\n{}", METHOD_TAG, map);
            return Optional.empty();
        }

        Object[] args = parser.parseArgs(map);
        boolean transform = Optional.ofNullable(map.get(TRANSFORM_TAG))
            .map(o -> Boolean.parseBoolean(o.toString()))
            .orElse(false);
        return Optional.of(new MethodConfig(mInfo.a(), mInfo.b(), args).transform(transform));
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Calls the instance method for the given `obj` with the previously parsed [#args()].
    /// 
    /// If the `transform` flag is true returns the method's result wrapped in an [Optional].
    /// 
    /// @see Reflect#on(Object) 
    /// @see Reflect#call(String, Object...) 
    @SuppressWarnings("unchecked")
    protected <T> Optional<T> runStandard(Object obj) throws ReflectException {
        Logger.debug("Running config {} on standard method {} with args {}", name(), member, Arrays.toString(args));
        T ret = Reflect.on(obj)
            .call(member, args)
            .get();
        return Optional.ofNullable(transform ? ret : (T) obj);
    }

    /// Calls the static method on the parsed [#owner()] with the parsed [#args()].
    ///
    /// If the `transform` flag is true returns the method's result wrapped in an [Optional].
    ///
    /// @see Reflect#onClass(Class)
    /// @see Reflect#call(String, Object...)
    @SuppressWarnings("unchecked")
    protected <T> Optional<T> runStatic(Object obj) throws ReflectException {
        Logger.debug("Running config {} on static method {} with args {}", name(), member, Arrays.toString(args));
        T ret = Reflect.onClass(owner)
            .call(member, args)
            .get();
        return Optional.ofNullable(transform ? ret : (T) obj);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// The actual logic to execute the configuration is not here.
    ///
    /// The presence of the [#owner()] class determines whether the method we want to execute is a static or an instance
    /// method. For this reason, we handle the two cases differently. Respectively, this may delegate to:
    /// [#runStatic(Object)] or [#runStandard(Object)].
    ///
    /// The `transform` flag
    ///
    /// The config uses this flag to basically decide what to do with the result of a method.
    /// In case it's true, the config will return an [Optional] wrapping the result of the executed method, otherwise
    /// returns the wrapped input.
    ///
    /// In other words, this simple mechanism allows supporting fluent APIs, factories, and builders.
    /// The only downside is that the user must be very careful as the system does not perform any check on the invoked method.
    @Override
    public <T> Optional<T> run(Object obj) {
        try {
            return owner != null ? runStatic(obj) : runStandard(obj);
        } catch (ReflectException ex) {
            Logger.error("Failed to execute config:\n{}", ex);
        }
        return Optional.empty();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    /// @return the arguments needed to run the method
    public Object[] args() {
        return args;
    }

    /// @return whether the config should return the method's result once executed
    public boolean isTransform() {
        return transform;
    }

    /// Sets whether the config should return the method's result once executed
    public MethodConfig transform(boolean transform) {
        this.transform = transform;
        return this;
    }
}
