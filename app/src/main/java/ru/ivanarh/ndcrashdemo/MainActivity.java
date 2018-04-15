package ru.ivanarh.ndcrashdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import ru.ivanarh.jndcrash.NDCrashError;
import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.NDCrashUnwinder;


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
                Crasher.doCrash(Crasher.Type.nullPointerDereference);
            }
        });
        findViewById(R.id.crash_type_free_call_with_garbage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.freeGarbagePointer);
            }
        });
        findViewById(R.id.crash_type_abort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.abortCall);
            }
        });
        findViewById(R.id.crash_type_cpp_exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.cppException);
            }
        });
        findViewById(R.id.crash_type_stack_overflow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.stackOverflow);
            }
        });
        findViewById(R.id.crash_type_built_in_trap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.builtInTrap);
            }
        });
        findViewById(R.id.crash_type_undefined_instruction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.undefinedInstruction);
            }
        });
        findViewById(R.id.crash_type_privileged_instruction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.privilegedInstruction);
            }
        });
        findViewById(R.id.crash_type_division_by_zero_integer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crasher.doCrash(Crasher.Type.divisionByZeroInteger);
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

        findViewById(R.id.send_report).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final File crashFile = new File(MainApplication.getReportPath());
                if (!crashFile.exists()) return;
                final Uri crashFileUri = FileProvider.getUriForFile(MainActivity.this, "ru.ivanarh.ndcrashdemo.files", crashFile);
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_STREAM, crashFileUri);
                final List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    grantUriPermission(resolveInfo.activityInfo.packageName, crashFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(Intent.createChooser(emailIntent, null));
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences prefs = getSharedPreferences(MainApplication.SHARED_PREFS_NAME, MODE_PRIVATE);
        final NDCrashUnwinder unwinder = NDCrashUnwinder.values()[prefs.getInt(MainApplication.UNWINDER_FOR_NEXT_LAUNCH_KEY, 0)];
        NDCrashError error;
        String message = null;
        switch (item.getItemId()) {
            case R.id.menu_in_initialize_signal_handler:
                error = NDCrash.initializeInProcess(
                        MainApplication.getReportPath(),
                        unwinder);
                message = "In-process initialization result: " + error;
                break;
            case R.id.menu_in_deinitialize_signal_handler:
                message = "Out-of-process de-initialization result: " + NDCrash.deInitializeInProcess();
                break;
            case R.id.menu_out_initialize_signal_handler:
                error = NDCrash.initializeOutOfProcess(
                        getApplicationContext(),
                        MainApplication.getReportPath(),
                        unwinder,
                        CrashService.class);
                message = "Out-of-process initialization result: " + error;
                break;
            case R.id.menu_out_deinitialize_signal_handler:
                message = "Out-of-process de-initialization result: " + NDCrash.deInitializeOutOfProcess(this);
                break;
            case R.id.menu_out_start_service:
                final Intent serviceIntent = new Intent(this, CrashService.class);
                serviceIntent.putExtra(CrashService.EXTRA_REPORT_FILE, MainApplication.getReportPath());
                serviceIntent.putExtra(CrashService.EXTRA_UNWINDER, unwinder.ordinal());
                startService(serviceIntent);
                break;
            case R.id.menu_out_stop_service:
                getApplicationContext().stopService(new Intent(getApplicationContext(), CrashService.class));
                break;
        }
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        return true;
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
