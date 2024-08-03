package com.demo.tts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaddleTextTokenizer implements TextTokenizer {
    private final Map<String, String> replaceMap = new HashMap<>();
    private final String contentSplit = "[\n，。？?！!,;；]";
    private final String[] symbolPunc = "\" . 。 , 、 ！ ？ ： ； ` ﹑ • ＂ ^ … ‘ ’ “ ” 〝 〞 ~ \\ ∕ | ¦ ‖ —　 ( ) 〈 〉 ﹞ ﹝ 「 」 ‹ › 〖 〗 】 【 » « 』 『 〕 〔 》 《 } { ] [ ﹐ ¸ ﹕ ︰ ﹔ ; ！ ¡ ？ ¿ ﹖ ﹌ ﹏ ﹋ ＇ ´ ˊ ˋ - ― ﹫ @ ︳ ︴ _ ¯ ＿ ￣ ﹢ + ﹦ = ﹤ ‐ < \u00AD ˜ ~ ﹟ # ﹩ $ ﹠ & ﹪ % ﹡ * ﹨ \\ ﹍ ﹉ ﹎ ﹊ ˇ ︵ ︶ ︷ ︸ ︹ ︿ ﹀ ︺ ︽ ︾ _ ˉ ﹁ ﹂ ﹃ ﹄ ︻ ︼ ? _ “ ” 、 。 《 》 $ : / （ > ） < ! ·".split(" ");
    private final Map<String, ZHTone> vocab = new HashMap<>();
    private final Map<String, String> pinyin = new HashMap<>();
    private final Map<String, Integer> symbol = new HashMap<>();
    private final String vocabPath;
    private final String pinyinPath;
    private final String symbolPath;

    public PaddleTextTokenizer(String vocabPath, String pinyinPath, String symbolPath){
        this.vocabPath = vocabPath;
        this.pinyinPath = pinyinPath;
        this.symbolPath = symbolPath;
    }

    @Override
    public List<Long> encode(String text) {
        text = text.toLowerCase() + "。";
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) text = text.replaceAll(entry.getKey(), entry.getValue());
        text = NumberUtils.replaceNumber(text);
        return getIds(text);
    }

    @Override
    public String decode(List<Long> tokens) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destory() {
        try {
            vocab.clear();
            pinyin.clear();
            symbol.clear();
        }catch (Exception e){}
    }

    @Override
    public void init() throws Exception {
        vocab.clear();
        pinyin.clear();
        symbol.clear();
        replaceMap.clear();
        BufferedReader reader = new BufferedReader(new FileReader(vocabPath));
        String line = null;
        while ((line = reader.readLine()) != null){
            if (line.isEmpty()) continue;
            String[] array = line.split(":");
            vocab.put(array[0], new ZHTone(array[0], array[1]));
        }
        reader.close();

        reader = new BufferedReader(new FileReader(pinyinPath));
        while ((line = reader.readLine()) != null){
            if (line.isEmpty()) continue;
            String[] array = line.split(":");
            pinyin.put(array[0], array[1]);
        }
        reader.close();

        reader = new BufferedReader(new FileReader(symbolPath));
        while ((line = reader.readLine()) != null){
            if (line.isEmpty()) continue;
            String[] array = line.split(":");
            symbol.put(array[0], Integer.valueOf(array[1]));
        }
        reader.close();

        replaceMap.put("℃", "摄氏度");
        replaceMap.put("℉", "华氏度");
        replaceMap.put("℉", "兰氏度");
        replaceMap.put("°", "度");
        replaceMap.put("¥", "人民币");
        replaceMap.put("\\$", "美元");
        replaceMap.put("-", "负");
    }

    private void pinyin(int start, String text, List<String> result){
        if (start == text.length() - 1){
            ZHTone item = vocab.get(text.substring(start, text.length()));
            push(item, result);
            return;
        }

        if (start + 1 == text.length()){
            ZHTone item = vocab.get(text.substring(start, start + 1));
            push(item, result);
            return;
        }

        boolean isFirstContain = false;
        for (int i = start; i < text.length(); i++){
            String word = text.substring(start, i + 1);
            int end = i;
            if (!vocab.containsKey(word)){
                ZHTone item = vocab.get(text.substring(start, end));
                push(item, result);
                pinyin(isFirstContain ? end : end + 1, text, result);
                return;
            }
            isFirstContain = true;
        }
    }

    private void push(ZHTone zhTone, List<String> result){
        if (zhTone != null){
            String[] items = zhTone.getTone().split(" ");
            for (int x = 0; x < items.length; x++){
                items[x] = items[x].replaceAll("ü", "v");
            }
            buSanhi(zhTone, items);
            yiSanhi(zhTone, items);
            mixSanhi(zhTone, items);
            result.addAll(Arrays.asList(items));
        }
    }

    private void yiSanhi(ZHTone tone, String[] tones){
        String[] splits = StringUtils.toArrays(tone.getText());
        if (splits.length == 3 && splits[1].equals("一") && splits[0].equals(splits[2])){
            tones[1] = replaceEnd(tones[1], "5");
        }else if (tone.getText().startsWith("第一")){
            tones[1] = replaceEnd(tones[1], "1");
        }else {
            for (int i = 0; i < splits.length; i++){
                String str = splits[i];
                if (str.equals("一") && i + 1 < splits.length){
                    if (tones[i + 1].substring(tones[i + 1].length() - 1).matches("[45]")){
                        tones[i] = replaceEnd(tones[i], "2");
                    }else if (!in(symbolPunc, splits[i + 1])){
                        tones[i] = replaceEnd(tones[i], "4");
                    }
                }
            }
        }
    }

    private void buSanhi(ZHTone tone, String[] tones){
        String[] splits = StringUtils.toArrays(tone.getText());
        if (splits.length == 3 && splits[1].equals("不")){
            tones[1] = replaceEnd(tones[1], "5");
        }else {
            for (int i = 0; i < splits.length; i++) {
                String str = splits[i];
                if (str.equals("不") && i + 1 < splits.length && tones[i + 1].substring(tones[i + 1].length() - 1).equals("4")){
                    tones[i] = replaceEnd(tones[i], "2");
                }
            }
        }
    }

    private void mixSanhi(ZHTone tone, String[] tones){
        String[] splits = StringUtils.toArrays(tone.getText());
        if (splits.length == 2 && isAllToneC(tones)){
            tones[0] = replaceEnd(tones[0], "2");
        }else if (splits.length == 3){

        }else if (splits.length == 4){
            for (int i = 0; i < tones.length; i++){
                if (i == 0 || i == 2){
                    if (isAllToneC(i == 0 ? Arrays.copyOfRange(tones, 0, 2) : Arrays.copyOfRange(tones, 2, tones.length))){
                        tones[i] = replaceEnd(tones[i], "2");
                    }
                }
            }
        }
    }

    private int findIndex(String[] array, String val){
        for (int i = 0; i < array.length; i++){
            if (array[i].equals(val)) return i;
        }
        return -1;
    }

    private boolean isAllToneC(String[] tones){
        boolean isTrue = true;
        for (String word : tones){
            if (!word.substring(word.length() - 1).equals("3")){
                isTrue = false;
                break;
            }
        }
        return isTrue;
    }

    @Override
    public String[] split(String text){
        return text.split(contentSplit);
    }

    private boolean in(String[] array, String val){
        for (String item : array){
            if (item.equals(val)) return true;
        }
        return false;
    }

    private List<String> splitPinyin(List<String> list){
        List<String> result = new ArrayList<>();
        for (String item : list) {
            String what = pinyin.get(item.replaceAll("\\d+$", ""));
            if (what != null) {
                what += item.substring(item.length() - 1);
                result.addAll(Arrays.asList(what.split(" ")));
            }
        }
        return result;
    }

    private List<Long> getIds(String text){
        List<String> result = new ArrayList<>();
        result.add("<sil>");
        List<String> pin = new ArrayList<>();
        pinyin(0, text, pin);
        result.addAll(splitPinyin(pin));
        result.add("sp");
        result.add("<sil>");
        return getIds(result);
    }

    @Override
    public long[] toLong(List<Long> list){
        long[] ids = new long[list.size()];
        for (int i = 0; i < ids.length; i++) ids[i] = list.get(i);
        return ids;
    }

    private List<Long> getIds(List<String> list){
        List<Long> result = new ArrayList<>(list.size());
        for (String item : list){
            Integer index = symbol.get(item);
            if (index != null) result.add(Long.valueOf(index));
        }
        return result;
    }

    private String replaceEnd(String text, String val){
        return text.substring(0, text.length() - 1) + val;
    }
}
