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

    TextView tvData1,tvData2, tvData3;
    private SerialPortManager serialPortManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        tvData1 = (TextView) findViewById(R.id.tv_data1);
        tvData2 = (TextView) findViewById(R.id.tv_data2);
        tvData3 = (TextView) findViewById(R.id.tv_data3);
        findViewById(R.id.btn_send1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessengerSendUtil.sendMessageToServer(201, "0x01");
            }
        });
        findViewById(R.id.btn_send2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessengerSendUtil.sendMessageToServer(202, "0x01");
            }
        });
        serialPortManager = new SerialPortManager(new File("/dev/ttyS4"), 9600, SerialPortManager.SPLICING)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(final byte[] bytes) {
                        /** 接收到串口数据*/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvData3.setText(new String(bytes));
                            }
                        });
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SerialDataEvent event) {
        switch (event.getCode()){
            case 101:
                /** 接收到101数据*/
                tvData1.setText(event.getData());
                break;
            case 102:
                /** 接收到101数据*/
                tvData2.setText(event.getData());
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
