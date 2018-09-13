package com.a4dotsinc.dynamicwalls;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Date;

public class TimeReceiver extends BroadcastReceiver {

    ImageLoader imageLoader;
    @Override
    public void onReceive(final Context context, Intent intent) {

        final SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_switches), Context.MODE_PRIVATE);

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
            imageLoader.loadImage("https://source.unsplash.com/1080x1920/?"+tags, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    try{
                        wallpaperManager.setBitmap(loadedImage);
                        wallpaperManager.setBitmap((loadedImage), null, true, WallpaperManager.FLAG_LOCK);
                        Log.d("Wall Changed", "newWallAt: ");
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.last_sync), android.text.format.DateFormat.format("hh:mm:aa dd-MM-yyyy", new Date()).toString());
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
}
