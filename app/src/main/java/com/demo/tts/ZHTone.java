package com.demo.tts;

/**
 * Created by ZTMIDGO 2023/7/19
 */
public class ZHTone {
    private final String text;
    private final String tone;

    public ZHTone(String text, String tone) {
        this.text = text;
        this.tone = tone;
    }

    public String getText() {
        return text;
    }

    public String getTone() {
        return tone;
    }
}
