/*
 * Copyright (C) 2016 LingDaNet.Co.Ltd. All Rights Reserved.
 */
package com.lzy.imagepicker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.lzy.imagepicker.R;

public class PlayVideoActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {

    public static final String TYPE_VIDEO_PATH = "video_path";
    public static final String TYPE_AUTO_FINISH = "activity_auto_finish";
    private ImageView mBackImg;
    private VideoView mVideoView;
    private ProgressBar mProBar;
    private boolean autoFinish;
    private Context mContext;

    // 视频播放到的位置
    int mPlayingPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        //不显示程序的标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //不显示系统的标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_play_video);
        mBackImg = (ImageView) findViewById(R.id.back_icon_img);
        mVideoView = (VideoView) findViewById(R.id.video_view_vv);
        mProBar = (ProgressBar) findViewById(R.id.loading_pb);
        initData();
    }

    private void initData() {
        String path = getIntent().getStringExtra(TYPE_VIDEO_PATH);
        autoFinish = getIntent().getBooleanExtra(TYPE_AUTO_FINISH, false);
        if (!TextUtils.isEmpty(path)) {
            Uri uri = Uri.parse(path);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setOnCompletionListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVideoView.setOnInfoListener(this);
            }
            mVideoView.setOnErrorListener(this);
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
        mBackImg.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        mPlayingPos = mVideoView.getCurrentPosition(); //先获取再stopPlay(),原因自己看源码
        mVideoView.stopPlayback();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mPlayingPos > 0) {
            //此处为更好的用户体验,可添加一个progressBar,有些客户端会在这个过程中隐藏底下控制栏,这方法也不错
            mVideoView.start();
            mVideoView.seekTo(mPlayingPos);
            mPlayingPos = 0;
        }
        super.onResume();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(PlayVideoActivity.this, R.string.media_play_complete, Toast.LENGTH_SHORT).show();
        if (autoFinish) {
            finishPage();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_icon_img) {
            finishPage();
        }
    }

    private void finishPage() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishPage();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mProBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(PlayVideoActivity.this, R.string.media_play_fail, Toast.LENGTH_SHORT).show();
        return true;
    }
}
