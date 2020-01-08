package com.kw.ssg10.code.utils;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// 全局可见的会话管理器
public class SessionManager {
    private static final String TAG = "kwdebug";
    private static Context context;
    private static ArrayList<SessionConfig> sessions = null;

    public static void initSessionManager(Context _context) {
        context = _context;
        load();
    }

    // 将内存中的session列表写入文件
    public static void save() {
        try {
            FileOutputStream out = context.openFileOutput("sessions", Context.MODE_PRIVATE); // 覆盖模式
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(sessions);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "save: 已写入文件，长度为 " + getSize());
    }

    // 从文件读取session列表到内存中
    public static void load() {
        try {
            FileInputStream in = context.openFileInput("sessions");
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            sessions = (ArrayList<SessionConfig>) objectInputStream.readObject();
        } catch (Exception e) {
            // 载入失败，可能是第一次启动，也有可能是开发阶段修改了SessionConfig
            sessions = new ArrayList<>();
            save();
        }
        //Log.d(TAG, "load: 已载入到内存 长度为" + getSize());
    }

    // 获取指定下标的session
    public static SessionConfig get(int pos) throws IndexOutOfBoundsException {
        return sessions.get(pos);
    }

    // 修改指定下标的session
    public static void set(int pos, SessionConfig config) {
        sessions.set(pos, config);
        save();
    }

    // 添加session
    public static void add(SessionConfig sessionConfig) {
        add(0, sessionConfig);
    }

    public static void add(int pos, SessionConfig sessionConfig) {
        if (pos > sessions.size() || pos < 0) pos = 0;
        sessions.add(pos, sessionConfig);
        save();
    }

    // 删除session
    public static void remove(int pos) {
        try {
            sessions.remove(pos);
            save();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    // 获取列表
    public static ArrayList<SessionConfig> getSessions() {
        return sessions;
    }

    // 获取长度
    public static int getSize() {
        return sessions.size();
    }

    // 移动到顶部
    public static void moveToTop(int pos) {
        SessionConfig config = sessions.get(pos);
        sessions.remove(pos);
        sessions.add(0, config);
        save();
    }
}
