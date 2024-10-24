package io.github.palexdev.architectfx.backend.yaml;

import java.util.*;
import java.util.Map.Entry;

import org.tinylog.Logger;

import static io.github.palexdev.architectfx.backend.utils.CastUtils.asYamlMap;
import static io.github.palexdev.architectfx.backend.yaml.YamlTreeIterator.*;

/// Implementation of [Iterator] which can traverse YAML trees in pre-order.
/// Each entry in the tree is of type `Entry<String, Object>`, but the iterator wraps them in [TreeEntry] for two reasons:
/// 1) Aliasing: writing `Entry<String, Object>` everywhere is annoying
/// 2) They are used as keys in a map, but we need to check for identity rather than equality
///
/// *What do you mean by that?*
///
/// Well, you see, when traversing a tree as we do here, an important piece of information is lost: relationships.
/// The iterator starts from the root element and uses a `cursor` to advance to the next elements. Before an element is
/// returned [#extractChildren(TreeEntry)] retrieves the element's children and adds them to the sequence. In other words,
/// the tree structure is being flattened as you advance with [#next()]. Because of this, we can't tell anymore who is
/// the parent and who are the children.
///
/// To avoid this loss of information (which is quite important for performance reasons), every time children are extracted
/// from an element they are saved in a map as \[Parent -> Children] -> \[TreeEntry -> List<TreeEntry>].
/// Relationships can be retrieved from [#relationships()].
///
/// Since this uses a cursor to advance in the sequence, you are allowed to reuse the iterator by resetting it, [#reset()].
public class YamlTreeIterator implements Iterator<TreeEntry> {
    //================================================================================
    // Properties
    //================================================================================
    private final TreeEntry root;
    private final List<TreeEntry> elements = new ArrayList<>();
    private final Map<TreeEntry, List<TreeEntry>> relationships = new LinkedHashMap<>();
    private int cursor = 0;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlTreeIterator(Entry<String, Object> root) {
        this.root = TreeEntry.of(root);
        elements.add(this.root);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// @return the tree's root wrapped in a [TreeEntry]
    public TreeEntry root() {
        return root;
    }

    /// @return the map which holds the relationships between parents and children:
    /// \[Parent -> Children] -> \[TreeEntry -> List<TreeEntry>]
    public Map<TreeEntry, List<TreeEntry>> relationships() {
        return relationships;
    }

    /// Resets the cursor to 0, thus leading [#next()] to return the root element again.
    public void reset() {
        cursor = 0;
    }

    /// Given an element of the YAML tree as a [TreeEntry] extracts its children, and only if they are present:
    /// - Adds them to the iterator's sequence
    /// - Registers the parent-children relationship in the [#relationships()] map
    ///
    /// @see #next()
    protected void extractChildren(TreeEntry entry) {
        try {
            SequencedMap<String, Object> map = asYamlMap(entry.entry().getValue());
            List<?> children = Optional.ofNullable(map.remove("children"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .orElse(null);
            if (children == null) return;

            List<TreeEntry> tmp = new ArrayList<>();
            for (Object child : children) {
                SequencedMap<String, Object> childMap = asYamlMap(child);
                if (childMap.size() != 1) {
                    Logger.warn("Expected size 1 for child map, found {}", childMap.size());
                }
                TreeEntry childEntry = TreeEntry.of(childMap.firstEntry());
                elements.add(childEntry);
                tmp.add(childEntry);
            }
            relationships.put(entry, tmp);
        } catch (Exception ex) {
            Logger.error("Failed to push children to elements during tree traversal");
        }
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// @return whether the iterator's cursor is lesser than the number of elements in the sequence
    @Override
    public boolean hasNext() {
        return cursor < elements.size();
    }

    /// {@inheritDoc}
    ///
    /// Retrieves the next element if present, but before returning it, calls [#extractChildren(TreeEntry)] to add its
    /// children to the sequence and register the relationship.
    @Override
    public TreeEntry next() {
        if (!hasNext())
            throw new NoSuchElementException();
        TreeEntry entry = elements.get(cursor++);
        extractChildren(entry);
        return entry;
    }

    //================================================================================
    // Internal Classes
    //================================================================================

    /// Simple wrapper for a YAML entries.
    ///
    /// Serves two purposes:
    /// - Acts as an alias for `Entry<String, Object>
    /// - Makes all maps behave as [IdentityHashMap] because `equals()` is overridden to check for identity rather than
    /// equality
    public record TreeEntry(Entry<String, Object> entry) {

        public static TreeEntry of(Entry<String, Object> entry) {
            return new TreeEntry(entry);
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(entry);
        }
    }
}
