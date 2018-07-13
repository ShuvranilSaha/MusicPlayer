package com.example.subhranil.simplemusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtility {
    private static String TAG =StorageUtility.class.getName();
    private final String STORAGE = " com.example.subhranil.simplemusicplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtility(Context context) {
        this.context = context;
    }

    public void storeSong(ArrayList<SongFile> list) {
        Log.d(TAG, "storeSong: ");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("songArrayList", json);
        editor.apply();
    }

    public ArrayList<SongFile> loadSongs() {
        Log.d(TAG, "loadSongs: ");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("songArrayList", null);
        Type type = new TypeToken<ArrayList<SongFile>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeSongIndex(int index) {
        Log.d(TAG, "storeSongIndex: ");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("songIndex", index);
        editor.apply();
    }

    public int loadSongIndex() {
        Log.d(TAG, "loadSongIndex: ");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("songIndex", -1);//return -1 if no data found
    }

    public void clearCachedSongPlayList() {
        Log.d(TAG, "clearCachedSongPlayList: ");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
