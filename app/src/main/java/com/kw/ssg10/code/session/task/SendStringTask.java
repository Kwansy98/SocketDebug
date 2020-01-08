package com.kw.ssg10.code.session.task;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

public class SendStringTask implements SessionTask {
    private String text;                // 发送的文本
    private int EOL;                    // 追加换行符 0 LF 1 CRLF 2 CR 3 NO APPEND
    private String charset;             // 转换成byte数组的字符集
    private byte[] buffer;              // 缓冲区
    private long totalBytes;            // 文本的总字节
    private long sentBytes;             // 已发送的字节数
    private DataInputStream in;         // 二进制输入流

    public SendStringTask(String text, String charset, int EOL) throws UnsupportedEncodingException {
        this.text = text;
        this.EOL = EOL;
        this.charset = charset;
        switch (this.EOL) {
            case 0:
                this.text += "\n";
                break;
            case 1:
                this.text += "\r\n";
                break;
            case 2:
                this.text += "\r";
                break;
            default:
        }
        // 转换成字节输入流

//        US-ASCII: Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
//        ISO-8859-1: ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
//        UTF-8: Eight-bit UCS Transformation Format
//        UTF-16BE: Sixteen-bit UCS Transformation Format, big-endian byte order
//        UTF-16LE: Sixteen-bit UCS Transformation Format, little-endian byte order
//        UTF-16: Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark.

        buffer = this.text.getBytes(this.charset);
        totalBytes = buffer.length;
        sentBytes = 0;
        in = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(buffer)));
    }

    @Override
    public String showText() {
        if (totalBytes == sentBytes)
            return text;
        else
            return text + "\n" + sentBytes + " / " + totalBytes;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(long sentBytes) {
        this.sentBytes = sentBytes;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }
}
