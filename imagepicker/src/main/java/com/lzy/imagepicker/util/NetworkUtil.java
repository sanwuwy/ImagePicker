package com.lzy.imagepicker.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by xupeng on 2016/7/7.
 */
public class NetworkUtil {

    /**
     * @param context 上下文文本对象
     * @return true表示网络可用，false表示网络不可用
     * @brief 检测数据网络是否可用
     * @req MeetMeMobile1.0-ANDROIDSRS-003
     * @req MeetMeMobile1.0-ANDROIDSRS-008
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String getNetworkType(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "WIFI";
                } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "MOBILE";
                }
            }
        } catch (Exception e) {
        }
        return "NULL";
    }

}
