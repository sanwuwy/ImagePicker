package com.lzy.imagepicker.adapter;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.bean.MediaItem;
import com.lzy.imagepicker.ui.ImageBaseActivity;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.view.SuperCheckBox;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 加载相册图片的RecyclerView适配器
 *
 * 用于替换原项目的GridView，使用局部刷新解决选中照片出现闪动问题
 *
 * 替换为RecyclerView后只是不再会导致全局刷新，
 *
 * 但还是会出现明显的重新加载图片，可能是picasso图片加载框架的问题
 *
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-04-05  10:04
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {


    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
    private ImagePicker imagePicker;
    private Activity mActivity;
    private ArrayList<MediaItem> medias;            //当前需要显示的所有的媒体文件数据
    private ArrayList<MediaItem> mSelectedImages;   //全局保存的已经选中的媒体文件数据
    private ArrayList<MediaItem> mSelectedVideos;   //全局保存的已经选中的视频文件数据
    private boolean isShowCamera;         //是否显示拍照按钮
    private int mImageSize;               //每个条目的大小
    private LayoutInflater mInflater;
    private OnImageItemClickListener listener;   //图片被点击的监听

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(View view, MediaItem mediaItem, int position);
    }

    public void refreshData(ArrayList<MediaItem> images) {
        if (images == null || images.size() == 0) this.medias = new ArrayList<>();
        else this.medias = images;
        notifyDataSetChanged();
    }

    /**
     * 构造方法
     */
    public ImageRecyclerAdapter(Activity activity, ArrayList<MediaItem> medias) {
        this.mActivity = activity;
        if (medias == null || medias.size() == 0) this.medias = new ArrayList<>();
        else this.medias = medias;

        mImageSize = Utils.getImageItemWidth(mActivity);
        imagePicker = ImagePicker.getInstance();
        isShowCamera = imagePicker.isShowCamera();
        mSelectedImages = imagePicker.getSelectedMedias();
        mSelectedVideos = imagePicker.getSelectedVideos();
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CAMERA){
            return new CameraViewHolder(mInflater.inflate(R.layout.adapter_camera_item,parent,false));
        }
        return new ImageViewHolder(mInflater.inflate(R.layout.adapter_image_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder){
            ((CameraViewHolder)holder).bindCamera();
        }else if (holder instanceof ImageViewHolder){
            ((ImageViewHolder)holder).bind(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return isShowCamera ? medias.size() + 1 : medias.size();
    }

    public MediaItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return medias.get(position - 1);
        } else {
            return medias.get(position);
        }
    }

    private class ImageViewHolder extends ViewHolder{

        View rootView;
        ImageView ivThumb;
        View mask;
        SuperCheckBox cbCheck;
        ImageView videoIcon;
        TextView videoDuration;

        ImageViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            ivThumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
            mask = itemView.findViewById(R.id.mask);
            cbCheck = (SuperCheckBox) itemView.findViewById(R.id.cb_check);
            videoIcon = (ImageView) itemView.findViewById(R.id.video_icon);
            videoDuration = (TextView) itemView.findViewById(R.id.video_duration);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
        }

        void bind(final int position){
            final MediaItem mediaItem = getItem(position);
            final String mimeType = mediaItem.mimeType;
            videoIcon.setVisibility(View.GONE);
            videoDuration.setVisibility(View.GONE);
            if (mimeType.startsWith("video")) {
                videoIcon.setVisibility(View.VISIBLE);
                videoDuration.setVisibility(View.VISIBLE);
                videoDuration.setText(formatDuration(mediaItem.duration));
            }
            ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onImageItemClick(rootView, mediaItem, position);
                }
            });
            cbCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mediaLimit = imagePicker.getMediaLimit();
                    int videoLimit = imagePicker.getVideoLimit();
                    if (mimeType.startsWith("video")) {
                        if (cbCheck.isChecked() && mSelectedVideos.size() >= videoLimit) {
                            Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.video_limit, videoLimit), Toast.LENGTH_SHORT).show();
                            cbCheck.setChecked(false);
                            mask.setVisibility(View.GONE);
                        } else {
                            if (cbCheck.isChecked() && mSelectedImages.size() >= mediaLimit) {
                                Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.select_limit, mediaLimit), Toast.LENGTH_SHORT).show();
                                cbCheck.setChecked(false);
                                mask.setVisibility(View.GONE);
                            } else {
                                imagePicker.addSelectedMediaItem(position, mediaItem, cbCheck.isChecked());
                                imagePicker.addSelectedVideoItem(position, mediaItem, cbCheck.isChecked());
                                mask.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        if (cbCheck.isChecked() && mSelectedImages.size() >= mediaLimit) {
                            Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.select_limit, mediaLimit), Toast.LENGTH_SHORT).show();
                            cbCheck.setChecked(false);
                            mask.setVisibility(View.GONE);
                        } else {
                            imagePicker.addSelectedMediaItem(position, mediaItem, cbCheck.isChecked());
                            mask.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            //根据是否多选，显示或隐藏checkbox
            if (imagePicker.isMultiMode()) {
                cbCheck.setVisibility(View.VISIBLE);
                boolean checked = mSelectedImages.contains(mediaItem);
                if (checked) {
                    mask.setVisibility(View.VISIBLE);
                    cbCheck.setChecked(true);
                } else {
                    mask.setVisibility(View.GONE);
                    cbCheck.setChecked(false);
                }
            } else {
                cbCheck.setVisibility(View.GONE);
            }
            imagePicker.getImageLoader().displayImage(mActivity, mediaItem.path, ivThumb, mImageSize, mImageSize); //显示图片
        }

    }

    private String formatDuration(long duration) {
        int length = Math.round(duration / 1000.0f);
        int min = length % 3600 / 60;
        int second = length % 60;
        return String.format(Locale.CHINA, "%d:%02d", min, second);
    }

    private class CameraViewHolder extends ViewHolder{

        View mItemView;

        CameraViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        void bindCamera(){
            mItemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
            mItemView.setTag(null);
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((ImageBaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, ImageGridActivity.REQUEST_PERMISSION_CAMERA);
                    } else {
                        imagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE);
                    }
                }
            });
        }
    }
}
