package com.example.mybudget.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mybudget.utils.JavaUtils;

public class Preferences {

    private static SharedPreferences pref;

    public static void setPreferences(Context context) {
        pref = context.getApplicationContext().getSharedPreferences("MyPref", 0);
    }

    public static <T> void putValue(String key, T value) {
        SharedPreferences.Editor editor = pref.edit();
        String className = value.getClass().getSimpleName();

        switch (className) {
            case "Integer":
                editor.putInt(key, (Integer) value);
                break;
            case "String":
                editor.putString(key, (String) value);
                break;
            case "Boolean":
                editor.putBoolean(key, (Boolean) value);
                break;
            case "Long":
                editor.putLong(key, (Long) value);
                break;
            case "Float":
                editor.putFloat(key, (Float) value);
                break;
            default:
                editor.putString(key, JavaUtils.objectToString(value));
        }
        editor.apply();
    }

    public static <T> T getValue(String key, Class<T> classs) {
        String className = classs.getSimpleName();
        switch (className) {
            case "Integer":
                return (T) Integer.valueOf(pref.getInt(key, 0));
            case "String":
                return (T) pref.getString(key, "");
            case "Boolean":
                return (T) Boolean.valueOf(pref.getBoolean(key, false));
            case "Long":
                return (T) Long.valueOf(pref.getLong(key, 0l));
            case "Float":
                return (T) Float.valueOf(pref.getFloat(key, 0f));
            default:
                return JavaUtils.stringToObject(pref.getString(key, ""), classs);
        }
    }
}
