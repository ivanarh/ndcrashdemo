#include <jni.h>
#include <stdexcept>
#include <stdlib.h>

extern "C" {

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_nullPointerDereference(JNIEnv *env, jclass type) {
    volatile int *ptr = NULL;
    *ptr = 1;
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_freeGarbagePointer(JNIEnv *env, jclass type) {
    free((void *)0x1234);
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_abort(JNIEnv *env, jclass type) {
    abort();
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_cppException(JNIEnv *env, jclass type) {
    throw std::runtime_error("ndcrashdemo error");
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_divisionByZeroInteger(JNIEnv *env, jclass type) {
    int d = 0;
    int j = 10 / d;
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "InfiniteRecursion"
JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_stackOverflow(JNIEnv *env, jclass type) {
    Java_ru_ivanarh_ndcrashdemo_Crasher_stackOverflow(env, type);
}
#pragma clang diagnostic pop

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_builtInTrap(JNIEnv *env, jclass type) {
    __builtin_trap();
}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_undefinedInstruction(JNIEnv *env, jclass type) {
#if __i386__
    asm volatile ( "ud2" : : : );
#elif __x86_64__
    asm volatile ( "ud2" : : : );
#elif __arm__ && __ARM_ARCH == 7 && __thumb__
    asm volatile ( ".word 0xde00" : : : );
#elif __arm__ && __ARM_ARCH == 7
    asm volatile ( ".long 0xf7f8a000" : : : );
#elif __arm__ && __ARM_ARCH == 6 && __thumb__
    asm volatile ( ".word 0xde00" : : : );
#elif __arm__ && __ARM_ARCH == 6
    asm volatile ( ".long 0xf7f8a000" : : : );
#elif __arm64__
    asm volatile ( ".long 0xf7f8a000" : : : );
#endif

}

JNIEXPORT void JNICALL
Java_ru_ivanarh_ndcrashdemo_Crasher_privilegedInstruction(JNIEnv *env, jclass type) {
#if __i386__
    asm volatile ( "hlt" : : : );
#elif __x86_64__
    asm volatile ( "hlt" : : : );
#elif __arm__ && __ARM_ARCH == 7 && __thumb__
    asm volatile ( ".long 0xf7f08000" : : : );
#elif __arm__ && __ARM_ARCH == 7
    asm volatile ( ".long 0xe1400070" : : : );
#elif __arm__ && __ARM_ARCH == 6 && __thumb__
    asm volatile ( ".long 0xf5ff8f00" : : : );
#elif __arm__ && __ARM_ARCH == 6
    asm volatile ( ".long 0xe14ff000" : : : );
#elif __arm64__
  asm volatile ( "tlbi alle1" : : : );
#endif
}

}// extern "C"