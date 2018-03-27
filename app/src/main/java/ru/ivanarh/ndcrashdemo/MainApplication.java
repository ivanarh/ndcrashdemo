package ru.ivanarh.ndcrashdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;

import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.NDCrashService;

public class MainApplication extends Application {

    public static final String SHARED_PREFS_NAME = "jndcrash";
    public static final String UNWINDER_FOR_NEXT_LAUNCH_KEY = "unwinder_for_next_launch";
    public static final String OUT_OF_PROCESS_KEY = "out_of_process";
    public static String mNativeCrashPath;

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
        final int unwinder = prefs.getInt(UNWINDER_FOR_NEXT_LAUNCH_KEY, 0);
        final boolean outOfProcess = prefs.getBoolean(OUT_OF_PROCESS_KEY, false);
        NDCrash.initialize(mNativeCrashPath, unwinder, outOfProcess);
        if (outOfProcess) {
            //Starting the crash report catching service (in another process)
            final Intent serviceIntent = new Intent(this, NDCrashService.class);
            serviceIntent.putExtra(NDCrashService.EXTRA_REPORT_FILE, mNativeCrashPath);
            serviceIntent.putExtra(NDCrashService.EXTRA_UNWINDER, unwinder);
            startService(serviceIntent);
        }
    }
}
