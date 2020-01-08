package com.kw.ssg10.code.session;


import com.kw.ssg10.code.session.task.SessionTask;


public interface Session {
    void establish(); // （重新）连接
    void close(); // 断开连接
    boolean isEstablish(); // 获取连接状态
    void postTask(SessionTask task); // 添加一个上传/下载任务
}
