package io.github.palexdev.architectfx.backend.utils.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.utils.CastUtils;

/// API for handling various types of Java collections with reflection. It mainly does two things:
/// 1) Ensures that both the target collection and the given one are of the same type
/// 2) Can append to the target, optionally can also clear the target before
///
/// There are two concrete implementations:
/// - [GenericCollectionHandler]
/// - [MapHandler]
public sealed interface CollectionHandler permits CollectionHandler.GenericCollectionHandler, CollectionHandler.MapHandler {

    /// Implementations should try retrieving a collection on the given target by the given name and handle accordingly
    /// to the type.
    void handle(Object target, String name, Object value, boolean clear);

    /// Given a [CollectionType], returns the appropriate handler.
    static CollectionHandler handlerFor(CollectionType type) {
        if (type == CollectionType.MAP) return MapHandler.INSTANCE;
        GenericCollectionHandler.INSTANCE.expectedType = type.klass();
        return GenericCollectionHandler.INSTANCE;
    }

    //================================================================================
    // Inner Classes
    //================================================================================

    /// Implementation of [CollectionHandler] which can handle generic collections inheriting from [Collection].
    ///
    /// Because of this, and for laziness, when this is returned by [CollectionHandler#handlerFor(CollectionType)] it also
    /// sets the internal field [#expectedType] and reset at the end of the [#handle(Object, String, Object, boolean)]
    /// method. Not elegant, but functional nonetheless.
    final class GenericCollectionHandler implements CollectionHandler {
        private static final GenericCollectionHandler INSTANCE = new GenericCollectionHandler();
        private Class<?> expectedType;

        private GenericCollectionHandler() {}

        @Override
        public void handle(Object target, String name, Object value, boolean clear) {
            try {
                List<?> src = Reflector.checkTypes(target, name, value, expectedType);
                if (src == null) {
                    Setter.write(target, name, value);
                } else {
                    if (clear) src.clear();
                    src.addAll(CastUtils.unchecked(value));
                }
            } finally {
                expectedType = null;
            }
        }
    }

    /// Implementation of [CollectionHandler] to specifically handle [Map].
    final class MapHandler implements CollectionHandler {
        private static final MapHandler INSTANCE = new MapHandler();

        private MapHandler() {}

        @Override
        public void handle(Object target, String name, Object value, boolean clear) {
            Map<?, ?> src = Reflector.checkTypes(target, name, value, Map.class);
            if (src == null) {
                Setter.write(target, name, value);
            } else {
                if (clear) src.clear();
                src.putAll(CastUtils.unchecked(value));
            }
        }
    }
}
