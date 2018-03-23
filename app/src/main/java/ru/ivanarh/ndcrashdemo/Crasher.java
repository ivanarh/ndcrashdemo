package ru.ivanarh.ndcrashdemo;

/**
 * Class contains methods to crash an application by a different way.
 */
public class Crasher {

    // Functions that crash application. A way to crash should be clear from function name.
    public static native void nullPointerDereference();
    public static native void freeGarbagePointer();
    public static native void divisionByZeroInteger();
    public static native void abort();
    public static native void cppException();
    public static native void stackOverflow();
    public static native void builtInTrap();
    public static native void undefinedInstruction();
    public static native void privilegedInstruction();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ndcrashdemo");
    }

}
