package com.example.subhranil.simplemusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.subhranil.simplemusicplayer.PlayNewSong";
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.example.subhranil.simplemusicplayer.CURRENT_MEDIA_DESCRIPTION";
    private MediaPlayerService player;
    private static final String TAG = MainActivity.class.getName();
    boolean serviceBound = false;

    ArrayList<SongFile> songList;
    private int STORAGE_PERMISSION_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (isReadStorageAllowed()) {
            Toast.makeText(this, "You already granted the permissions", Toast.LENGTH_SHORT).show();
            loadSongs(this);
            initialiseRecyclerView();
            return;
        } else {
            requestStoragePermission();
        }

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */
    }

    //toCheckStoragePermission Given or not
    private boolean isReadStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;

    }

    // requesting Permission
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //reason for permission
        }
        // finally ask for permission again
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        //check the requestCode of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted to read External Storage", Toast.LENGTH_LONG).show();
                loadSongs(this);

                initialiseRecyclerView();
            } else {
                Toast.makeText(this, "Permission Dennied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initialiseRecyclerView() {
        Log.d(TAG, "initialiseRecyclerView: ");
        if (songList.size() > 0) {

            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(songList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            LayoutAnimationController controller = null;
            controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
            //set anim
            recyclerView.setLayoutAnimation(controller);
            //  recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();

            recyclerView.addOnItemTouchListener(new CustomTouchListener(this,
                    new onItemClickListener() {
                        @Override
                        public void onClick(View view, int index) {
                            Intent intent = new Intent(MainActivity.this, songPlayingFull.class);
                            intent.putExtra("index", index);
                            startActivity(intent);
                            playSong(index);
                        }
                    }));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    public void playSong(int songIndex) {
        Log.d(TAG, "playSong: ");
        if (!serviceBound) {
            StorageUtility storage = new StorageUtility(getApplicationContext());
            storage.storeSong(songList);
            storage.storeSongIndex(songIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            StorageUtility storage = new StorageUtility(getApplicationContext());
            storage.storeSongIndex(songIndex);

            Intent broadCastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadCastIntent);
        }
    }

    public void loadSongs(Context context) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";


        Cursor cursor = contentResolver.query(
                uri,
                null,
                selection,
                null,
                sortOrder
        );
        if (cursor != null && cursor.getCount() > 0) {
            songList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long albumId2 = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Log.e(TAG, "loadSongs: 1 : " + albumId);
                Log.e(TAG, "loadSongs: 2  : " + albumId2);

                songList.add(new SongFile(data, title, album, artist, albumId));

            }
        }
        cursor.close();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            // stop the service
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }
}
