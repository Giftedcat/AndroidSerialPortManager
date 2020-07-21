# 将SerialPortManager添加到你的项目
Via Gradle:
```
implementation 'com.github.Giftedcat:AndroidSerialPortManager:v1.0.2'
```
Via Maven:
```
	<dependency>
	    <groupId>com.github.Giftedcat</groupId>
	    <artifactId>AndroidSerialPortManager</artifactId>
	    <version>v1.0.2</version>
	</dependency>
```
# 一、前言

前段时间在工作中有一个棘手的问题，接手了一个Android项目，因需要投入使用的设备内存小（RAM1GB）且使用到串口较多，频次较高的原因，在某些页面使用上会出现略微的卡顿，导致用户体验不是特别好，我当时的想法是：如果把1GB换成2GB，那么这把牌将绝杀，可惜设备已经订好了，换不得。

初步决定使用多线程的方式，将串口读写的工作和业务代码分开来，一个是把代码模块化，另外一个是多进程便可以从系统处分配出更多的内存以加快整个APP的响应速度。

# 二、原理图

先贴一下整个流程的原理图

![image](https://upload-images.jianshu.io/upload_images/20395467-8298f40fb2778809.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

原理比较清晰，由Service和串口进程通信，我们的Application与Service进行绑定，通过Messenger和Service进行交互，而数据由Application通过Eventbus进行下发给页面

# 三、实现功能

#### （一）Service和串口通信
我这边开了两个串口做演示，一个是ttyS0,波特率9600
另外一个是ttyS1,波特率9600
```
        mSerialPortManager1 = new SerialPortManager(new File("/dev/ttyS0"), 9600)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        /** 接收到串口数据*/
                        sendMessageToClient(101, new String(bytes));
                    }
                });
        mSerialPortManager2 = new SerialPortManager(new File("/dev/ttyS1"), 9600)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        sendMessageToClient(102, new String(bytes));
                    }
                });
```
#### （二）Service和Application的交互
Service和Application属于不同的进程，所以涉及到跨进程通信
我使用的是Messenger，可参考[https://www.jianshu.com/p/671b07f5ef86](https://www.jianshu.com/p/671b07f5ef86)
当然跨进程的方式有很多，我这边选了一个比较简单的来使用
感兴趣的同学可以自行深挖，此处不展开篇幅谈多进程

因为实际使用如我这个项目中用了n多的指令和串口，所以我使用Message的what作为状态码code来区分不同的指令
这是service中接收数据的Handler
200是接收到application传递过来的Messenger，用于初始化
201和202都是从主进程发过来的数据，用于两个串口
```
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
```
这是Application中的Handler
数据来源于串口的数据回调
```
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
```

#### （三）Application下发数据和接收数据
数据下发就由EventBus来完成了，非常好用
而页面需要向串口传数据我在Application中创建了一个全局的Messenger
mServer，在绑定Service的时候将其初始化，便可以使用
```
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
```
发送数据的代码
```
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
```

# 四、总结
比起直接使用串口多了几个步骤，便可以实现多进程的串口读写了
不仅运行速度有了明显的提高，而且可以全局的读写串口数据，并不是以前单一的页面使用串口，也不需要频繁的开关串口，如果有多个页面需要用到串口，也不用重复的写无意义的代码，可谓是一举多得
