package com.wh.extendexoplayer.extendexoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.wh.extendexoplayer.renderers.NormalRectRenderer;
import com.wh.extendexoplayer.widget.RendererView;
import com.wh.extendexoplayer.widget.TextureRendererView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextureRendererView textureView = findViewById(R.id.bt_view);
        RendererView.Renderer renderer = new NormalRectRenderer(true);
        textureView.setRenderer(renderer);
    }
}
