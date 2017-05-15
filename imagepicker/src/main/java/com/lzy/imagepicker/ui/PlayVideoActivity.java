/*
 * Copyright (C) 2016 LingDaNet.Co.Ltd. All Rights Reserved.
 */
package com.lzy.imagepicker.ui;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.lzy.imagepicker.R;

public class PlayVideoActivity extends Activity implements View.OnClickListener {

    public static final String TYPE_VIDEO_PATH = "video_path";
    public static final String TYPE_AUTO_FINISH = "activity_auto_finish";
    private ImageView mBackImg;
    private VideoView mVideoView;
    private boolean autoFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //不显示程序的标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //不显示系统的标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_play_video);
        mBackImg = (ImageView) findViewById(R.id.back_icon_img);
        mVideoView = (VideoView) findViewById(R.id.video_view_vv);
        initData();
    }

    private void initData() {
        String path = getIntent().getStringExtra(TYPE_VIDEO_PATH);
        autoFinish = getIntent().getBooleanExtra(TYPE_AUTO_FINISH, false);
        if (!TextUtils.isEmpty(path)) {
            Uri uri = Uri.parse(path);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setOnCompletionListener(new OnMediaCompleteListener());
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
        mBackImg.setOnClickListener(this);
    }

    private class OnMediaCompleteListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(PlayVideoActivity.this, R.string.media_play_complete, Toast.LENGTH_SHORT).show();
            if (autoFinish) {
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_icon_img) {
            finish();
        }
    }
}
