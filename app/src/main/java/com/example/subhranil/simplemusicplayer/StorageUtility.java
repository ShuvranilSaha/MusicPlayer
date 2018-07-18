package com.example.subhranil.simplemusicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileDescriptor;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtility {
    private static String TAG = StorageUtility.class.getName();
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


    public Bitmap getAlbumArt(long album_id, Context context) {

        Log.d(TAG, "getAlbumart: " + album_id);
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        if (bm == null) {
            Log.e(TAG, "getAlbumart: it is nulll");
        } else {

            Log.e(TAG, "getAlbumart: it is not null");
        }
        return bm;
    }

}
