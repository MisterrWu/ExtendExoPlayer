package com.wh.extendexoplayer.extendexoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.wh.extendexoplayer.renderers.NormalRectRenderer;
import com.wh.extendexoplayer.widget.TextureRendererView;

public class VideoPlayerActivity extends Activity {

    private static final String DATA = "data";
    private int videoViewHeight;
    private DefaultDataSourceFactory dataSourceFactory;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(DATA, path);
        context.startActivity(intent);
    }

    private String mVideoPath;

    private SimpleExoPlayer exoPlayer;
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
            int screenWith = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels:metrics.heightPixels;
            videoViewHeight = (int) (screenWith / (16 / 9f));
        }
        ViewGroup.LayoutParams params = rendererView.getLayoutParams();
        params.height = videoViewHeight;
        //创建轨道选择器实例
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        //创建播放器
        //直播必须使用P2pRenderersFactory，否则音频无法解码
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), trackSelector);
        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "ExoPlayer"));

        NormalRectRenderer renderer = new NormalRectRenderer(true);
        rendererView.setRenderer(renderer);
        renderer.setVideoComponent(exoPlayer.getVideoComponent());
        loadPrevImage(renderer);
    }

    private void loadPrevImage(NormalRectRenderer renderer) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try{
            retriever.setDataSource(mVideoPath);
            bitmap = retriever.getFrameAtTime();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            retriever.release();
            if(bitmap != null) {
                renderer.setFirstBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.stop();
    }

    private void openPlayer(Context context) {
        //playerView.changeSource(mediaSource);
        Uri mp4VideoUri = Uri.parse(mVideoPath);
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mp4VideoUri);
        exoPlayer.prepare(videoSource);
    }

    public void startPlay(View view) {
        if(exoPlayer.getPlaybackState() == 1
                || exoPlayer.getPlaybackState() == 4){
            openPlayer(this);
        }
        exoPlayer.setPlayWhenReady(true);
    }

    public void pausePlay(View view) {
        exoPlayer.setPlayWhenReady(false);
    }

    public void stopPLay(View view) {
        exoPlayer.stop();
    }

    public void switchScreen(View view) {
        ViewGroup.LayoutParams params = rendererView.getLayoutParams();
        int orientation = getRequestedOrientation();
        if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT){
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            params.height = videoViewHeight;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
