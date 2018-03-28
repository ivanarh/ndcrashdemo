package ru.ivanarh.ndcrashdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ru.ivanarh.jndcrash.Error;
import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.Unwinder;

/**
 * Service for out-of-process crash handling daemon. Should be run from a separate process.
 */
public class CrashService extends Service implements NDCrash.OnCrashCallback
{
    /**
     * Log tag.
     */
    private static final String TAG = "JNDCRASH";

    /**
     * Key for report file in arguments.
     */
    public static final String EXTRA_REPORT_FILE = "report_file";

    /**
     * Key for unwinder in arguments. Ordinal value is saved as integer.
     */
    public static final String EXTRA_UNWINDER = "unwinder";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mNativeStarted && intent != null) {
            mNativeStarted = true;
            final Unwinder unwinder = Unwinder.values()[intent.getIntExtra(EXTRA_UNWINDER, 0)];
            final String reportPath = intent.getStringExtra(EXTRA_REPORT_FILE);
            final Error initResult = NDCrash.startOutOfProcessDaemon(reportPath, unwinder, this);
            Log.i(TAG, "Out-of-process unwinding daemon is started with result: " + initResult +  ", unwinder: " + unwinder + " report path: " + reportPath);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        if (mNativeStarted) {
            mNativeStarted = false;
            final boolean stoppedSuccessfully = NDCrash.stopOutOfProcessDaemon();
            Log.i(TAG, "Out-of-process daemon " + (stoppedSuccessfully ? "is successfully stopped." : "failed to stop."));
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * Indicates if a daemon was started.
     */
    private static boolean mNativeStarted = false;

    @Override
    public void onCrash(String reportPath) {
        Log.i(TAG, "Crash report has been successfully saved: " + reportPath);
    }
}
