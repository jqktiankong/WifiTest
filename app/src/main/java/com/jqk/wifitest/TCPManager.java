package com.jqk.wifitest;


import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import com.example.udptest.udp.ByteUtil;
import org.greenrobot.eventbus.EventBus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPManager {
    private static TCPManager instance;

    //    单个CPU线程池大小
    private static final int POOL_SIZE = 5;
    private static final int BUFFER_LENGTH = 1024;
    private ExecutorService mThreadPool;

    private Socket clientSocket;
    private ServerSocket serverSocket;

    private Thread serverThread;
    private Thread clientThread;

    private OutputStream clientOutputStream;
    private InputStream clientInputStream;

    private boolean serverConnect = false;
    private boolean clientConnect = false;

    //    构造函数私有化
    private TCPManager() {
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * POOL_SIZE);
    }

    //    提供一个全局的静态方法
    public static TCPManager getInstance() {
        if (instance == null) {
            synchronized (TCPManager.class) {
                if (instance == null) {
                    instance = new TCPManager();
                }
            }
        }
        return instance;
    }

    /**
     * 通过IP地址(域名)和端口进行连接
     *
     * @param ipAddress IP地址(域名)
     * @param port      端口
     */
    public void openServer(final String ipAddress, final int port) {
        L.d("打开服务端");
        if (serverSocket != null) {
            L.d("服务端已存在");
            return;
        }

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建服务器端 Socket，指定监听端口
                    serverSocket = new ServerSocket(8888);
                    //等待客户端连接
                    clientSocket = serverSocket.accept();
                    L.d("服务端连接成功");
                    while (serverConnect) {
                        if (clientSocket != null) {
                            //获取客户端输入流，
                            clientInputStream = clientSocket.getInputStream();
                            //获取客户端输出流
                            clientOutputStream = clientSocket.getOutputStream();
                            // 接收客户端数据
                            receiveClient();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    L.d("服务端 = " + e.toString());
                }
            }
        });
        serverConnect = true;
        serverThread.start();
    }

    public void openClient(final String ipAddress, final int port) {
        L.d("打开客户端");
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建客户端Socket，指定服务器的IP地址和端口
                    clientSocket = new Socket(ipAddress, 8888);
                    L.d("客户端连接成功");
                    while (clientConnect) {
                        if (clientSocket != null) {
                            //获取输出流，向服务器发送数据
                            clientOutputStream = clientSocket.getOutputStream();
                            //获取输入流，接收服务器发来的数据
                            clientInputStream = clientSocket.getInputStream();
                            // 接收服务端消息
                            receiveServer();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    L.d("客户端 = " + e.toString());
                }
            }
        });
        clientConnect = true;
        clientThread.start();
    }

    /**
     * 接收数据
     */
    public void receiveClient() {
        try {
            /**得到的是16进制数，需要进行解析*/
            byte[] bt = new byte[BUFFER_LENGTH];
//                获取接收到的字节和字节数
            int length = clientInputStream.read(bt);
//                获取正确的字节
            if (length < 0) {
                return;
            }
            byte[] bs = new byte[length];
            System.arraycopy(bt, 0, bs, 0, length);
            String str = ByteUtil.INSTANCE.bytesToHex(bs);
            if (str != null) {
                L.d("接收客户端消息成功 = " + str);
                EventBus.getDefault().post(new EventBusMessage("接收客户端消息成功 = " + str));
            }
        } catch (IOException e) {
            L.d("接收客户端消息失败 = " + e.toString());
        }
    }

    public void receiveServer() {
        try {
            /**得到的是16进制数，需要进行解析*/
            byte[] bt = new byte[BUFFER_LENGTH];
//                获取接收到的字节和字节数
            int length = clientInputStream.read(bt);
//                获取正确的字节
            if (length < 0) {
                return;
            }
            byte[] bs = new byte[length];
            System.arraycopy(bt, 0, bs, 0, length);
            String str = ByteUtil.INSTANCE.bytesToHex(bs);
            if (str != null) {
                L.d("接收服务端消息成功 = " + str);
                EventBus.getDefault().post(new EventBusMessage("接收服务端消息成功 = " + str));
            }
        } catch (IOException e) {
            L.d("接收服务端消息失败 = " + e.toString());
        }
    }

    /**
     * 发送数据
     *
     * @param data 数据
     */
    public void send2Server(final byte[] data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (clientSocket != null) {
                    try {
                        if (clientOutputStream != null) {
                            clientOutputStream.write(data);
                            clientOutputStream.flush();
                            L.d("发送成功");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        L.d("send2Server = " + e.toString());
                    }
                } else {
                    // 重新连接
                }
            }
        });
    }

    public void send2Client(final byte[] data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (clientSocket != null) {
                    try {
                        if (clientOutputStream != null) {
                            clientOutputStream.write(data);
                            clientOutputStream.flush();
                            L.d("发送成功");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        L.d("send2Client = " + e.toString());
                    }
                } else {
                    // 重新连接
                }
            }
        });
    }

    public void closeAll() {
        L.d("closeAll");
        serverConnect = false;
        clientConnect = false;
        try {
            // 关闭clientSocket
            if (clientSocket != null) {
                clientSocket.shutdownInput();
                clientSocket.shutdownOutput();
                clientSocket.close();
            }

            if (clientInputStream != null) {
                clientInputStream.close();
            }
            if (clientOutputStream != null) {
                clientOutputStream.close();
            }

            // 关闭serverSocket
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (clientThread != null) {
                clientThread.interrupt();
            }

            if (serverThread != null) {
                serverThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
            L.d("closeAll = " + e.toString());
        } finally {
            clientSocket = null;
            serverSocket = null;
        }
    }
}
