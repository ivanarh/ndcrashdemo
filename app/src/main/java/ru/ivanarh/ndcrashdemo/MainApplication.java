package ru.ivanarh.ndcrashdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.util.Log;

import ru.ivanarh.jndcrash.Error;
import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.Unwinder;

public class MainApplication extends Application {
    /**
     * Log tag.
     */
    private static final String TAG = "JNDCRASH";

    public static final String SHARED_PREFS_NAME = "jndcrash";
    public static final String UNWINDER_FOR_NEXT_LAUNCH_KEY = "unwinder_for_next_launch";
    public static final String OUT_OF_PROCESS_KEY = "out_of_process";
    public static String mNativeCrashPath;

    /**
     * Retrieves a flag whether a code is being run in main process.
     *
     * @return Flag value.
     */
    private boolean isMainProcess() {
        final int pid = Process.myPid();
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (final ActivityManager.RunningAppProcessInfo info : manager.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    return getPackageName().equals(info.processName);
                }
            }
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If it's a process for NDCrashService we don't need to initialize NDCrash signal handler.
        if (!isMainProcess()) return;
        mNativeCrashPath = getFilesDir().getAbsolutePath() + "/crash.txt";
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        final Unwinder unwinder = Unwinder.values()[prefs.getInt(UNWINDER_FOR_NEXT_LAUNCH_KEY, 0)];
        final boolean outOfProcess = prefs.getBoolean(OUT_OF_PROCESS_KEY, false);
        if (outOfProcess) {
            // Initializing signal handler.
            final Error initResult = NDCrash.initializeOutOfProcess();
            Log.i(TAG, "Out-of-process signal handler is initialized with result: " + initResult);

            // Starting the crash report catching service (in another process). It will start a daemon.
            final Intent serviceIntent = new Intent(this, CrashService.class);
            serviceIntent.putExtra(CrashService.EXTRA_REPORT_FILE, mNativeCrashPath);
            serviceIntent.putExtra(CrashService.EXTRA_UNWINDER, unwinder.ordinal());
            startService(serviceIntent);
        } else {
            final Error initResult = NDCrash.initializeInProcess(mNativeCrashPath, unwinder);
            Log.i(TAG, "In-process signal handler is initialized with result: " + initResult + " unwinder: " + unwinder);

            // Stopping service. We don't need it anymore.
            stopService(new Intent(this, CrashService.class));
        }
    }
}
