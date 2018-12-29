package com.a4dotsinc.dynamicwalls;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.a4dotsinc.dynamicwalls.databinding.ActivityMainBinding;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private PendingIntent pendingIntent;
    Dialog provChange;

    SharedPreferences sharedPref;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.active_switches), Context.MODE_PRIVATE);

        provChange = new Dialog(this);

        final SharedPreferences.Editor editor = sharedPref.edit();

        Intent alarmIntent = new Intent(MainActivity.this, TimeReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        provChange.setContentView(R.layout.provider_selection_dialog);
        final RadioGroup rg = (RadioGroup) provChange.findViewById(R.id.radioGp);

        int logo = sharedPref.getInt(getString(R.string.wall_provider), ((RadioButton)rg.getChildAt(2)).getId());
        if (logo == ((RadioButton)rg.getChildAt(0)).getId())
            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.pexels_logo));
        else if (logo == ((RadioButton)rg.getChildAt(1)).getId())
            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.pixabay_logo));
        else if (logo == ((RadioButton)rg.getChildAt(2)).getId())
            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.unsplash_logo));

        int timerToogle = sharedPref.getInt(getString(R.string.timer_on), 0);
        if(timerToogle == 1){
            binding.turnOn.setChecked(true);
        }

        if(sharedPref.getInt(getString(R.string.blur_on), 0) == 1){
            binding.blur.setChecked(true);
        }

        if(sharedPref.getInt(getString(R.string.just_hd), 0) == 1){
            binding.onlyHD.setChecked(true);
        }

        binding.onlyHD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putInt(getString(R.string.just_hd), 1);
                    editor.apply();
                    cancel();
                    start();
                    vibrator.vibrate(30);
                    Snackbar snackbar = Snackbar.make(buttonView, "Only 1080p Walls will be used!"
                            , Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }else{
                    editor.putInt(getString(R.string.just_hd), 0);
                    editor.apply();
                    cancel();
                    start();
                    vibrator.vibrate(20);
                    Snackbar snackbar = Snackbar.make(buttonView, "Varying Resolution Walls will be used!"
                            , Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });
        binding.blur.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putInt(getString(R.string.blur_on), 1);
                    editor.apply();
                    cancel();
                    start();
                    Snackbar snackbar = Snackbar.make(buttonView, "Blur Effedt Will be Added to the Next Image"
                                                , Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }else{
                    editor.putInt(getString(R.string.blur_on), 0);
                    editor.apply();
                    cancel();
                    start();
                }
            }
        });

        binding.providerChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provChange.show();
                rg.check(sharedPref.getInt(getString(R.string.wall_provider), ((RadioButton)rg.getChildAt(2)).getId()));
                rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        editor.putInt(getString(R.string.wall_provider), checkedId);
                        editor.apply();
                        cancel();
                        vibrator.vibrate(30);
                        start();
                        provChange.dismiss();
                        if (checkedId == ((RadioButton)rg.getChildAt(0)).getId())
                            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.pexels_logo));
                        else if (checkedId == ((RadioButton)rg.getChildAt(1)).getId())
                            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.pixabay_logo));
                        else if (checkedId == ((RadioButton)rg.getChildAt(2)).getId())
                            binding.creditLogo.setImageDrawable(getResources().getDrawable(R.drawable.unsplash_logo));
                    }
                });
            }
        });

        binding.lastChange.setText("Last Change - "+sharedPref.getString(getString(R.string.last_sync), ""));

        binding.tags.setText(sharedPref.getString(getString(R.string.tags), ""));
        binding.timeInter.setText(String.valueOf(sharedPref.getInt(getString(R.string.time_interval), 10)));

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        binding.turnOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    start();
                    vibrator.vibrate(30);
                    editor.putInt(getString(R.string.timer_on), 1);
                    editor.putString(getString(R.string.tags), binding.tags.getText().toString());
                    editor.apply();
                } else {
                    cancel();
                    vibrator.vibrate(30);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.timer_on), 0);
                    editor.apply();
                }
            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(getString(R.string.tags), binding.tags.getText().toString());
                editor.putInt(getString(R.string.time_interval), Integer.parseInt(binding.timeInter.getText().toString()));
                editor.commit();
                vibrator.vibrate(30);
                cancel();
                start();
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
