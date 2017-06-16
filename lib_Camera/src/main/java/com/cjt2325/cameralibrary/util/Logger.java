/*
 * Copyright (C) 2016 LingDaNet.Co.Ltd. All Rights Reserved.
 */
package com.cjt2325.cameralibrary.util;

import android.util.Log;

import com.cjt2325.cameralibrary.BuildConfig;

public final class Logger {

    public static final boolean LOG_DEBUG = BuildConfig.DEBUG;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int LEVEL = VERBOSE;

    /**
     * 默认构造函数
     * private不允许实例化.
     */
    private Logger() {

    }

    public static void v(String tag, String msg) {
        if (LOG_DEBUG) {
            if (LEVEL <= VERBOSE) {
                Log.v(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (LOG_DEBUG) {
            if (LEVEL <= DEBUG) {
                Log.d(tag, msg);
            }
        }
    }

    public static void i(String tag, String msg) {
        if (LOG_DEBUG) {
            if (LEVEL <= INFO) {
                Log.i(tag, msg);
            }
        }
    }

    public static void w(String tag, String msg) {
        if (LOG_DEBUG) {
            if (LEVEL <= WARN) {
                Log.w(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg) {
        if (LOG_DEBUG) {
            if (LEVEL <= ERROR) {
                Log.e(tag, msg);
            }
        }
    }
}
