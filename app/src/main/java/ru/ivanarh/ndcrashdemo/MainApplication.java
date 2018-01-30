package ru.ivanarh.ndcrashdemo;

import android.app.Application;

import ru.ivanarh.jndcrash.NDCrash;


public class MainApplication extends Application {

    public static String mNativeCrashPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mNativeCrashPath = getFilesDir().getAbsolutePath() + "/crash.txt";
        NDCrash.initialize(mNativeCrashPath);
    }
}
