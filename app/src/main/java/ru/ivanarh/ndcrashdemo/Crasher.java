package ru.ivanarh.ndcrashdemo;

import android.support.annotation.NonNull;

/**
 * Class contains methods to crash an application by a different way.
 */
public class Crasher {

    /**
     * Functions that crash application.
     *
     * @param type A way how to crash an application.
     */
    public static void doCrash(@NonNull Type type) {
        nativeDoCrash(type.ordinal());
    }

    /**
     * Functions that crash application in a native code.
     *
     * @param crashTypeOrdinal A way how to crash an application, ordinal value.
     */
    private static native void nativeDoCrash(int crashTypeOrdinal);

    /**
     * Represents a type of crash, an error type. Should match enum in crasher.cpp.
     */
    public enum Type {
        nullPointerDereference,
        freeGarbagePointer,
        divisionByZeroInteger,
        abortCall,
        cppException,
        stackOverflow,
        builtInTrap,
        undefinedInstruction,
        privilegedInstruction,
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ndcrashdemo");
    }

}
