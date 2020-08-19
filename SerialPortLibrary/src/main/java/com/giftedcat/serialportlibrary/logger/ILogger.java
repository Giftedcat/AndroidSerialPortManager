package com.giftedcat.serialportlibrary.logger;


import com.giftedcat.serialportlibrary.utils.Consts;

/**
 * Logger
 *
 * @author 徐诚聪
 * @version 1.0
 * @since 20/8/19 下午5:39
 */
public interface ILogger {

    boolean isShowLog = false;
    boolean isShowStackTrace = false;
    String defaultTag = Consts.TAG;

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String tag, String message);

    void info(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);

    void monitor(String message);

    boolean isMonitorMode();

    String getDefaultTag();
}