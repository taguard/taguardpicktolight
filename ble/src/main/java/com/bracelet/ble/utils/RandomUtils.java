package com.bracelet.ble.utils;

import java.util.Random;

public class RandomUtils {

    public static byte[] generateRandom(int length) {
        byte[] random = new byte[length];
        Random r = new Random();
        for (int i = 0; i < random.length; i ++) {
            random[i] = (byte)(r.nextInt(256));
        }
        return random;
    }
}
