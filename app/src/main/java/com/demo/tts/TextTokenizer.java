package com.demo.tts;

import java.util.List;

/**
 * Created by ZTMIDGO 2022/9/9
 */
public abstract interface TextTokenizer {
    void init() throws Exception;
    List<Long> encode(String text);
    String decode(List<Long> tokens);
    String[] split(String text);
    long[] toLong(List<Long> tokens);
    void destory();
}
