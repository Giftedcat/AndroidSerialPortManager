package com.giftedcat.serialport.utils;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

import com.giftedcat.serialport.application.InitApplication;

public class MessengerSendUtil {

    /**
     * 发送数据至Service
     * */
    public static void sendMessageToServer(int code, String data) {
        Message message = Message.obtain(null, code);
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        message.setData(bundle);
        try {
            InitApplication.mServer.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }
}
