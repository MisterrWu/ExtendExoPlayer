package com.wh.extendexoplayer.extendexoplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private final int REQUEST_CODE = 0x100;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ListView videoList = findViewById(R.id.video_list);
        mAdapter = new ArrayAdapter<String>(this, R.layout.textview);
        videoList.setAdapter(mAdapter);
        videoList.setOnItemClickListener(this);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            initData(this);
        }else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(REQUEST_CODE == requestCode && permissions.length > 0 && grantResults.length > 0){
            if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[0])
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initData(this);
            } else {
                finish();
            }
        }
    }

    private void initData(Context context) {
        mAdapter.addAll(getLocalVideo(context));
    }

    public List<String> getLocalVideo(Context context) {
        List<String> videos = new ArrayList<>();
        Uri originalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        String selection = MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"video/mp4"};
        Cursor cursor = cr.query(originalUri, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                    videos.add(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return videos;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = mAdapter.getItem(position);
        VideoPlayerActivity.start(this,path);
    }
}
