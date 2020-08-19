package com.giftedcat.serialport.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.giftedcat.serialport.BuildConfig;
import com.giftedcat.serialport.event.SerialDataEvent;
import com.giftedcat.serialportlibrary.SerialPortManager;

import org.greenrobot.eventbus.EventBus;

import static android.content.ContentValues.TAG;

/**
 * Created by kqw on 2016/10/26.
 * InitApplication
 */

public class InitApplication extends Application {

    /** 发送数据*/
    public static Messenger mServer;
    /** 接收数据*/
    private Messenger mClient;

    /**
     * 串口传递数据接收Handler
     */
    private Handler serialHandler;
    private ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        initSerialReaderServer();
        if (BuildConfig.DEBUG){
            SerialPortManager.openLog();
        }
    }

    /**
     * 初始化串口数据读取服务
     * */
    private void initSerialReaderServer() {
        serialHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                switch (msg.what) {
                    case 100:
                        /** 初始化时返回的数据*/
                        Log.i(TAG, "接收到Service发送的数据");
                        break;
                    case 101:
                        /** 接收101数据*/
                        EventBus.getDefault().post(new SerialDataEvent(msg.what, bundle.get("data").toString()));
                        break;
                    case 102:
                        /** 接收到102数据*/
                        EventBus.getDefault().post(new SerialDataEvent(msg.what, bundle.get("data").toString()));
                        break;
                    default:
                        break;
                }
            }
        };
        mClient = new Messenger(serialHandler);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "onServiceConnected: ComponentName = " + name);
                mServer = new Messenger(service);
                Log.i(TAG, "连接成功...................");
                Message message = Message.obtain(null, 200);
                message.replyTo = mClient;
                try {
                    mServer.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent("com.giftedcat.server");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        unbindService(serviceConnection);
    }

}
