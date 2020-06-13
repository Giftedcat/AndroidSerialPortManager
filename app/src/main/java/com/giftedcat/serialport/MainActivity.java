package com.giftedcat.serialport;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.giftedcat.serialport.event.SerialDataEvent;
import com.giftedcat.serialport.utils.MessengerSendUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    TextView tvData1,tvData2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        tvData1 = (TextView) findViewById(R.id.tv_data1);
        tvData2 = (TextView) findViewById(R.id.tv_data2);
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
