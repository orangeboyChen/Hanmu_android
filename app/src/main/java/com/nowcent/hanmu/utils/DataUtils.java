package com.nowcent.hanmu.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class DataUtils {
    public static void saveImei(Context context, String string){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imei", string);
        editor.apply();
    }

    public static String getImei(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("imei", null);
    }

}
