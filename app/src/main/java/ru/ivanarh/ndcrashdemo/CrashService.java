package ru.ivanarh.ndcrashdemo;

import android.util.Log;
import android.widget.Toast;

import ru.ivanarh.jndcrash.NDCrashError;
import ru.ivanarh.jndcrash.NDCrashService;
import ru.ivanarh.jndcrash.NDCrashUnwinder;

/**
 * Service for out-of-process crash handling daemon. Should be run from a separate process.
 */
public class CrashService extends NDCrashService {

    /**
     * Log tag.
     */
    private static final String TAG = "NDCRASHDEMO";

    @Override
    public void onCrash(String reportPath) {
        Log.i(TAG, "Crash report has been successfully saved: " + reportPath);
    }

    @Override
    protected void onDaemonStart(NDCrashUnwinder unwinder, String reportPath, NDCrashError result) {
        final String message;
        if (result == NDCrashError.ok) {
            message = "NDCrash out-of-process daemon is successfully started with unwinder: " + unwinder;
        } else {
            message = "Couldn't start NDCrash out-of-process daemon with unwinder: " + unwinder + ", error: " + result;
        }
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
