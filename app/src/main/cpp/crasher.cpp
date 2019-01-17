#include <jni.h>
#include <stdexcept>
#include <stdlib.h>
#include <assert.h>

/// Represents a type of crash, an error type. Should match enum in Crasher.java.
enum CrashType {
    nullPointerDereference,
    freeGarbagePointer,
    divisionByZeroInteger,
    abortCall,
    cppException,
    stackOverflow,
    builtInTrap,
    undefinedInstruction,
    privilegedInstruction,
    assertionFailed,
};

extern "C" {

void do_nullPointerDereference() {
    volatile int *ptr = NULL;
    *ptr = 1;
}

void do_freeGarbagePointer() {
    free((void *) 0x1234);
}

void do_divisionByZeroInteger() {
    int d = 0;
    int j = 10 / d;
}

void do_abortCall() {
    abort();
}

/// Functions for stack overflow.
#pragma clang diagnostic push
#pragma ide diagnostic ignored "InfiniteRecursion"

void doStackOverflow2();

void do_stackOverflow() {
    doStackOverflow2();
}

void doStackOverflow2() {
    do_stackOverflow();
}

#pragma clang diagnostic pop

void do_builtInTrap() {
    __builtin_trap();
}

void do_undefinedInstruction() {
#if __i386__
    asm volatile ( "ud2" : : : );
#elif __x86_64__
    asm volatile ( "ud2" : : : );
#elif __arm__ && __thumb__
    asm volatile ( ".word 0xde00" : : : );
#elif __arm__
    asm volatile ( ".long 0xf7f8a000" : : : );
#elif __arm64__
    asm volatile ( ".long 0xf7f8a000" : : : );
#endif
}

void do_privilegedInstruction() {
#if __i386__
    asm volatile ( "hlt" : : : );
#elif __x86_64__
    asm volatile ( "hlt" : : : );
#elif __arm__ && __thumb__
    asm volatile ( ".long 0xf7f08000" : : : );
#elif __arm__
    asm volatile ( ".long 0xe1400070" : : : );
#elif __arm64__
    asm volatile ( "tlbi alle1" : : : );
#endif
}

void do_assertionFailed() {
    __assert(__FILE__, __LINE__, "This is a test crash with __assert function.");
}

/// Performs a crash according to passed crash type.
void performCrash(CrashType type) {
    switch (type) {
        case nullPointerDereference:
            do_nullPointerDereference();
            break;
        case freeGarbagePointer:
            do_freeGarbagePointer();
            break;
        case divisionByZeroInteger:
            do_divisionByZeroInteger();
            break;
        case abortCall:
            do_abortCall();
            break;
        case cppException:
            throw std::runtime_error("ndcrashdemo error");
            break;
        case stackOverflow:
            do_stackOverflow();
            break;
        case builtInTrap:
            do_builtInTrap();
            break;
        case undefinedInstruction:
            do_undefinedInstruction();
            break;
        case privilegedInstruction:
            do_privilegedInstruction();
            break;
        case assertionFailed:
            do_assertionFailed();
            break;
    }
}
} // extern "C"

void simpleCppFunction(CrashType crashType) {
    performCrash(crashType);
}

class TestClass {
public:
    TestClass() {
    }

    TestClass(CrashType type) {
        performCrash(type);
    }

    void dynamicMethod(CrashType type) {
        performCrash(type);
    }

    static void staticMethod(CrashType type) {
        performCrash(type);
    }
};

extern "C" {

void simpleCFunction(CrashType type) {
    performCrash(type);
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_nativeDoCrash(JNIEnv *env, jclass type, jint crashTypeOrdinal) {
    const CrashType crashType = (CrashType) crashTypeOrdinal;
    srand((unsigned int)time(NULL));
    const int r = rand() % 5;
    // Calling random intermediate function.
    switch (r) {
        case 0:
            simpleCppFunction(crashType);
            break;
        case 1: {
            TestClass c(crashType);
        }
            break;
        case 2:
            TestClass().dynamicMethod(crashType);
            break;
        case 3:
            TestClass::staticMethod(crashType);
            break;
        case 4:
            simpleCFunction(crashType);
            break;
        default:
            performCrash(crashType);
    }
}
}// extern "C"