package com.kw.ssg10.code.session.task;

import java.io.IOException;

public class LogTask implements SessionTask {
    private String text;

    public LogTask(String text) {
        this.text = text;
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
