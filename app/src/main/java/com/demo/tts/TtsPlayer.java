package com.demo.tts;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TtsPlayer extends MyRunnable {
    private final int format = AudioFormat.ENCODING_PCM_FLOAT;
    private final int sampleRate = 24000;
    private final int channel = AudioFormat.CHANNEL_OUT_MONO;
    private final int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, format);
    private final LinkedBlockingQueue<SpeechData> queue = new LinkedBlockingQueue<>();

    private final AudioTrack audioTrack;

    private EventListener listener;
    private SpeechData currentSpeech;

    TtsPlayer() {
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(format)
                        .setChannelMask(channel)
                        .build(),
                bufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        play();

        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(this);
        exec.shutdown();
    }

    public void play(SpeechData speechData) {
        queue.offer(speechData);
    }

    public void play(){
        audioTrack.play();
    }

    public void pause(){
        audioTrack.pause();
    }

    public void setVolume(float volume){
        audioTrack.setVolume(volume);
    }

    public void stop() {
        queue.clear();
        audioTrack.release();
        if (currentSpeech != null) currentSpeech.interrupt();
    }

    public void destory(){
        try {
            stop();
            interrupt();
        }catch (Exception e){}
    }

    @Override
    public void run() {
        while (!isInterrupt()) {
            try {
                currentSpeech = queue.take();
                if (listener != null) listener.onSpeech(currentSpeech);
                int index = 0;
                while (index < currentSpeech.getAudio().length && !currentSpeech.isInterrupt()) {
                    int buffer = Math.min(bufferSize, currentSpeech.getAudio().length - index);
                    audioTrack.write(currentSpeech.getAudio(), index, buffer, AudioTrack.WRITE_BLOCKING);
                    index += bufferSize;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getSize(){
        return queue.size();
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public interface EventListener{
        void onSpeech(SpeechData speech);
    }
}
