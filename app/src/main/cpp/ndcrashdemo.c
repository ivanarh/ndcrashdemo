#include <jni.h>

JNIEXPORT void JNICALL Java_ru_ivanarh_ndcrashdemo_MainActivity_crashApp(JNIEnv *env, jobject instance) {
    int *iptr = NULL;
    *iptr = 1;
}