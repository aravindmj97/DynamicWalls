package com.a4dotsinc.dynamicwalls;

import android.app.AlarmManager;
import android.app.Dialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TimeReceiver extends BroadcastReceiver {

    ImageLoader imageLoader;
    Context context;
    SharedPreferences sharedPref;

    @Override
    public void onReceive(final Context context, Intent intent) {

        sharedPref = context.getSharedPreferences(
                context.getString(R.string.active_switches), Context.MODE_PRIVATE);

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
                Intent alarmIntent = new Intent(context, TimeReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                int interval = sharedPref.getInt(context.getString(R.string.time_interval), 10);

            if (manager != null) {
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
            }
            Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
            if (manager != null) {
                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+10000,
                        1000 * 60 * interval, pendingIntent);
            }
            Toast.makeText(context, "Dynamic Walls Activated", Toast.LENGTH_SHORT).show();
        }

        this.context = context;
        imageLoader = ImageLoader.getInstance();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.provider_selection_dialog);
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radioGp);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
        RequestQueue queue = Volley.newRequestQueue(context);

        try{
            String tags;
            if(sharedPref.getString(context.getString(R.string.tags), "").length()==0){
                tags = "nature";
            }
            else{
                tags = sharedPref.getString(context.getString(R.string.tags), "");
            }

            int r1 = ((RadioButton)rg.getChildAt(0)).getId();
            int r2 = ((RadioButton)rg.getChildAt(1)).getId();
            int r3 = ((RadioButton)rg.getChildAt(2)).getId();

            if (sharedPref.getInt(context.getString(R.string.wall_provider), r3) == r3){
                /*
                 ********* Unsplash Area *********
                 */
                String url;
                if (sharedPref.getInt(context.getString(R.string.just_hd), 0) == 0){
                    url = "https://source.unsplash.com/featured/?";
                }else{
                    url = "https://source.unsplash.com/1080x1920/?";
                }
                setWall(url+tags);
            }
            else if (sharedPref.getInt(context.getString(R.string.wall_provider), r3) == r2){
                /*
                 ********* PixaBay Area *********
                 */
                String PIXABAY_API_KEY = new Constants().PIXABAY_API_KEY;
                final ArrayList<String> pixabay_urls = new ArrayList<>();
                String pixabay_url = "https://pixabay.com/api/?key="+ PIXABAY_API_KEY +"&image_type=photo&q="+tags+"&orientation=vertical&per_page=200&pretty=true";

                JsonObjectRequest getRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, pixabay_url, null,
                        new com.android.volley.Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                try {
                                    JSONArray pixabay_array = response.getJSONArray("hits");
                                    for (int i=0;i< pixabay_array.length();i++){
                                        JSONObject temp_obj = pixabay_array.getJSONObject(i);
                                        pixabay_urls.add(temp_obj.getString("largeImageURL"));
                                    }
                                    setWall(pixabay_urls.get(new Random().nextInt(pixabay_urls.size())));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new com.android.volley.Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                queue.add(getRequest);
            }
            else if (sharedPref.getInt(context.getString(R.string.wall_provider), r3) == r1){
                /*
                 ********* Pexels Area *********
                 */
                final String PEXEL_API = new Constants().PEXEL_API;
                String pexel_url = "https://api.pexels.com/v1/search?query="+tags+"&per_page=39&page=5";
                final ArrayList<String> pexel_urls = new ArrayList<>();
                JsonObjectRequest getRequest2 = new JsonObjectRequest(com.android.volley.Request.Method.GET, pexel_url, null,
                        new com.android.volley.Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                try {
                                    JSONArray pixabay_array = response.getJSONArray("photos");
                                    for (int i=0;i< pixabay_array.length();i++){
                                        JSONObject temp_obj = pixabay_array.getJSONObject(i);
                                        JSONObject temp_2 = temp_obj.getJSONObject("src");
                                        pexel_urls.add(temp_2.getString("portrait"));
                                    }
                                    setWall(pexel_urls.get(new Random().nextInt(pexel_urls.size())));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new com.android.volley.Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", PEXEL_API);
                        return params;
                    }
                };
                queue.add(getRequest2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setWall(String Url){
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        imageLoader.loadImage(Url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                try{
                    if (sharedPref.getInt(context.getString(R.string.blur_on), 0) == 1){
                        wallpaperManager.setBitmap(blurIt(loadedImage, 2f));
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            wallpaperManager.setBitmap((blurIt(loadedImage, 2f)), null, true, WallpaperManager.FLAG_LOCK);
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
                    editor.apply();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public Bitmap blurIt(Bitmap original, float radius){
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
