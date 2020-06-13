package com.giftedcat.serialport.event;

public class SerialDataEvent {

    public SerialDataEvent(int code, String data) {
        this.code = code;
        this.data = data;
    }

    private int code;
    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
