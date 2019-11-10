package com.wh.extendexoplayer.extendexoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.exoplayer2.util.MimeTypes;
import com.wh.extendexoplayer.soft.SoftCodec;

public class NativeTestActivity extends Activity {

    SoftCodec softCodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_test_activity);
        softCodec = SoftCodec.createByCodecName(MimeTypes.VIDEO_H265);
    }

    public void setup(View view) {
    }
}
