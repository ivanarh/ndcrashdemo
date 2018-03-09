package ru.ivanarh.ndcrashdemo;

import android.app.Application;
import android.content.Context;

import ru.ivanarh.jndcrash.NDCrash;


public class MainApplication extends Application {

    public static final String SHARED_PREFS_NAME = "jndcrash";
    public static final String BACKEND_FOR_NEXT_LAUNCH_KEY = "backend_for_next_launch";
    public static String mNativeCrashPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mNativeCrashPath = getFilesDir().getAbsolutePath() + "/crash.txt";
        NDCrash.initialize(mNativeCrashPath, getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getInt(BACKEND_FOR_NEXT_LAUNCH_KEY, 0));
    }
}
