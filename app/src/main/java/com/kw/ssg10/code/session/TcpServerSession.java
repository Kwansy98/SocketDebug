package com.kw.ssg10.code.session;

import android.os.Message;
import com.kw.ssg10.code.activity.SessionActivity;
import com.kw.ssg10.code.session.task.LogTask;
import com.kw.ssg10.code.session.task.ReceiveStringTask;
import com.kw.ssg10.code.session.task.SendStringTask;
import com.kw.ssg10.code.session.task.SessionTask;
import com.kw.ssg10.code.session.task.UploadFileTask;
import com.kw.ssg10.code.utils.SessionConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class TcpServerSession implements Session {
    private static final String TAG = "kwdebug";
    private static final int BUF_SIZE = 8192;

    private SessionActivity.UpdateUIHandler handler; // handler是子线程和活动通信的桥梁
    private SessionConfig config;
    private ArrayList<SessionTask> tasks; // 输出列表副本

    private boolean isEstablish = false; // 连接状态
    private Queue<SessionTask> sendTasks; // 上传队列

    private class SendThread extends Thread {
        @Override
        public void run() {
            while (isEstablish) {
                try {
                    synchronized (this) {
                        wait();
                    }
                    while (isEstablish && !sendTasks.isEmpty()) {
                        SessionTask task = sendTasks.poll();
                        if (task == null) continue;
                        if (task instanceof SendStringTask) {
                            send((SendStringTask) task);
                        } else if (task instanceof UploadFileTask) {
                            send((UploadFileTask) task);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
    private SendThread sendThread;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public TcpServerSession(SessionActivity.UpdateUIHandler handler, SessionConfig config, ArrayList<SessionTask> tasks) {
        this.handler = handler;
        this.config = config;
        this.tasks = tasks;
        sendTasks = new LinkedList<>();
    }

    // 开始监听并接收数据
    @Override
    public void establish() {
        if (isEstablish) close();
        isEstablish = true;
        sendThread = new SendThread();
        sendThread.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetSocketAddress localAddress = new InetSocketAddress(
                            InetAddress.getByName(config.getListenHost()), config.getListenPort());
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(localAddress);
                    onEstablish();
                    clientSocket = serverSocket.accept();
                    serverSocket.close();
                    in = new DataInputStream(clientSocket.getInputStream());
                    out = new DataOutputStream(clientSocket.getOutputStream());
                    log(clientSocket.getRemoteSocketAddress().toString());

                    if (sendThread != null && !sendTasks.isEmpty()) {
                        synchronized (sendThread) {
                            sendThread.notify();
                        }
                    }
                    // 循环接收数据
                    byte[] byteRecv = new byte[BUF_SIZE];
                    int read = 0;
                    try {
                        while ((read = in.read(byteRecv)) > 0) {
                            postTask(new ReceiveStringTask(byteRecv, config.getEncodeRecv()));
                            updateUI();
                        }
                    } catch (IOException e) {
                        read = -1;
                        e.printStackTrace();
                    } finally {
                        if (read == -1) {
                            log("断开连接");
                            close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log("debug:" + e.getMessage());
                    close();
                }
            }
        }).start();
    }

    // 断开连接
    @Override
    public void close() {
        isEstablish = false;
        try {
            if (clientSocket != null) {
                clientSocket.shutdownInput();
                clientSocket.shutdownOutput();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 激活发送线程，使其退出
        try {
            if (sendThread != null) {
                synchronized (sendThread) {
                    sendThread.notify();
                }
            }
            sendTasks.clear();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        onClose();
    }

    // 添加一个任务，如果是接收数据，直接打印；如果是发送数据，则添加到发送列表，然后激活发送线程
    @Override
    public void postTask(SessionTask task) {
        if (task == null) return;
        if (task instanceof ReceiveStringTask) { // 接收字符串
            addOutputItem(task);
        } else if (task instanceof SendStringTask) { // 发字符串
            sendTasks.add(task);
            addOutputItem(task);
            if (sendThread != null) {
                synchronized (sendThread) {
                    sendThread.notify();
                }
            }
        } else if (task instanceof LogTask) { // 日志
            addOutputItem(task);
        } else if (task instanceof UploadFileTask) { // 发文件
            sendTasks.add(task);
            addOutputItem(task);
            if (sendThread != null) {
                synchronized (sendThread) {
                    sendThread.notify();
                }
            }
        }
    }

    @Override
    public boolean isEstablish() {
        return isEstablish;
    }

    // 发送字符串
    private void send(SendStringTask task) {
        byte[] buffer = task.getBuffer();
        try {
            out.write(buffer);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        task.setSentBytes(task.getTotalBytes());
    }

    // 发送文件
    private void send(UploadFileTask task) {
        byte[] buffer = new byte[BUF_SIZE];
        try {
            int read;
            while ((read = task.read(buffer)) != -1) {
                try {
                    out.write(buffer);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateUI();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            task.close();
        }
    }

    // 通知 handler 向输出列表增加一个 item
    private void addOutputItem(SessionTask task) {
        tasks.add(task);
        updateUI();
    }

    // 只更新UI，不加新item（如果在发送大文件时频繁调用可能会影响效率）
    private void updateUI() {
        Message msg = new Message();
        msg.what = SessionActivity.UpdateUIHandler.MSG_UPDATE_OUTPUT;
        handler.sendMessage(msg);
    }

    // 打印日志
    private void log(String text) {
        LogTask task = new LogTask(text);
        tasks.add(task);
        updateUI();
    }

    // 通知活动会话建立
    private void onEstablish() {
        Message msg = new Message();
        msg.what = SessionActivity.UpdateUIHandler.MSG_ONESTABLISH;
        handler.sendMessage(msg);
    }

    // 通知活动会话关闭
    private void onClose() {
        Message msg = new Message();
        msg.what = SessionActivity.UpdateUIHandler.MSG_ONCLOSE;
        handler.sendMessage(msg);
    }
}
