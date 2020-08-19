package com.giftedcat.serialport.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.giftedcat.serialportlibrary.SerialPortManager;
import com.giftedcat.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;


/**
 * Created by GiftedCat on 2020/6/13.
 * 串口数据交互的Service
 */
public class SerialInteractiveService extends Service {

    private String TAG = "SerialInteractiveService";

    /**
     * 发送数据
     */
    private Messenger mClient;

    /**
     * 接收数据
     */
    private Messenger mServer = new Messenger(new MyHandler());

    private SerialPortManager mSerialPortManager1, mSerialPortManager2;

    @Override
    public void onCreate() {
        super.onCreate();

        mSerialPortManager1 = new SerialPortManager(new File("/dev/ttyS3"), 9600, SerialPortManager.SPLICING)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        sendMessageToClient(101, new String(bytes));
                    }
                });
        mSerialPortManager2 = new SerialPortManager(new File("/dev/ttyS2"), 9600)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        sendMessageToClient(102, new String(bytes));
                    }
                });
    }

    /**
     * 发送数据函数
     *
     * @param code 状态码
     * @param data 数据
     */
    private void sendMessageToClient(int code, String data) {
        Message message = Message.obtain(null, code);
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        message.setData(bundle);
        try {
            mClient.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            switch (msg.what) {
                /** 初始化成功*/
                case 200:
                    Message message = Message.obtain(null, 100, 1, 1);
                    bundle.putString("key", "client create success");
                    message.setData(bundle);
                    mClient = msg.replyTo;
                    try {
                        mClient.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 201:
                    /** 接收到用户端需要发送数据指令201*/
                    boolean sendBytes = mSerialPortManager1.sendTxt(bundle.get("data").toString());
                    Log.i(TAG, "onSend: sendBytes = " + sendBytes);
                    Log.i(TAG, sendBytes ? "发送成功" : "发送失败");
                    break;
                case 202:
                    /** 接收到用户端需要发送数据指令202*/
                    mSerialPortManager2.sendTxt(bundle.get("data").toString());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServer.getBinder();
    }

    @Override
    public void onDestroy() {
        if (null != mSerialPortManager1) {
            mSerialPortManager1.closeSerialPort();
            mSerialPortManager1 = null;
        }
        if (null != mSerialPortManager2) {
            mSerialPortManager2.closeSerialPort();
            mSerialPortManager2 = null;
        }
        super.onDestroy();
    }
}
