package com.giftedcat.serialportlibrary.listener;

/**
 * Created by Kongqw on 2017/11/14.
 * 串口消息监听
 */

public interface OnSerialPortDataListener {

    /**
     * 数据接收
     *
     * @param bytes 接收到的数据
     */
    void onDataReceived(byte[] bytes);
}
