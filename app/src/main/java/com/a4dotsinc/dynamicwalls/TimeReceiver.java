package com.a4dotsinc.dynamicwalls;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsic;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Date;

public class TimeReceiver extends BroadcastReceiver {

    ImageLoader imageLoader;
    Context context;
    @Override
    public void onReceive(final Context context, Intent intent) {

        final SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_switches), Context.MODE_PRIVATE);

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
            imageLoader.loadImage("https://source.unsplash.com/1080x1920/?"+tags, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    try{
                        if (sharedPref.getInt(context.getString(R.string.blur_on), 0) == 1){
                            wallpaperManager.setBitmap(blutIt(loadedImage, 2f));
                            wallpaperManager.setBitmap((blutIt(loadedImage, 2f)), null, true, WallpaperManager.FLAG_LOCK);
                        }
                        else{
                            wallpaperManager.setBitmap(loadedImage);
                            wallpaperManager.setBitmap(loadedImage, null, true, WallpaperManager.FLAG_LOCK);
                        }
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
