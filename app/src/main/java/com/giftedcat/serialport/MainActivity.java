package com.giftedcat.serialport;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.giftedcat.serialport.event.SerialDataEvent;
import com.giftedcat.serialport.utils.MessengerSendUtil;
import com.giftedcat.serialportlibrary.SerialPortManager;
import com.giftedcat.serialportlibrary.listener.OnSerialPortDataListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    TextView tvData1,tvData2;

    byte[] mybyte = new byte[]{(byte)0xFF, (byte)0x09, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x99, (byte)0x91, (byte)0xFE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initView();
        final SerialPortManager manager = new SerialPortManager(new File("/dev/ttyS1"), 115200)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        /** 接收到扫码数据*/
                        if (bytes.length < 3)
                            return;
                        /** 接收到垃圾桶返回数据*/
                        switch (bytes[2]) {
                            case (byte) 0x01:
                                /** 开门结果返回*/
                                if (bytes[7] == (byte) 0x29) {
                                    /** 开门成功*/
                                } else {
                                    /** 开门失败*/
                                }
                                break;
                            case (byte) 0x02:
                                /** 查询垃圾箱满箱高度*/

                                break;
                            case (byte) 0x03:
                                /** 查询垃圾箱重量*/
                                Log.i("", "");
                                break;
                            case (byte) 0x04:
                                /** 主动上传垃圾箱满箱信号*/

                                break;

                        }
                    }
                });
        findViewById(R.id.btn_send1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.sendBytes(mybyte);
            }
        });
    }

//    private void initView() {
//        tvData1 = (TextView) findViewById(R.id.tv_data1);
//        tvData2 = (TextView) findViewById(R.id.tv_data2);
//        findViewById(R.id.btn_send1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MessengerSendUtil.sendMessageToServer(201, "0x01");
//            }
//        });
//        findViewById(R.id.btn_send2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MessengerSendUtil.sendMessageToServer(202, "0x01");
//            }
//        });
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(SerialDataEvent event) {
//        switch (event.getCode()){
//            case 101:
//                /** 接收到101数据*/
//                tvData1.setText(event.getData());
//                break;
//            case 102:
//                /** 接收到101数据*/
//                tvData2.setText(event.getData());
//                break;
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }
}
