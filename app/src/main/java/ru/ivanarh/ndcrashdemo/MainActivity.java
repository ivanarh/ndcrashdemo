package ru.ivanarh.ndcrashdemo;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ru.ivanarh.jndcrash.Unwinder;


public class MainActivity extends Activity {

    private TextView mNativeCrashTextField;
    private String mNativeCrashTextFieldDefaultValue = "";

    Spinner mUnwinderForNextLaunch;
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
                final File crashFile = new File(MainApplication.getReportPath());
                if (!crashFile.delete()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Couldn't delete a file: " + crashFile.getAbsolutePath(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
        mUnwinderForNextLaunch = findViewById(R.id.unwinder_for_next_launch_spinner);
        mOutOfProcess = findViewById(R.id.out_of_process_checkbox);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[] { "libcorkscrew", "libunwind", "libunwindstack", "cxxabi", "stackscan" } // should match ndcrash_unwinder order.
                );
        mUnwinderForNextLaunch.setAdapter(adapter);

        final SharedPreferences prefs = getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE);
        mUnwinderForNextLaunch.setSelection(prefs.getInt(MainApplication.UNWINDER_FOR_NEXT_LAUNCH_KEY, 0));
        mUnwinderForNextLaunch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                saveUnwinderToSettings();
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) {
                saveUnwinderToSettings();
            }
        });

        mOutOfProcess.setChecked(prefs.getBoolean(MainApplication.OUT_OF_PROCESS_KEY, false));
        mOutOfProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveUnwinderToSettings();
            }
        });

        mNativeCrashTextField = (TextView) findViewById(R.id.lastCrashContents);
        mNativeCrashTextFieldDefaultValue = mNativeCrashTextField.getText().toString();

        // Start service and stop service buttons handlers.
        findViewById(R.id.start_out_of_process_service).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final SharedPreferences prefs = getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE);
                final Unwinder unwinder = Unwinder.values()[prefs.getInt(MainApplication.UNWINDER_FOR_NEXT_LAUNCH_KEY, 0)];
                MainApplication.startCrashService(getApplicationContext(), unwinder);
            }
        });
        findViewById(R.id.stop_out_of_process_service).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                getApplicationContext().stopService(new Intent(getApplicationContext(), CrashService.class));
            }
        });
    }

    private void saveUnwinderToSettings() {
        getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(MainApplication.UNWINDER_FOR_NEXT_LAUNCH_KEY, mUnwinderForNextLaunch.getSelectedItemPosition())
                .putBoolean(MainApplication.OUT_OF_PROCESS_KEY, mOutOfProcess.isChecked())
                .apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final File crashFile = new File(MainApplication.getReportPath());
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
