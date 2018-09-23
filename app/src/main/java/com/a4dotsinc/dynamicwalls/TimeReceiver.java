package com.a4dotsinc.dynamicwalls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Calendar;
import java.util.Date;

public class TimeReceiver extends BroadcastReceiver {

    ImageLoader imageLoader;
    Context context;
    @Override
    public void onReceive(final Context context, Intent intent) {

        final SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_switches), Context.MODE_PRIVATE);

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
                Intent alarmIntent = new Intent(context, TimeReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                int interval = sharedPref.getInt(context.getString(R.string.time_interval), 10);

                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+10000,
                        1000 * 60 * interval, pendingIntent);
                Toast.makeText(context, "Dynamic Walls Activated", Toast.LENGTH_SHORT).show();
        }

        this.context = context;
        imageLoader = ImageLoader.getInstance();

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));


        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

        try{
            String tags;
            if(sharedPref.getString(context.getString(R.string.tags), "").length()==0){
                tags = "nature";
            }
            else{
                tags = sharedPref.getString(context.getString(R.string.tags), "");
            }
            String url;
            if (sharedPref.getInt(context.getString(R.string.just_hd), 0) == 0){
                url = "https://source.unsplash.com/featured/?";
            }else{
               url = "https://source.unsplash.com/1080x1920/?";
            }

            imageLoader.loadImage(url+tags, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    try{
                        if (sharedPref.getInt(context.getString(R.string.blur_on), 0) == 1){
                            wallpaperManager.setBitmap(blutIt(loadedImage, 2f));
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                wallpaperManager.setBitmap((blutIt(loadedImage, 2f)), null, true, WallpaperManager.FLAG_LOCK);
                            }
                        }
                        else{
                            wallpaperManager.setBitmap(loadedImage);
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                wallpaperManager.setBitmap(loadedImage, null, true, WallpaperManager.FLAG_LOCK);
                            }
                        }
                        Log.d("Wall Changed", "newWallAt: ");
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.last_sync), DateFormat.format("hh:mm:aa dd-MM-yyyy", new Date()).toString());
                        editor.commit();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Bitmap blutIt(Bitmap original, float radius){
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                    Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(this.context);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();

        return bitmap;
    }

}
