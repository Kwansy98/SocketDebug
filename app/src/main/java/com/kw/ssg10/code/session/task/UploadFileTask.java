package com.kw.ssg10.code.session.task;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class UploadFileTask implements SessionTask {
    private File file;
    private FileChannel channel;
    private FileInputStream fileInputStream;
    private long send;
    private long length;

    public UploadFileTask(File file) throws FileNotFoundException {
        this.file = file;
        fileInputStream = new FileInputStream(file);
        channel = fileInputStream.getChannel();
        send = 0;
        length = file.length();
    }

    @Override
    public String showText() {
        if (send == file.length()) return file.getName() + "\n" + "上传完成：" + file.length() + " B";
        return file.getName() + "\n" + send + " / " + length;
    }

    // 非阻塞IO
    public int read(ByteBuffer buffer) throws IOException {
        int write = channel.read(buffer);
        if (write == -1) return -1;
        send += write;
        return write;
    }

    // 传统IO
    public int read(byte []buffer) throws IOException {
        int write = fileInputStream.read(buffer);
        if (write == -1) return -1;
        send += write;
        return write;
    }

    public void close() {
        try {
            if (channel != null) channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
