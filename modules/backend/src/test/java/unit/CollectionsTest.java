package unit;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import misc.CollectionsTestClass;
import misc.DummyLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionsTest {

    @Test
    void testCollectionsSet() throws IOException {
        String doc = """
            CollectionsTestClass {
              ARRAY: int[10, 11]
              ARRAY_IG: Integer[99]
              ARRAY_CHAR: char['z', 'x', 'y']
              ARRAY_BOOLEAN: boolean[true, false]
              ARRAY_MIXED: Object[1, 'a', "Str", 5.0, false]
              list = listOf (20, 30, 40)
              map = mapOf (
                1, "1",
                2, "2",
                10, "10"
              )
            }
            """;
        CollectionsTestClass root = new DummyLoader<CollectionsTestClass>()
            .load(new ByteArrayInputStream(doc.getBytes()), null)
            .root();

        assertEquals(2, CollectionsTestClass.ARRAY.length);
        assertArrayEquals(new int[]{10, 11}, CollectionsTestClass.ARRAY);

        assertEquals(1, CollectionsTestClass.ARRAY_IG.length);
        assertArrayEquals(new Integer[]{99}, CollectionsTestClass.ARRAY_IG);

        assertEquals(3, CollectionsTestClass.ARRAY_CHAR.length);
        assertArrayEquals(new char[]{'z', 'x', 'y'}, CollectionsTestClass.ARRAY_CHAR);

        assertEquals(2, CollectionsTestClass.ARRAY_BOOLEAN.length);
        assertArrayEquals(new boolean[]{true, false}, CollectionsTestClass.ARRAY_BOOLEAN);

        assertEquals(5, CollectionsTestClass.ARRAY_MIXED.length);
        assertArrayEquals(new Object[]{1, 'a', "Str", 5.0, false}, CollectionsTestClass.ARRAY_MIXED);

        assertEquals(3, root.list.size());
        assertArrayEquals(new Integer[]{20, 30, 40}, root.list.toArray());

        assertEquals(3, root.map.size());
        for (Integer i : new Integer[]{1, 2, 10}) {
            assertTrue(root.map.containsKey(i));
            assertEquals(String.valueOf(i), root.map.get(i));
        }
    }

    @Test
    void testCollectionsAdd() throws IOException {
        String doc = """
            CollectionsTestClass {
              list += listOf (20, 30, 40)
            }
            """;
        CollectionsTestClass root = new DummyLoader<CollectionsTestClass>()
            .load(new ByteArrayInputStream(doc.getBytes()), null)
            .root();

        assertEquals(8, root.list.size());
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 20, 30, 40}, root.list.toArray());
    }
}
