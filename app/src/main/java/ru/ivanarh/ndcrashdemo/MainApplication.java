package ru.ivanarh.ndcrashdemo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import ru.ivanarh.jndcrash.NDCrashError;
import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.NDCrashUtils;
import ru.ivanarh.jndcrash.NDCrashUnwinder;

public class MainApplication extends Application {
    /**
     * Log tag.
     */
    private static final String TAG = "NDCRASHDEMO";

    /**
     * Name of shared preferences that we use to keep parameters.
     */
    public static final String SHARED_PREFS_NAME = "ndcrashdemo";

    /**
     * Key in shared preferences for next launch unwinder.
     */
    public static final String UNWINDER_FOR_NEXT_LAUNCH_KEY = "unwinder_for_next_launch";

    /**
     * Key in shared preferences for out-of-process flag.
     */
    public static final String OUT_OF_PROCESS_KEY = "out_of_process";

    /**
     * Path where we save a crash report. Includes a file name.
     */
    private static String mReportPath;

    public static String getReportPath() {
        return mReportPath;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If it's a process for NDCrashService we don't need to initialize NDCrash signal handler.
        if (!NDCrashUtils.isMainProcess(this)) return;
        mReportPath = getFilesDir().getAbsolutePath() + "/crash.txt";
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        final NDCrashUnwinder unwinder = NDCrashUnwinder.values()[prefs.getInt(UNWINDER_FOR_NEXT_LAUNCH_KEY, 0)];
        final boolean outOfProcess = prefs.getBoolean(OUT_OF_PROCESS_KEY, false);
        if (outOfProcess) {
            // Initializing signal handler. It will start a service on success.
            final NDCrashError initResult = NDCrash.initializeOutOfProcess(
                    this,
                    mReportPath,
                    unwinder,
                    CrashService.class);
            Log.i(TAG, "Out-of-process signal handler is initialized with result: " + initResult);
            if (initResult != NDCrashError.ok) {
                Toast.makeText(
                        getApplicationContext(),
                        "Couldn't initialize NDCrash signal handler for out-of-process mode, error: " + initResult,
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            final NDCrashError initResult = NDCrash.initializeInProcess(mReportPath, unwinder);
            Log.i(TAG, "In-process signal handler is initialized with result: " + initResult + " unwinder: " + unwinder);
            if (initResult != NDCrashError.ok) {
                Toast.makeText(
                        getApplicationContext(),
                        "Couldn't initialize NDCrash with unwinder " + unwinder + " in in-process mode, error: " + initResult,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
