package com.kw.ssg10.code.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

// 会话配置类

public class SessionConfig implements Serializable, Cloneable {
    private int CSMode; // 0 client, 1 server
    private int protocol; // 0 tcp, 1 udp

    // 客户端模式
    private String serverHost; // 服务器IP
    private int serverPort; // 服务器端口
    private String sourceHost; // 本地IP，默认 0.0.0.0
    private int sourcePort; // 本地端口，默认自动分配
    private int connectTimeout; // 连接超时
    private boolean autoConnect; // 自动连接

    // 服务器模式
    private String listenHost; // 监听地址
    private int listenPort; // 监听端口
    private boolean autoListen; // 自动监听
//    private int listenMode; // 0 normal, 1 multiple, 2 broker, 3 chat
//    private int maxConnects;

    // 其他设置
    private int IOMode; // 0 rw, 1 ro, 2 wo
    private int EOL; // 0 lf, 1 crlf, 2 cr, 3 nothing
    private String encodeSend, encodeRecv; // 0 UTF8 1 ASCII 2 ISO8859 3 UTF16BE 4 UTF16LE 5 UTF16 6 GBK 7 GB2312

    // 默认构造器
    public SessionConfig() {
        CSMode = 0; // 客户端模式
        protocol = 0; // TCP协议

        serverHost = "ip.sb";
        serverPort = 80;
        sourceHost = "0.0.0.0"; // 本地IP
        sourcePort = 0; // 自动设置本地端口
        connectTimeout = 0; // 默认超时
        autoConnect = false; // 手动连接

        listenHost = "0.0.0.0";
        listenPort = 2020;
        autoListen = false; // 手动监听
//        listenMode = 0;
//        maxConnects = 1; // normal 固定为1，且不显示


        IOMode = 0; // 读写
        EOL = 0; // LF 换行符
        encodeSend = "UTF-8";
        encodeRecv = "UTF-8";
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(this);
    }

    public boolean fromString(String json) {
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();
            copy(gson.fromJson(json, SessionConfig.class));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 拷贝构造器，添加/删除新属性这里不要忘了改
    public void copy(SessionConfig config) {
        if (config == null) return;
        CSMode = config.CSMode;
        protocol  = config.protocol;

        serverHost = config.serverHost;
        serverPort = config.serverPort;
        sourceHost = config.sourceHost;
        sourcePort = config.sourcePort;
        connectTimeout = config.connectTimeout;
        autoConnect = config.autoConnect;

        listenHost = config.listenHost;
        listenPort = config.listenPort;
        autoListen = config.autoListen;
//        listenMode = config.listenMode;
//        maxConnects = config.maxConnects;


        IOMode = config.IOMode;
        EOL = config.EOL;
        encodeSend = config.encodeSend;
        encodeRecv = config.encodeRecv;
    }























    /**    GET SET    **/
    public int getCSMode() {
        return CSMode;
    }

    public void setCSMode(int CSMode) {
        this.CSMode = CSMode;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getIOMode() {
        return IOMode;
    }

    public void setIOMode(int IOMode) {
        this.IOMode = IOMode;
    }

    public int getEOL() {
        return EOL;
    }

    public void setEOL(int EOL) {
        this.EOL = EOL;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public String getEncodeSend() {
        return encodeSend;
    }

    public void setEncodeSend(String encodeSend) {
        this.encodeSend = encodeSend;
    }

    public String getEncodeRecv() {
        return encodeRecv;
    }

    public void setEncodeRecv(String encodeRecv) {
        this.encodeRecv = encodeRecv;
    }

    public String getListenHost() {
        return listenHost;
    }

    public void setListenHost(String listenHost) {
        this.listenHost = listenHost;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public boolean isAutoListen() {
        return autoListen;
    }

    public void setAutoListen(boolean autoListen) {
        this.autoListen = autoListen;
    }

    /**    GET SET    **/

}
