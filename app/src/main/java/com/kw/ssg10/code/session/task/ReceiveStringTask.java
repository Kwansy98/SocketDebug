package com.kw.ssg10.code.session.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ReceiveStringTask implements SessionTask {
    private byte[] buffer;  // 接收到的二进制数据
    private String charset; // 转换字符集
    private String text;    // 输出文本

    public ReceiveStringTask(byte[] buffer, String charset) throws UnsupportedEncodingException {
        this.buffer = buffer;
        this.charset = charset;
        text = new String(this.buffer, this.charset);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String showText() {
        return text;
    }

}
