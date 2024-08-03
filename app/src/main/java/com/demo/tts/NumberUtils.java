package com.demo.tts;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ZTMIDGO 2023/7/21
 */
public class NumberUtils {
    public static String indexToZH(int index){
        switch (index){
            case 0:
                return "";
            case 1:
                return "十";
            case 2:
                return "百";
            case 3:
                return "千";
            case 4:
                return "万";
            case 5:
                return "十万";
            case 6:
                return "百万";
            case 7:
                return "千万";
            case 8:
                return "亿";
            case 9:
                return "十亿";
            case 10:
                return "百亿";
            case 11:
                return "千亿";
            case 12:
                return "万亿";
            case 13:
                return "百万亿";
            case 14:
                return "千万亿";
            default:
                return "兆";
        }
    }

    public static String numberToHAN(String number){
        String[] array = StringUtils.toArrays(number);

        int path = 4;
        int size = array.length / path + 1;
        int end = array.length;

        List<String> list = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--){
            StringBuilder sb = new StringBuilder();

            int start = end - path;
            start = start < 0 ? 0 : start;
            if (end == start) break;

            int limit = end - start - 1;
            for (int j = start; j < end; j++){
                String m = indexToZH(limit --);
                sb.append(numberToZH(array[j]));
                if (!array[j].equals("0")) sb.append(m);
            }

            end = start;
            sb.append(indexToZH(path * (size - 1 - i)));
            list.add(sb.toString());
        }

        String result = "";
        for (int i = list.size() - 1; i >= 0; i--) result += list.get(i)+" ";
        result = result.trim();
        result = result.replaceFirst("[零]{1,}$", "");
        return result;
    }

    public static String numberToZH(String number){
        String[] array = StringUtils.toArrays(number);
        StringBuilder sb = new StringBuilder();
        for (String val : array){
            if (TextUtils.isEmpty(val)) continue;
            String res = "";
            switch (val) {
                case "0":
                    res = "零";
                    break;
                case "1":
                    res = "一";
                    break;
                case "2":
                    res = "二";
                    break;
                case "3":
                    res = "三";
                    break;
                case "4":
                    res = "四";
                    break;
                case "5":
                    res = "五";
                    break;
                case "6":
                    res = "六";
                    break;
                case "7":
                    res = "七";
                    break;
                case "8":
                    res = "八";
                    break;
                case "9":
                    res = "九";
                    break;
                default:
                    res = val;
            }
            sb.append(res);
        }
        return sb.toString();
    }

    public static String replaceNumber(String text){
        Pattern pattern = Pattern.compile("(\\d+\\.{0,}[0-9]{0,})");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()){
            int next = matcher.end();
            String value = matcher.group();
            if ((next != text.length() && text.substring(next, next + 1).equals("年") && value.length() == 4) || value.startsWith("0")) {
                text = text.replaceFirst(value, NumberUtils.numberToZH(value));
            }else {
                String[] array = matcher.group().split("\\.");
                StringBuilder sb = new StringBuilder();

                if (array.length == 1){
                    for (String line : array) sb.append(NumberUtils.numberToHAN(line));
                }else if (array.length == 2){
                    sb.append(NumberUtils.numberToHAN(array[0]));
                    sb.append("点");
                    sb.append(NumberUtils.numberToHAN(array[1]));
                }else {
                    for (int i = 0; i < array.length; i++){
                        sb.append(NumberUtils.numberToZH(array[i]));
                        if (i < array.length - 1) sb.append("点");
                    }
                }

                text = text.replaceFirst(value, sb.toString());
            }
            matcher.reset(text);
        }
        return text;
    }
}
