package com.giftedcat.serialportlibrary.thread;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.giftedcat.serialportlibrary.SerialPortManager;

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
                normalRead();
                break;
            case SerialPortManager.SPLICING:
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
                int i = mInputStream.available();
                if (i == 0) {
                    size = 0;
                } else {
                    size = mInputStream.read(mReadBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (size > 0) {
                // 发现有信息后就追加到临时变量
                Log.i("SerialPortReadThread", size + "");
                readBytes = ArrayAppend(readBytes, mReadBuffer, size);
                Log.i("SerialPortReadThread", bytesToHexString(readBytes, readBytes.length));
            } else {
                // 这次没有数据了
                if (readBytes != null) {
                    onDataReceived(readBytes);
                }

                // 清空，等待下个信息单元
                readBytes = null;
            }

            SystemClock.sleep(50); //毫秒

        }
    }

    /**
     * 转换字节为十六进制
     *
     * @param src
     * @param size
     * @return
     */
    public static String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            ret += hex;
        }
        return ret.toUpperCase(Locale.US);
    }

    /**
     * 将源数组追加到目标数组
     *
     * @param byte_1 Sou1原数组1
     * @param byte_2 Sou2原数组2
     * @param size   长度
     * @return bytestr 返回一个新的数组，包括了原数组1和原数组2
     */
    private byte[] ArrayAppend(byte[] byte_1, byte[] byte_2, int size) {
        // java 合并两个byte数组

        if (byte_1 == null && byte_2 == null) {
            return null;
        } else if (byte_1 == null) {
            byte[] byte_3 = new byte[size];
            System.arraycopy(byte_2, 0, byte_3, 0, size);
            return byte_3;
            //return byte_2;
        } else if (byte_2 == null) {
            byte[] byte_3 = new byte[byte_1.length];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            return byte_3;
            //return byte_1;
        } else {
            byte[] byte_3 = new byte[byte_1.length + size];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            System.arraycopy(byte_2, 0, byte_3, byte_1.length, size);
            return byte_3;
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
