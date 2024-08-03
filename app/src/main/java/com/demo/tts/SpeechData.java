package com.demo.tts;

/**
 * Created by ZTMIDGO 2023/7/21
 */
public class SpeechData {
    private final String uuid;
    private final String text;
    private final float[] audio;
    private boolean interrupt;

    public SpeechData(String uuid, String text, float[] audio) {
        this.uuid = uuid;
        this.text = text;
        this.audio = audio;
    }

    public String getUuid() {
        return uuid;
    }

    public String getText() {
        return text;
    }

    public float[] getAudio() {
        return audio;
    }

    public void interrupt(){
        interrupt = true;
    }

    public boolean isInterrupt() {
        return interrupt;
    }
}
