package com.lzy.imagepicker;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.lzy.imagepicker.bean.MediaFolder;
import com.lzy.imagepicker.bean.MediaItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：加载手机图片实现类
 * 修订历史：
 * ================================================
 */
public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         //加载所有图片和视频
    public static final int LOADER_ALL_IMAGE = 1;   //加载所有图片
    public static final int LOADER_ALL_VIDEO = 2;   //加载所有视频
    public static final int LOADER_CATEGORY = 3;    //分类加载图片
    private final String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
            MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
            MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
            MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
            MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608

    private final String[] VIDEO_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Video.Media.DISPLAY_NAME,    //视频的显示名称
            MediaStore.Video.Media.DATA,            //视频的绝对路径
            MediaStore.Video.Media.SIZE,            //视频的大小
            MediaStore.Video.Media.WIDTH,           //视频的宽度
            MediaStore.Video.Media.HEIGHT,          //视频的高度
            MediaStore.Video.Media.MIME_TYPE,       //视频的类型
            MediaStore.Video.Media.DATE_ADDED,      //视频被添加的时间
            MediaStore.Video.Media.DURATION};       //视频时长

    private FragmentActivity activity;
    private OnImagesLoadedListener loadedListener;                     //图片加载完成的回调接口
    private ArrayList<MediaFolder> mediaFolders = new ArrayList<>();   //所有的图片文件夹
    private ArrayList<MediaItem> allMedias = new ArrayList<>();   //所有媒体文件的集合,不分文件夹
    private ArrayList<MediaItem> allVideos = new ArrayList<>();   //所有视频的集合,不分文件夹

    private int loadTimes = 0;  //初始化LoaderManager的次数
    private int loadType;   //加载的文件类型

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     * @param loadedListener 图片加载完成的监听
     */
    public ImageDataSource(FragmentActivity activity, String path, OnImagesLoadedListener loadedListener, int loadType) {
        this.activity = activity;
        this.loadedListener = loadedListener;
        this.loadType = loadType;

        LoaderManager loaderManager = activity.getSupportLoaderManager();
        if (loadType == LOADER_ALL) {
            loaderManager.initLoader(LOADER_ALL_IMAGE, null, this);//加载所有的图片
            loaderManager.initLoader(LOADER_ALL_VIDEO, null, this);//加载所有的视频
        } else if (loadType == LOADER_ALL_IMAGE) {
            loaderManager.initLoader(LOADER_ALL_IMAGE, null, this);//加载所有的图片
        } else if (loadType == LOADER_ALL_VIDEO) {
            loaderManager.initLoader(LOADER_ALL_VIDEO, null, this);//加载所有的视频
        } else {
            if (TextUtils.isEmpty(path)) {
                throw new RuntimeException("指定目录不能为空");
            }
            //加载指定目录的图片
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        //扫描所有图片
        if (id == LOADER_ALL_IMAGE)
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    null, null, IMAGE_PROJECTION[6] + " DESC");
        //扫描所有视频
        if (id == LOADER_ALL_VIDEO) {
            cursorLoader = new CursorLoader(activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                    null, null, VIDEO_PROJECTION[6] + " DESC");
        }
        //扫描某个图片文件夹
        if (id == LOADER_CATEGORY)
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[6] + " DESC");

        return cursorLoader;
    }

    @Override
    public synchronized void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loadType != LOADER_ALL) {   //如果不是图片和视频都加载，则只需要加载一次
            mediaFolders.clear();
            allMedias.clear();
            allVideos.clear();
            initLoadData(data);
        } else {                        //如果是图片和视频都需要加载，则一共加载两次
            loadTimes++;
            if (loadTimes < 2) {        //第一次加载
                mediaFolders.clear();
                allMedias.clear();
                allVideos.clear();
                initLoadData(data);
                return;
            } else {                    //第二次加载
                initLoadData(data);
            }
        }
        //回调接口，通知图片数据准备完成
        ImagePicker.getInstance().setImageFolders(mediaFolders);
        loadedListener.onImagesLoaded(mediaFolders);
        loadTimes = 0;
    }

    private void initLoadData(Cursor data) {
        if (data != null) {
            while (data.moveToNext()) {
                String mediaName;
                String mediaPath;
                long mediaSize;
                int mediaWidth;
                int mediaHeight;
                String mediaMimeType;
                long mediaAddTime;
                long mediaDuration;
                MediaItem mediaItem = new MediaItem();

                //查询数据
                if (-1 == data.getColumnIndex(VIDEO_PROJECTION[7])) {
                    mediaName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    mediaPath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    mediaSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    mediaWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                    mediaHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                    mediaMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
                    mediaAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
                    mediaDuration = -1;

                    //封装实体
                    mediaItem.name = mediaName;
                    mediaItem.path = mediaPath;
                    mediaItem.size = mediaSize;
                    mediaItem.width = mediaWidth;
                    mediaItem.height = mediaHeight;
                    mediaItem.mimeType = mediaMimeType;
                    mediaItem.addTime = mediaAddTime;
                    mediaItem.duration = mediaDuration;
                    allMedias.add(mediaItem);
                } else {
                    mediaName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
                    mediaPath = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
                    mediaSize = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
                    mediaWidth = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[3]));
                    mediaHeight = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
                    mediaMimeType = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[5]));
                    mediaAddTime = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6]));
                    mediaDuration = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]));

                    //封装实体
                    mediaItem.name = mediaName;
                    mediaItem.path = mediaPath;
                    mediaItem.size = mediaSize;
                    mediaItem.width = mediaWidth;
                    mediaItem.height = mediaHeight;
                    mediaItem.mimeType = mediaMimeType;
                    mediaItem.addTime = mediaAddTime;
                    mediaItem.duration = mediaDuration;
                    allMedias.add(mediaItem);
                    allVideos.add(mediaItem);
                }


                //根据父路径分类存放图片
                File imageFile = new File(mediaPath);
                File imageParentFile = imageFile.getParentFile();
                MediaFolder mediaFolder = new MediaFolder();
                mediaFolder.name = imageParentFile.getName();
                mediaFolder.path = imageParentFile.getAbsolutePath();

                if (!mediaFolders.contains(mediaFolder)) {
                    ArrayList<MediaItem> medias = new ArrayList<>();
                    medias.add(mediaItem);
                    mediaFolder.cover = mediaItem;
                    mediaFolder.images = medias;
                    mediaFolders.add(mediaFolder);
                } else {
                    mediaFolders.get(mediaFolders.indexOf(mediaFolder)).images.add(mediaItem);
                }
            }
            if (loadType != LOADER_ALL) {
                //防止没有图片报异常
                if (allMedias.size() > 0) {
                    //构造所有图片或所有视频的集合
                    MediaFolder allMediasFolder = new MediaFolder();
                    if (loadType == LOADER_ALL_VIDEO) {
                        allMediasFolder.name = activity.getResources().getString(R.string.all_videos);
                    } else {
                        allMediasFolder.name = activity.getResources().getString(R.string.all_images);
                    }
                    allMediasFolder.path = "/";
                    allMediasFolder.cover = allMedias.get(0);
                    allMediasFolder.images = allMedias;
                    mediaFolders.add(0, allMediasFolder);  //确保第一条是所有图片
                }
            } else {
                if (loadTimes >= 2 && allMedias.size() > 0) {
                    // 构造所有图片和视频的集合
                    MediaFolder allImagesFolder = new MediaFolder();
                    allImagesFolder.name = activity.getResources().getString(R.string.all_medias);
                    allImagesFolder.path = "/";
                    allImagesFolder.cover = allMedias.get(0);
                    allImagesFolder.images = allMedias;
                    mediaFolders.add(0, allImagesFolder);  //确保第一条是所有图片和视频

                    if (allVideos.size() > 0) {
                        // 构造所有视频的集合
                        MediaFolder allVidoesFolder = new MediaFolder();
                        allVidoesFolder.name = activity.getResources().getString(R.string.all_videos);
                        allVidoesFolder.path = "/";
                        allVidoesFolder.cover = allVideos.get(0);
                        allVidoesFolder.images = allVideos;
                        mediaFolders.add(1, allVidoesFolder);  //确保第二条是所有视频
                    }

                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<MediaFolder> mediaFolders);
    }
}
