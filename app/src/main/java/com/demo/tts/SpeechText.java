package com.demo.tts;

public class SpeechText {
    private final String uuid;
    private final String text;

    public SpeechText(String uuid, String text) {
        this.uuid = uuid;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getUUID() {
        return uuid;
    }
}
