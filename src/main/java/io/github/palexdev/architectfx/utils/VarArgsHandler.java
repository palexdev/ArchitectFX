package io.github.palexdev.architectfx.utils;

import java.lang.reflect.Array;
import java.util.List;

public class VarArgsHandler {

    //================================================================================
    // Constructors
    //================================================================================
    private VarArgsHandler() {}

    //================================================================================
    // Static Methods
    //================================================================================
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static Object generateArray(List<?> varargs) {
        if (varargs.isEmpty()) return null;
        // TODO For now, we only support uniform varargs
        Class<?> type = varargs.getFirst().getClass();
        Object vArr = Array.newInstance(type, varargs.size());
        System.arraycopy(varargs.toArray(), 0, vArr, 0, varargs.size());
        return vArr;
    }

    public static Object[] combine(Object[] args, Object varargs) {
        if (varargs == null) return args;
        Object[] newArr = new Object[args.length + 1];
        System.arraycopy(args, 0, newArr, 0, args.length);
        newArr[args.length] = varargs;
        return newArr;
    }
}
