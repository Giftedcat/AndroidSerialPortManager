package com.giftedcat.serialportlibrary.thread;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.giftedcat.serialportlibrary.SerialPortManager;
import com.giftedcat.serialportlibrary.utils.DataUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by giftedcat on 2020/6/13.
 * 串口消息读取线程
 */

public abstract class SerialPortReadThread extends Thread {

    public abstract void onDataReceived(byte[] bytes);

    private static final String TAG = SerialPortReadThread.class.getSimpleName();
    private InputStream mInputStream;
    private byte[] mReadBuffer;

    byte[] readBytes = null;

    private int readType;

    public SerialPortReadThread(InputStream inputStream, int readType) {
        mInputStream = inputStream;
        mReadBuffer = new byte[1024];

        this.readType = readType;
    }

    @Override
    public void run() {
        super.run();

        switch (readType){
            case SerialPortManager.NORMAL:
                /** 常规读取*/
                normalRead();
                break;
            case SerialPortManager.SPLICING:
                /** 轮询读取*/
                splicingRead();
                break;
        }
    }

    /**
     * 一般使用,等待inputStream卡死返回数据
     * */
    private void normalRead(){
        while (!isInterrupted()) {
            try {
                if (null == mInputStream) {
                    return;
                }

                Log.i(TAG, "run: ");
                int size = mInputStream.read(mReadBuffer);

                if (-1 == size || 0 >= size) {
                    return;
                }

                byte[] readBytes = new byte[size];

                System.arraycopy(mReadBuffer, 0, readBytes, 0, size);

                Log.i(TAG, "run: readBytes = " + new String(readBytes));
                onDataReceived(readBytes);

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 轮询读取，判断inputStream中是否还有数据，还有就拼接
     * */
    private void splicingRead(){
        while (!isInterrupted()) {
            if (null == mInputStream) {
                return;
            }

            Log.i(TAG, "run: ");
            int size = 0;

            try {
                /** 获取流中数据的量*/
                int i = mInputStream.available();
                if (i == 0) {
                    size = 0;
                } else {
                    /** 流中有数据，则添加到临时数组中*/
                    size = mInputStream.read(mReadBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (size > 0) {
                /** 发现有信息后就追加到临时变量*/
                Log.i("SerialPortReadThread", size + "");
                readBytes = DataUtil.arrayAppend(readBytes, mReadBuffer, size);
                Log.i("SerialPortReadThread", DataUtil.bytesToHexString(readBytes, readBytes.length));
            } else {
                /** 没有需要追加的数据了，回调*/
                if (readBytes != null) {
                    onDataReceived(readBytes);
                }

                /** 清空，等待下个信息单元*/
                readBytes = null;
            }

            SystemClock.sleep(50);

        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * 关闭线程 释放资源
     */
    public void release() {
        interrupt();

        if (null != mInputStream) {
            try {
                mInputStream.close();
                mInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
