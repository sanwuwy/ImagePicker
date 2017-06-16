package com.cjt2325.cameralibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static String storagePath = "";
    private static String DST_FOLDER_NAME = "PlayCamera";

    // 程序主文件夹（内部存储区）
    public static final String DEFAULT_INTERNAL_PATH = "/data/data/com.netposa.cyqz/";
    // 外部存储区默认主目录
    public static final String DEFAULT_EXTERNAL_PATH = "/mnt/sdcard/";
    // 应用程序主目录（外部存储区）
    public static final String MAIN_PATH = "cyqz/";
    // 拍照目录
    public static final String CAPTURE_PATH = "picture/";
    //video 暂存目录
    public static final String VIDEO_PATH = "video/";

    private static String ROOT_PATH = DEFAULT_EXTERNAL_PATH + MAIN_PATH;

    private static String initPath() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + File.separator + DST_FOLDER_NAME;
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        return storagePath;
    }

    public static String saveBitmap(String dir, Bitmap b) {
        DST_FOLDER_NAME = dir;
        String path = initPath();
        long dataTake = System.currentTimeMillis();
        String jpegName = path + File.separator + "picture_" + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 保存图片到指定路径
     * Save image with specified size
     *
     * @param bitmap   the image what be save   目标图片
     * @param savePath 图片存储的路径
     * @param size     the file size of image   期望大小
     */
    public static String saveImage(Bitmap bitmap, String savePath, long size) {
        File takeImageFile = createFile(new File(savePath), "IMG_", ".jpg");
        if (takeImageFile != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);

            while (stream.toByteArray().length / 1024 > size && options > 6) {
                stream.reset();
                options -= 6;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
            }
            Logger.i("JCameraView", "saveImage: options = " + options);
            bitmap.recycle();

            try {
                FileOutputStream fos = new FileOutputStream(takeImageFile.getAbsolutePath());
                fos.write(stream.toByteArray());
                fos.flush();
                fos.close();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return takeImageFile.getAbsolutePath();
        }
        return "";
    }

    public static boolean hasSDCard() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @brief 获取应用程序存储根目录目录
     */
    public static String getRootPath(Context context) {
        if (!fileExists(ROOT_PATH)) {
            initDirectoryPath(context);
        }
        return ROOT_PATH;
    }

    /**
     * @param filePath 目录名称
     * @brief 判断目录是否存在
     */
    public static boolean fileExists(String filePath) {
        // if filePath is null, return false, to resolve nullpointer of File Class Constructor
        if (filePath == null) return false;

        File file = new File(filePath);
        return file.exists();
    }

    /**
     * @return
     * @brief 获取拍照存储目录
     */
    public static String getCaptureDirectoryPath(Context context) {
        String path = getRootPath(context) + CAPTURE_PATH;
        mkdirs(path);
        return path;
    }

    /**
     * @brief 获取视频目录
     */
    public static String getVideoDirectoryPath(Context context) {
        String path = getRootPath(context) + VIDEO_PATH;
        mkdirs(path);
        return path;
    }

    /**
     * @param path 目录名称
     * @return 创建成功：true，创建失败：false
     * @brief 创建目录 - 会将必要的父级目录一并创建 - 如果该目录已经存在，则直接返回true
     */
    public static boolean mkdirs(String path) {
        if (path == null) return false;

        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * 初始化应用目录
     */
    public static void initDirectoryPath(Context context) {
        if (hasSDCard()) {
            ROOT_PATH = getExternalROOTPath();
        } else {
            ROOT_PATH = getInternalROOTPath(context);
        }
    }

    public static String getExternalROOTPath() {
        return getExternalStorage() + MAIN_PATH;
    }

    public static String getInternalROOTPath(Context context) {
        //return MyApplication.getInstance().getFilesDir() + Constants.MAIN_PATH;
        return getInternalFilesDir(context);
    }

    /**
     * @return 应用程序外部存储区目录，目录以"/"结尾
     * @brief 获取应用程序外部存储区的目录
     */
    public static String getExternalStorage() {
        String externalPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        if (externalPath == null || "".equals(externalPath.trim()) || !FileUtil.fileExists(externalPath)) {
            externalPath = DEFAULT_EXTERNAL_PATH;
        } else if (!externalPath.endsWith("/")) {
            externalPath += "/";
        }
        return externalPath;
    }

    /**
     * @brief 获取内部存放文件的目录
     * @detail /data/data/com.netposa.cyqz/files/
     */
    public static String getInternalFilesDir(Context context) {
        File dir = context.getFilesDir();
        if (dir == null || !dir.exists()) {
            dir = new File(getInternalStorage(context), "files");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        String filesDir = dir.getAbsolutePath();
        if (!filesDir.endsWith("/")) {
            filesDir += "/";
        }
        return filesDir;
    }

    /**
     * @return 内部存储区数据目录，目录以"/"结尾
     * @brief 获取应用程序内部存储区的数据目录
     */
    public static String getInternalStorage(Context context) {
        String internalPath = context.getApplicationInfo().dataDir;
        if (internalPath == null || "".equals(internalPath.trim())) {
            internalPath = DEFAULT_INTERNAL_PATH;
        } else if (!internalPath.endsWith("/")) {
            internalPath += "/";
        }
        return internalPath;
    }
}
