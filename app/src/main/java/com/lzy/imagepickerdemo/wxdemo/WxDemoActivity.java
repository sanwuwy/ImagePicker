package com.lzy.imagepickerdemo.wxdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.cjt2325.cameralibrary.JCameraView;
import com.lzy.imagepicker.ImageDataSource;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.MediaItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewDelActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.lzy.imagepickerdemo.R;
import com.lzy.imagepickerdemo.SelectDialog;
import com.lzy.imagepickerdemo.imageloader.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ikkong （ikkong@163.com），修改 jeasonlzy（廖子尧）
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：微信图片选择的Adapter, 感谢 ikkong 的提交
 * ================================================
 */
public class WxDemoActivity extends AppCompatActivity implements ImagePickerAdapter.OnRecyclerViewItemClickListener {

    public static final int IMAGE_ITEM_ADD = -1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;

    private ImagePickerAdapter adapter;
    private ArrayList<MediaItem> mSelectImageList; //当前选择的所有图片
    private ArrayList<MediaItem> mSelectVideoList; //当前选择的所有视频文件
    private int maxImgCount = 3;               //允许选择媒体文件最大数
    private int maxVideoCount = 1;             //允许选择的视频文件最大数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxdemo);

        //最好放到 Application oncreate执行
        initImagePicker();
        initWidget();
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(false);                      //显示拍照按钮
        imagePicker.setCrop(true);                           //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setMediaLimit(maxImgCount);              //选中媒体文件数量限制
        imagePicker.setVideoLimit(maxVideoCount);             //选中视频数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }

    private void initWidget() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSelectImageList = new ArrayList<>();
        mSelectVideoList = new ArrayList<>();
        adapter = new ImagePickerAdapter(this, mSelectImageList, maxImgCount);
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private SelectDialog showDialog(SelectDialog.SelectDialogListener listener, List<String> names) {
        SelectDialog dialog = new SelectDialog(this, R.style
                .transparentFrameWindowStyle,
                listener, names);
        if (!this.isFinishing()) {
            dialog.show();
        }
        return dialog;
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case IMAGE_ITEM_ADD:
                List<String> names = new ArrayList<>();
                names.add("拍照");
                names.add("相册");
                showDialog(new SelectDialog.SelectDialogListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //打开选择,本次允许选择的数量
                        int videoCount = maxVideoCount - mSelectVideoList.size();
                        ImagePicker.getInstance().setVideoLimit(videoCount);
                        ImagePicker.getInstance().setMediaLimit(maxImgCount - mSelectImageList.size());
                        Intent intent = new Intent(WxDemoActivity.this, ImageGridActivity.class);
                        if (videoCount <= 0) {
                            intent.putExtra(ImageGridActivity.EXTRAS_LOAD_TYPE, ImageDataSource.LOADER_ALL_IMAGE);
                            intent.putExtra(JCameraView.TYPE_CAPTURE, true);
                        } else {
                            intent.putExtra(ImageGridActivity.EXTRAS_LOAD_TYPE, ImageDataSource.LOADER_ALL);
                        }
                        /* 如果需要进入选择的时候显示已经选中的图片，
                         * 详情请查看ImagePickerActivity
                         * */
//                        intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, mSelectImageList);
//                        intent.putExtra(ImageGridActivity.EXTRAS_VIDEOS, mSelectVideoList);
                        if (position == 0) { // 直接调起相机
                            /**
                             * 0.4.7 目前直接调起相机不支持裁剪，如果开启裁剪后不会返回图片，请注意，后续版本会解决
                             *
                             * 但是当前直接依赖的版本已经解决，考虑到版本改动很少，所以这次没有上传到远程仓库
                             *
                             * 如果实在有所需要，请直接下载源码引用。
                             */
                            //打开选择,本次允许选择的数量
                            intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true); // 是否是直接打开相机
                        }
                        startActivityForResult(intent, REQUEST_CODE_SELECT);
                    }
                }, names);


                break;
            default:
                //打开预览
                Intent intentPreview = new Intent(this, ImagePreviewDelActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<MediaItem>) adapter.getImages());
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
                break;
        }
    }

    ArrayList<MediaItem> images = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                images = (ArrayList<MediaItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                ArrayList<MediaItem> videos = (ArrayList<MediaItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_VIDEOS);
//                mSelectImageList.clear();
//                mSelectVideoList.clear();
                if (images != null) {
                    mSelectImageList.addAll(images);
                    adapter.setImages(mSelectImageList);
                }
                if (videos != null) {
                    mSelectVideoList.addAll(videos);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                images = (ArrayList<MediaItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                ArrayList<MediaItem> videos = (ArrayList<MediaItem>) data.getSerializableExtra(ImagePicker.EXTRA_VIDEO_ITEMS);
                mSelectImageList.clear();
                mSelectVideoList.clear();
                if (images != null) {
                    mSelectImageList.addAll(images);
                    adapter.setImages(mSelectImageList);
                }
                if (videos != null) {
                    mSelectVideoList.addAll(videos);
                }
            }
        }
    }
}
