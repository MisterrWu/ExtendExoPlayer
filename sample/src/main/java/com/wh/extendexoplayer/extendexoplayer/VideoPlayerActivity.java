package com.wh.extendexoplayer.extendexoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.wh.extendexoplayer.player.DefaultExoPlayer;
import com.wh.extendexoplayer.widget.TextureRendererView;

public class VideoPlayerActivity extends Activity {

    private static final String DATA = "data";
    private int videoViewHeight;


    public static void start(Context context, String path) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(DATA, path);
        context.startActivity(intent);
    }

    private String mVideoPath;

    private DefaultExoPlayer defaultExoPlayer;
    private TextureRendererView rendererView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVideoPath = getIntent().getStringExtra(DATA);
        setContentView(R.layout.player_activity);
        rendererView = findViewById(R.id.video_container);
        WindowManager manager = getWindowManager();
        if (manager != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(metrics);
            int screenWith = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels : metrics.heightPixels;
            videoViewHeight = (int) (screenWith / (16 / 9f));
        }
        ViewGroup.LayoutParams params = rendererView.getLayoutParams();
        params.height = videoViewHeight;

        //创建播放器
        defaultExoPlayer = new DefaultExoPlayer.Builder().rendererView(rendererView).fitXY(true).build();
        defaultExoPlayer.setClearColor(0.5f,0.5f,0.5f);

        loadPrevImage();
    }

    private void loadPrevImage() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try {
            retriever.setDataSource(mVideoPath);
            bitmap = retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
            if (bitmap != null) {
                defaultExoPlayer.setFirstBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        defaultExoPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        defaultExoPlayer.stop();
    }

    private void openPlayer(Context context) {
        defaultExoPlayer.prepareSource(mVideoPath);
    }

    public void startPlay(View view) {
        if (defaultExoPlayer.getPlaybackState() == 1
                || defaultExoPlayer.getPlaybackState() == 4) {
            openPlayer(this);
        }
        defaultExoPlayer.start();
    }

    public void pausePlay(View view) {
        defaultExoPlayer.pause();
    }

    public void stopPLay(View view) {
        defaultExoPlayer.stop();
    }

    public void switchScreen(View view) {
        ViewGroup.LayoutParams params = rendererView.getLayoutParams();
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            params.height = videoViewHeight;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
