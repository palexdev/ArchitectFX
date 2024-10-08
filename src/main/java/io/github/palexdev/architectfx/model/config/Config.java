package io.github.palexdev.architectfx.model.config;

import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.utils.CastUtils;
import io.github.palexdev.architectfx.yaml.Tags;
import io.github.palexdev.architectfx.yaml.YamlParser;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.Tags.FIELD_TAG;
import static io.github.palexdev.architectfx.yaml.Tags.METHOD_TAG;

/// Configs are a special kind of metadata which make the system much more flexible.
///
/// They can appear in different places:
/// 1) At document-level, which means outside any node. These are considered global-configs
/// 2) In factories, see [YamlParser#handleFactory(SequencedMap, Object\[\])]
/// 3) In "top-level" nodes, can be very useful to run extra config on instantiated nodes, see [Entity]
/// 4) In complex values, which means that even properties which expect objects as values can have config steps,
/// see [Type]
///
/// This however, is a base class for two kinds of configs: [FieldConfig] and [MethodConfig].
///
/// So, what does this base class offers?
/// 1) Common properties of course. Each config refers to some field or method of an object. So, [#owner)] refers
/// to the class containing the field/method. In case this is `null`, it means that the target is not static
/// 2) The [#member] value is the name of the field/method
/// 3) This is also the entry point for parsing the various kind of configs, see [#parse(YamlParser, Object)]
/// 4) An abstract method which executes the configuration, [#run(Object)]
public abstract class Config {
    //================================================================================
    // Properties
    //================================================================================
    protected final Class<?> owner;
    protected final String member;

    //================================================================================
    // Constructors
    //================================================================================
    protected Config(Class<?> owner, String member) {
        this.owner = owner;
        this.member = member;
    }

    //================================================================================
    // Static Methods
    //================================================================================

    /// This method is an entry point to parse the given `obj` into a `Config` (field or method).
    ///
    /// The `obj` parameter is expected to be a YAML map (see [CastUtils#asYamlMap(Object)]) and must contain the tag
    /// indicating which type of config we are dealing with, currently it's either [Tags#FIELD_TAG] or [Tags#METHOD_TAG].
    /// Depending on the tag, this delegates to [FieldConfig#parse(YamlParser, Object)] or [MethodConfig#parse(YamlParser, Object)].
    public static Optional<? extends Config> parse(YamlParser parser, Object obj) {
        if (!(obj instanceof SequencedMap<?, ?>)) {
            Logger.error("Expected map for config but found: {}", obj.getClass());
            return Optional.empty();
        }

        Logger.debug("Parsing config: {}", Objects.toString(obj));
        SequencedMap<String, Object> map = CastUtils.asYamlMap(obj);
        if (map.containsKey(FIELD_TAG)) return FieldConfig.parse(parser, map);
        if (map.containsKey(METHOD_TAG)) return MethodConfig.parse(parser, map);

        Logger.error("Config does not specify {} tag nor {} tag", FIELD_TAG, METHOD_TAG);
        return Optional.empty();
    }

    //================================================================================
    // Abstract Methods
    //================================================================================

    /// This method should be used by implementations to execute the configuration. Accepts the object to configure
    /// and can return anything wrapped in a [Optional].
    public abstract <T> Optional<T> run(Object obj);

    //================================================================================
    // Getters
    //================================================================================

    /// Convenience method to combine the [#owner] and the [#member] properties. If the [#owner] is `null` just returns
    /// [#member].
    public String name() {
        return owner != null ?
            owner.getName() + "." + member :
            member;
    }

    /// @return the class containing the field/method specified by [#member], `null` if it's not static
    public Class<?> owner() {
        return owner;
    }

    /// @return the field/method to set/execute for the configuration
    public String member() {
        return member;
    }
}
