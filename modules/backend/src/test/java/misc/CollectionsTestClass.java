package misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//================================================================================
// Inner Classes
//================================================================================
public class CollectionsTestClass {
    public static int[] ARRAY = new int[]{1, 2, 3, 4, 5};
    public static Integer[] ARRAY_IG = new Integer[]{0, 0};
    public static char[] ARRAY_CHAR = new char[]{};
    public static boolean[] ARRAY_BOOLEAN;
    public static Object[] ARRAY_MIXED;
    public final List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
    public Map<Integer, String> map;

    public void setMap(Map<Integer, String> map) {
        this.map = map;
    }
}
