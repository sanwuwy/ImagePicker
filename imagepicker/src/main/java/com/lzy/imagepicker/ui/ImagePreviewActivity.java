package com.lzy.imagepicker.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.bean.MediaItem;
import com.lzy.imagepicker.view.SuperCheckBox;

import java.io.File;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePreviewActivity extends ImagePreviewBaseActivity implements ImagePicker.OnImageSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String ISORIGIN = "isOrigin";

    private boolean isOrigin;                      //是否选中原图
    private SuperCheckBox mCbCheck;                //是否选中当前图片的CheckBox
    private SuperCheckBox mCbOrigin;               //原图
    private Button mBtnOk;                         //确认图片的选择
    private View bottomBar;
    private ImageView playIcon;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();

        isOrigin = getIntent().getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
        imagePicker.addOnImageSelectedListener(this);

        mBtnOk = (Button) topBar.findViewById(R.id.btn_ok);
        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setOnClickListener(this);

        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility(View.VISIBLE);

        mCbCheck = (SuperCheckBox) findViewById(R.id.cb_check);
        mCbOrigin = (SuperCheckBox) findViewById(R.id.cb_origin);
        mCbOrigin.setText(getString(R.string.origin));
        mCbOrigin.setOnCheckedChangeListener(this);
        mCbOrigin.setChecked(isOrigin);

        //初始化当前页面的状态
        onImageSelected(0, null, false);
        MediaItem item = mMediaItems.get(mCurrentPosition);
        playIcon = (ImageView) findViewById(R.id.play_icon);
        String mimeType = item.mimeType;
        if (mimeType != null && mimeType.startsWith("video")) {
            playIcon.setVisibility(View.VISIBLE);
            playIcon.setOnClickListener(this);
        }
        boolean isSelected = imagePicker.isSelectMedia(item);
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mMediaItems.size()));
        mCbCheck.setChecked(isSelected);
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                playIcon.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                MediaItem item = mMediaItems.get(mCurrentPosition);
                String mimeType = item.mimeType;
                if (mimeType != null && mimeType.startsWith("video")) {
                    playIcon.setVisibility(View.VISIBLE);
                } else {
                    playIcon.setVisibility(View.GONE);
                }
                boolean isSelected = imagePicker.isSelectMedia(item);
                mCbCheck.setChecked(isSelected);
                mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mMediaItems.size()));

            }
        });
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaItem mediaItem = mMediaItems.get(mCurrentPosition);
                File file = new File(mediaItem.path);
                if (!file.exists()) {
                    Toast.makeText(mContext,
                            ImagePreviewActivity.this.getApplicationContext().getString(R.string.file_not_exist),
                            Toast.LENGTH_SHORT).show();
                    mCbCheck.setChecked(false);
                    return;
                }
                int selectLimit = imagePicker.getMediaLimit();
                int videoLimit = imagePicker.getVideoLimit();
                String mimeType = mediaItem.mimeType;
                if (mimeType.startsWith("video")) {
                    if (mediaItem.duration > 10500) {
                        Toast.makeText(getApplicationContext(), getString(R.string.video_too_long), Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                        return;
                    }
                    if (!mimeType.equals("video/mp4") || !mediaItem.path.endsWith(".mp4")) { // 判断是否是 mp4 文件
                        Toast.makeText(getApplicationContext(), getString(R.string.not_support_video), Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                        return;
                    }
                    if (mCbCheck.isChecked() && selectedVideos.size() >= videoLimit) {
                        Toast.makeText(ImagePreviewActivity.this, ImagePreviewActivity.this.getString(R.string.video_limit, videoLimit), Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                    } else {
                        if (mCbCheck.isChecked() && selectedImages.size() >= selectLimit) {
                            Toast.makeText(ImagePreviewActivity.this, ImagePreviewActivity.this.getString(R.string.select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                            mCbCheck.setChecked(false);
                        } else {
                            imagePicker.addSelectedMediaItem(mCurrentPosition, mediaItem, mCbCheck.isChecked());
                            imagePicker.addSelectedVideoItem(mCurrentPosition, mediaItem, mCbCheck.isChecked());
                        }
                    }
                } else {
                    if (!imagePicker.isSupportImage(mediaItem)) {
                        Toast.makeText(mContext, mContext.getString(R.string.not_support_image), Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                        return;
                    }
                    if (mCbCheck.isChecked() && selectedImages.size() >= selectLimit) {
                        Toast.makeText(ImagePreviewActivity.this, ImagePreviewActivity.this.getString(R.string.select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                    } else {
                        imagePicker.addSelectedMediaItem(mCurrentPosition, mediaItem, mCbCheck.isChecked());
                    }
                }
            }
        });
    }

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedMediaItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    @Override
    public void onImageSelected(int position, MediaItem item, boolean isAdd) {
        if (imagePicker.getSelectMediaCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, imagePicker.getSelectMediaCount(), imagePicker.getMediaLimit()));
            mBtnOk.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
        }

        if (mCbOrigin.isChecked()) {
            long size = 0;
            for (MediaItem mediaItem : selectedImages)
                size += mediaItem.size;
            String fileSize = Formatter.formatFileSize(this, size);
            mCbOrigin.setText(getString(R.string.origin_size, fileSize));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedMedias());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
            finish();
        } else if (id == R.id.btn_back) {
            Intent intent = new Intent();
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            setResult(ImagePicker.RESULT_CODE_BACK, intent);
            finish();
        } else if (id == R.id.play_icon) {
            Intent intent = new Intent(this, PlayVideoActivity.class);
            MediaItem item = mMediaItems.get(mCurrentPosition);
            intent.putExtra(PlayVideoActivity.TYPE_VIDEO_PATH, item.path);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
        setResult(ImagePicker.RESULT_CODE_BACK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.cb_origin) {
            if (isChecked) {
                long size = 0;
                for (MediaItem item : selectedImages)
                    size += item.size;
                String fileSize = Formatter.formatFileSize(this, size);
                isOrigin = true;
                mCbOrigin.setText(getString(R.string.origin_size, fileSize));
            } else {
                isOrigin = false;
                mCbOrigin.setText(getString(R.string.origin));
            }
        }
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    /** 单击时，隐藏头和尾 */
    @Override
    public void onImageSingleTap() {
        if (topBar.getVisibility() == View.VISIBLE) {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_out));
            bottomBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
            topBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            tintManager.setStatusBarTintResource(R.color.transparent);//通知栏所需颜色
            //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_in));
            bottomBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            topBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            tintManager.setStatusBarTintResource(R.color.status_bar);//通知栏所需颜色
            //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
