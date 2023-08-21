package com.hehe.playletapp.util;

import java.util.UUID;

public class RandomUtil {

    /**
     * 生成随机数字字符串
     * @param length 位数
     * @return
     */
    public static String generateString (Integer length) {
        StringBuffer sb = new StringBuffer("");
        if (length > 0) {
            for (int i = 1; i<length; i++) {
                if (i == 1) {
                    int randomNum = (int) (Math.random() * 9) + 1;
                    sb.append(randomNum);
                }
                int randomNum = (int) (Math.random() * 9);
                sb.append(randomNum);
            }
        }
        return sb.toString();
    }

    /**
     * 生成最大数已下的随机数
     * @param maxNum
     * @return
     */
    public static int random(Integer maxNum) {
        int randomNum = (int) (Math.random() * maxNum);
        return randomNum;
    }

    public static String uuidString () {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public static void main(String[] args) {
        String s = generateString(6);
        System.out.println(s);
    }
}
