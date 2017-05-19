package com.cjt2325.cameralibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;

import java.io.File;

public class CaptureActivity extends AppCompatActivity {
    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    JCameraView jCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        /**
         * 关闭声音
         */
        jCameraView.forbiddenAudio(true);
        /**
         * 设置视频保存路径
         */
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        /**
         * JCameraView监听
         */
        jCameraView.setJCameraLisenter(new JCameraLisenter() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                /**
                 * 获取图片bitmap
                 */
                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
            }

            @Override
            public void recordSuccess(String url) {
                /**
                 * 获取视频路径
                 */
                Log.i("CJT", "url = " + url);
            }

            @Override
            public void quit() {
                /**
                 * 退出按钮
                 */
                CaptureActivity.this.finish();
            }
        });
        /**
         * 6.0动态权限获取
         */
        getPermissions();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        jCameraView.setFocusViewWidthAnimation(jCameraView.getMeasuredWidth() / 2, jCameraView.getMeasuredHeight() / 2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * 全屏显示
         */
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }
    /**
     * 获取权限
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(CaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(CaptureActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat
                    .checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //具有权限
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(CaptureActivity.this, new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA},
                        GET_PERMISSION_REQUEST);
            }

        } else {
            //具有权限
        }
    }

    /**
     * 获取内存权限回调
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            if (grantResults.length >= 1) {
                //获取读写内存的权限
                int writeResult = grantResults[0];//读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (writeGranted) {
                    //具备权限
                } else {

                    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    }
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (recordPermissionGranted) {
                    //具备权限
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    }
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (cameraPermissionGranted) {
                    //具备权限
                } else {
                    //不具有相关权限
                    Toast.makeText(this, "拍照被禁止，部分功能将失效，请到设置中开启。", Toast.LENGTH_SHORT).show();

                    if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                        //如果用户勾选了不再提醒，则返回false
                        Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
