package com.demo.tts;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class TTSManager implements TtsPlayer.EventListener {
    private final List<SpeechText> queue = new CopyOnWriteArrayList<>();
    private TextTokenizer tokenizer;
    private FastSpeech fastSpeech;
    private MBMelGan mbMelGan;
    private TtsPlayer player;
    private boolean isInitComplete;
    private MyRunnable runnable;
    private String ttsId;

    private Callback callback;

    private boolean init(Context context, String uuid){
        try {
            String basePath = context.getFilesDir().getAbsolutePath()+"/01";
            String acoustic = basePath+"/acoustic_model.ort";
            String mel = basePath+"/vocoder.ort";
            String vocabPath = basePath+"/vocab.txt";
            String pinyinPath = basePath+"/pinyin.txt";
            String symbolPath = basePath+"/symbol.txt";

            player = new TtsPlayer();
            tokenizer = new PaddleTextTokenizer(vocabPath, pinyinPath, symbolPath);
            fastSpeech = new FastSpeech(acoustic);
            mbMelGan = new MBMelGan(mel);
            tokenizer.init();
            player.setListener(this);
            isInitComplete = true;
        }catch (Exception e){
            isInitComplete = false;
            e.printStackTrace();
        }finally {
            return isInitComplete;
        }
    }

    public String getTtsId() {
        return ttsId;
    }

    public void push(List<SpeechText> list){
        queue.addAll(list);
    }

    private void speech(SpeechText text, float speed){
        if (text == null || TextUtils.isEmpty(text.getText()) || !isInitComplete) return;
        String[] lines = tokenizer.split(text.getText());
        for (String line : lines){
            if (!isInitComplete) return;
            long[] ids = tokenizer.toLong(tokenizer.encode(line));
            Object output = fastSpeech.forward(ids, speed);
            if (output == null) return;

            float[] audioData = mbMelGan.forward(output);
            if (audioData == null) return;

            player.play(new SpeechData(text.getUUID(), line, audioData));
        }
    }

    private MyRunnable getRunnable(Context context, String uuid, Callback callback){
        runnable = new MyRunnable() {
            @Override
            public void run() {
                init(context, uuid);
                if (callback != null) callback.init(isInitComplete);
                if (!isInitComplete) return;
                boolean isContinue = false;
                while (!isInterrupt()){
                    try {
                        if (player.getSize() >= 10){
                            isContinue = true;
                        }else if (player.getSize() < 5){
                            isContinue = false;
                        }

                        if (queue.isEmpty() || isContinue || !isInitComplete) continue;
                        speech(queue.remove(0), 1f);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
        return runnable;
    }

    public void run(Context context, String uuid, ExecutorService exec, Callback callback){
        if (isInitComplete) return;
        exec.execute(getRunnable(context, uuid, callback));
    }

    public void destory(){
        isInitComplete = false;
        ttsId = null;
        if (runnable != null) runnable.interrupt();
        if (tokenizer != null) tokenizer.destory();
        if (fastSpeech != null) fastSpeech.destory();
        if (mbMelGan != null) mbMelGan.destory();
        if (player != null) player.destory();
    }

    @Override
    public void onSpeech(SpeechData speech) {
        ttsId = speech.getUuid();
    }

    public interface Callback{
        void init(boolean success);
    }
}
