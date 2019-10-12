package com.jqk.wifitest;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;
import com.example.udptest.udp.AppConstants;
import com.example.udptest.udp.ByteUtil;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPManager {

    private volatile static UDPManager mInstance;
    //    单个CPU线程池大小
    private static final int POOL_SIZE = 5;
    private static final int BUFFER_LENGTH = 1024;
    private byte[] receiveByte = new byte[BUFFER_LENGTH];
    private byte[] receiveByte2 = new byte[BUFFER_LENGTH];

    private boolean isClientThreadRunning = false;
    private boolean isServerThreadRunning = false;

    private DatagramSocket client;
    private DatagramPacket receivePacket;

    private DatagramSocket server;
    private DatagramPacket receivePacket2;

    private ExecutorService mThreadPool;
    private Thread clientThread;
    private Thread serverThread;

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    private UDPManager() {
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * POOL_SIZE);
    }

    public static UDPManager getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        synchronized (UDPManager.class) {
            if (mInstance == null)
                mInstance = new UDPManager();
        }
        return mInstance;
    }

    public void buildClientUDPSocket() {
        if (client != null) return;
        try {
//            表明这个 Socket 在设置的端口上监听数据。
            client = new DatagramSocket(AppConstants.SOCKET_CLIENT_PORT);
            if (receivePacket == null) {
                receivePacket = new DatagramPacket(receiveByte, BUFFER_LENGTH);
            }
//            startClientSocketThread();
        } catch (SocketException e) {
            e.printStackTrace();
            L.d(e.toString());
        }
    }

    /**
     * 开启发送数据的线程
     **/
    private void startClientSocketThread() {
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                L.d("clientThread is running...");
//                receiveMessage();
            }
        });
        isClientThreadRunning = true;
        clientThread.start();
    }

    /**
     * 处理接受到的消息
     **/
    private void receiveMessage() {
        while (isClientThreadRunning) {
            if (client != null) {
                try {
                    L.d("1");
                    client.receive(receivePacket);
                    L.d("2");
                } catch (IOException e) {
                    L.d("UDP数据包接收失败！线程停止");
                    stopClientUDPSocket();
                    e.printStackTrace();
                    return;
                }
            }

            if (receivePacket == null || receivePacket.getLength() == 0) {
                L.d("无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }
            String strReceive = ByteUtil.INSTANCE.bytesToHex(receivePacket.getData());
            EventBus.getDefault().post(new EventBusMessage(strReceive));
            L.d("客户端接收 = " + receivePacket.getAddress().getHostAddress() + ":" + receivePacket.getPort() + " = " + strReceive);
            // 解析返回的数据
            ArrayList<Byte> data = new ArrayList<Byte>();

            for (int i = 0; i < receivePacket.getData().length; i++) {
                data.add(receivePacket.getData()[i]);
            }
            AnalyzeUtil.Companion.getInstance().analyze(data);


            // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
            if (receivePacket != null) {
                receivePacket.setLength(BUFFER_LENGTH);
            }
        }
    }

    /**
     * 停止UDP
     **/
    public void stopClientUDPSocket() {
        isClientThreadRunning = false;
        receivePacket = null;
        if (clientThread != null) {
            clientThread.interrupt();
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    /**
     * 发送信息
     **/
    public void sendMessageToServer(final String host, final byte[] message) {
        L.d("发送的数据 = " + ByteUtil.INSTANCE.bytesToHex(message));
        if (client == null) {
            buildClientUDPSocket();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(host);
                    DatagramPacket packet = new DatagramPacket(
                            message,
                            message.length, inetAddress, AppConstants.SOCKET_SERVER_PORT);
                    if (client != null) {
                        client.send(packet);
                        L.d("消息发送成功");
                    } else {
                        L.d("消息发送失败");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void buildServerUDPSocket() {
        if (server != null) return;
        try {
//            表明这个 Socket 在设置的端口上监听数据。
            if (server == null) {
                server = new DatagramSocket(null);
                server.setReuseAddress(true);
                server.bind(new InetSocketAddress(AppConstants.SOCKET_SERVER_PORT));
            }

            if (receivePacket2 == null) {
                receivePacket2 = new DatagramPacket(receiveByte2, BUFFER_LENGTH);
            }
            startServerSocketThread();
        } catch (SocketException e) {
            e.printStackTrace();
            L.d("buildServerUDPSocket = " + e.toString());
        }
    }

    public void startServerSocketThread() {
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                L.d("serverThread is running...");
                while (isServerThreadRunning) {
                    if (server != null) {
                        try {
                            L.d("3");
                            server.receive(receivePacket2);
                            L.d("4");
                        } catch (IOException e) {
                            L.d("UDP数据包接收失败！线程停止");
                            stopClientUDPSocket();
                            e.printStackTrace();
                            return;
                        }
                    }
                    if (receivePacket2 == null || receivePacket2.getLength() == 0) {
                        L.d("无法接收UDP数据或者接收到的UDP数据为空");
                        continue;
                    }

                    //读取数据（也可以调用 packet.getData()）
                    String strReceive = ByteUtil.INSTANCE.bytesToHex(receivePacket2.getData());
                    EventBus.getDefault().post(new EventBusMessage(strReceive));
                    L.d("服务器接收strReceive = " + strReceive);

                    // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
                    if (receivePacket2 != null) {
                        receivePacket2.setLength(BUFFER_LENGTH);
                    }
                }
            }
        }) {

        };
        isServerThreadRunning = true;
        serverThread.start();
    }

    /**
     * 发送信息
     **/
//    public void sendMessageToClient(final String host, final byte[] message) {
//        L.d("发送的数据 = " + ByteUtil.INSTANCE.bytesToHex(message));
//        if (server == null) {
//            buildServerUDPSocket();
//        }
//        mThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    InetAddress address = InetAddress.getByName(host);
//                    DatagramPacket dataPacket = new DatagramPacket(message, message.length, address, AppConstants.SOCKET_CLIENT_PORT);
//                    server.send(dataPacket);
//                    L.d("消息发送成功");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public void stopServerSocket() {
        isServerThreadRunning = false;
        receivePacket2 = null;
        if (serverThread != null) {
            serverThread.interrupt();
        }
        if (server != null) {
            server.close();
            server = null;
        }
    }
}