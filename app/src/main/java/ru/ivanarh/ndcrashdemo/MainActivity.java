package ru.ivanarh.ndcrashdemo;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends Activity {

    private TextView mNativeCrashTextField;
    private String mNativeCrashTextFieldDefaultValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.ndkcrash);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crashApp();
            }
        });

        b = (Button) findViewById(R.id.javacrash);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                throw new Error("I am crashed!");
            }
        });

        b = (Button) findViewById(R.id.clearndkcrash);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNativeCrashTextField.setText(mNativeCrashTextFieldDefaultValue);
                final File crashFile = new File(MainApplication.mNativeCrashPath);
                crashFile.delete();
            }
        });

        final Spinner backendForNextLaunch = findViewById(R.id.backend_for_next_launch_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[] { "libcorkscrew", "libunwind", "libunwindstack" } // should match ndcrash_backend order.
                );
        backendForNextLaunch.setAdapter(adapter);
        backendForNextLaunch.setSelection(getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE).
                getInt(MainApplication.BACKEND_FOR_NEXT_LAUNCH_KEY, 0));
        backendForNextLaunch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                saveBackendToSettings(i);
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) {
                saveBackendToSettings(0);
            }
        });

        mNativeCrashTextField = (TextView) findViewById(R.id.lastCrashContents);
        mNativeCrashTextFieldDefaultValue = mNativeCrashTextField.getText().toString();
    }

    private void saveBackendToSettings(int backend) {
        getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(MainApplication.BACKEND_FOR_NEXT_LAUNCH_KEY, backend)
                .apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final File crashFile = new File(MainApplication.mNativeCrashPath);
        if (crashFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(crashFile);
                ByteArrayOutputStream memstream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    memstream.write(buffer, 0, bytesRead);
                }
                final String crashContent = new String(memstream.toByteArray(), "UTF-8");
                mNativeCrashTextField.setText(crashContent);
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
        }
    }

    public native void crashApp();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ndcrashdemo");
    }
}
