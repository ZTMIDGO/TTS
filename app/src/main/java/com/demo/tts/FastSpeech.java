package com.demo.tts;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/**
 * Created by ZTMIDGO 2023/7/17
 */
public class FastSpeech {
    private final OrtSession.SessionOptions options = new OrtSession.SessionOptions();
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private OrtSession session;
    private boolean isWoring = false;
    private boolean isDestory = false;

    public FastSpeech(String modulePath) throws Exception {
        options.addConfigEntry("session.load_model_format", "ORT");
        session = environment.createSession(modulePath, options);
    }

    public Object forward(long[] ids, float speed) {
        Object data = null;
        try {
            isWoring = true;
            OnnxTensor idx = OnnxTensor.createTensor(environment, LongBuffer.wrap(ids), new long[]{ids.length});
            Map<String, OnnxTensor> input = new HashMap<>();
            input.put("text", idx);
            OrtSession.Result result = session.run(input);
            data = result.get(0).getValue();
            result.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            isWoring = false;
            if (isDestory) destory();
            return data;
        }
    }

    public void destory(){
        try {
            isDestory = true;
            if (!isWoring) session.close();
        } catch (Exception e) {}
    }
}
