package com.a4dotsinc.dynamicwalls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Switch turnOn;
    private PendingIntent pendingIntent;
    EditText tags, time;
    Button save;
    TextView lastChange;

    SharedPreferences sharedPref;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.active_switches), Context.MODE_PRIVATE);

        turnOn = (Switch) findViewById(R.id.turnOn);
        Intent alarmIntent = new Intent(MainActivity.this, TimeReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        int timerToogle = sharedPref.getInt(getString(R.string.timer_on), 0);
        if(timerToogle == 1){
            turnOn.setChecked(true);
        }

        tags = (EditText) findViewById(R.id.tags);
        time = (EditText) findViewById(R.id.timeInter);
        save = (Button) findViewById(R.id.save);

        lastChange = (TextView) findViewById(R.id.lastChange);

        lastChange.setText("Last Change - "+sharedPref.getString(getString(R.string.last_sync), ""));

        tags.setText(sharedPref.getString(getString(R.string.tags), ""));
        time.setText(String.valueOf(sharedPref.getInt(getString(R.string.time_interval), 10)));

        final SharedPreferences.Editor editor = sharedPref.edit();

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        turnOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    start();
                    vibrator.vibrate(30);
                    editor.putInt(getString(R.string.timer_on), 1);
                    editor.putString(getString(R.string.tags), tags.getText().toString());
                    editor.commit();
                } else {
                    cancel();
                    vibrator.vibrate(30);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.timer_on), 0);
                    editor.commit();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(getString(R.string.tags), tags.getText().toString());
                editor.putInt(getString(R.string.time_interval), Integer.parseInt(time.getText().toString()));
                editor.commit();
                vibrator.vibrate(30);
                Snackbar snackbar = Snackbar
                                        .make(v, "Tags and Interval Saved", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });

    }

    public void start() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = sharedPref.getInt(getString(R.string.time_interval), 10);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+10000,
                1000 * 60 * interval, pendingIntent);
        Toast.makeText(this, "Dynamic Walls Activated", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, "Dynamic Walls Deactivated", Toast.LENGTH_SHORT).show();
    }

}