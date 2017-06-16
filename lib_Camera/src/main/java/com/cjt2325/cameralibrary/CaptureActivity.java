package com.cjt2325.cameralibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cjt2325.cameralibrary.lisenter.ErrorLisenter;
import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;
import com.cjt2325.cameralibrary.util.DeviceUtil;
import com.cjt2325.cameralibrary.util.FileUtil;
import com.cjt2325.cameralibrary.util.Logger;

public class CaptureActivity extends AppCompatActivity {
    public static final String EXTRA_CAPTURE_ITEM = "extra_capture_item";
    public static final String EXTRA_IS_VIDEO = "extra_is_video";
    private JCameraView jCameraView;
    private String saveVideoPath;
    private String savePicturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CameraInterface.getInstance().setContext(this);
        setContentView(R.layout.activity_capture);

        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        saveVideoPath = FileUtil.getVideoDirectoryPath(getApplication());
        savePicturePath = FileUtil.getCaptureDirectoryPath(getApplication());
        //设置视频保存路径
        jCameraView.setSaveVideoPath(saveVideoPath);
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraLisenter() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                Logger.i("JCameraView", "width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
                Logger.i("JCameraView", "start time = " + System.currentTimeMillis());
                String url = FileUtil.saveImage(bitmap, savePicturePath, 300);
                Logger.i("JCameraView", "end time = " + System.currentTimeMillis());
                Logger.i("JCameraView", "url = " + url);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CAPTURE_ITEM, url);
                intent.putExtra(EXTRA_IS_VIDEO, false);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url) {
                //获取视频路径
                Logger.i("JCameraView", "url = " + url);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CAPTURE_ITEM, url);
                intent.putExtra(EXTRA_IS_VIDEO, true);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void quit() {
                //退出按钮
                CaptureActivity.this.finish();
            }
        });
        jCameraView.setErrorLisenter(new ErrorLisenter() {
            @Override
            public void onError() {
                //错误监听
                Logger.i("JCameraView", "camera error");
                Intent intent = new Intent();
                setResult(103, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(CaptureActivity.this, "给点录音权限可以?", Toast.LENGTH_SHORT).show();
            }
        });
        Logger.i("JCameraView", DeviceUtil.getDeviceModel());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.i("JCameraView", "onStart");
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
        int typeCapture = getIntent().getIntExtra(JCameraView.TYPE_CAPTURE, JCameraView.BUTTON_STATE_BOTH);
        if (typeCapture == JCameraView.BUTTON_STATE_ONLY_CAPTURE) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);
        } else if (typeCapture == JCameraView.BUTTON_STATE_BOTH) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);
        } else if (typeCapture == JCameraView.BUTTON_STATE_ONLY_RECORDER) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
        }
    }


    @Override
    protected void onResume() {
        Logger.i("JCameraView", "onResume");
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        Logger.i("JCameraView", "onPause");
        super.onPause();
        jCameraView.onPause();
    }

}

