package ru.ivanarh.ndcrashdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends Activity {

    private TextView mNativeCrashTextField;
    private String mNativeCrashTextFieldDefaultValue = "";

    Spinner mBackendForNextLaunch;
    CheckBox mOutOfProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up tab host.
        final TabHost tabHost = findViewById(R.id.tab_host);
        tabHost.setup();
        TabHost.TabSpec spec = tabHost.newTabSpec("CrashReport");
        spec.setContent(R.id.tab_last_report);
        spec.setIndicator(getString(R.string.last_ndk_crash_report));
        tabHost.addTab(spec);
        spec = tabHost.newTabSpec("CrashApplication");
        spec.setContent(R.id.tab_crash_app);
        spec.setIndicator(getString(R.string.crash_application));
        tabHost.addTab(spec);

        // Setting up crash buttons.
        findViewById(R.id.crash_type_nullptr_dereference).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.nullPointerDereference();
            }
        });
        findViewById(R.id.crash_type_free_call_with_garbage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.freeGarbagePointer();
            }
        });
        findViewById(R.id.crash_type_abort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.abort();
            }
        });
        findViewById(R.id.crash_type_cpp_exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.cppException();
            }
        });
        findViewById(R.id.crash_type_stack_overflow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.stackOverflow();
            }
        });
        findViewById(R.id.crash_type_built_in_trap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.builtInTrap();
            }
        });
        findViewById(R.id.crash_type_undefined_instruction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.undefinedInstruction();
            }
        });
        findViewById(R.id.crash_type_privileged_instruction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.privilegedInstruction();
            }
        });
        findViewById(R.id.crash_type_division_by_zero_integer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.divisionByZeroInteger();
            }
        });

        findViewById(R.id.clear_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNativeCrashTextField.setText(mNativeCrashTextFieldDefaultValue);
                final File crashFile = new File(MainApplication.mNativeCrashPath);
                crashFile.delete();
            }
        });
        mBackendForNextLaunch = findViewById(R.id.backend_for_next_launch_spinner);
        mOutOfProcess = findViewById(R.id.out_of_process_checkbox);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[] { "libcorkscrew", "libunwind", "libunwindstack", "cxxabi", "stackscan" } // should match ndcrash_backend order.
                );
        mBackendForNextLaunch.setAdapter(adapter);

        final SharedPreferences prefs = getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE);
        mBackendForNextLaunch.setSelection(prefs.getInt(MainApplication.BACKEND_FOR_NEXT_LAUNCH_KEY, 0));
        mBackendForNextLaunch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                saveBackendToSettings();
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) {
                saveBackendToSettings();
            }
        });

        mOutOfProcess.setChecked(prefs.getBoolean(MainApplication.OUT_OF_PROCESS_KEY, false));
        mOutOfProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveBackendToSettings();
            }
        });

        mNativeCrashTextField = (TextView) findViewById(R.id.lastCrashContents);
        mNativeCrashTextFieldDefaultValue = mNativeCrashTextField.getText().toString();
    }

    private void saveBackendToSettings() {
        getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(MainApplication.BACKEND_FOR_NEXT_LAUNCH_KEY, mBackendForNextLaunch.getSelectedItemPosition())
                .putBoolean(MainApplication.OUT_OF_PROCESS_KEY, mOutOfProcess.isChecked())
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
}
