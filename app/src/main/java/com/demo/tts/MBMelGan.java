package com.demo.tts;

import java.util.HashMap;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/**
 * Created by ZTMIDGO 2023/7/17
 */
public class MBMelGan {
    private final OrtSession.SessionOptions options = new OrtSession.SessionOptions();
    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private OrtSession session;
    private boolean isWoring = false;
    private boolean isDestory = false;

    public MBMelGan(String modulePath) throws Exception {
        options.addConfigEntry("session.load_model_format", "ORT");
        session = environment.createSession(modulePath, options);
    }

    public float[] forward(Object data) {
        float[] audioArray = null;
        try {
            isWoring = true;
            Map<String, OnnxTensor> input = new HashMap<>();
            OnnxTensor idx = OnnxTensor.createTensor(environment, data);
            input.put("logmel", idx);

            OrtSession.Result result = session.run(input);
            OnnxTensor tensor = OnnxTensor.createTensor(environment, result.get(0).getValue());
            audioArray = tensor.getFloatBuffer().array();
            result.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            isWoring = false;
            if (isDestory) destory();
            return audioArray;
        }
    }

    public void destory(){
        try {
            isDestory = true;
            if (!isWoring) session.close();
        } catch (Exception e) {}
    }
}
