package com.tc.xuanxu.util;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用随机工具：不重复的洗牌与随机挑取。
 */
public final class RandomUtils {
    private RandomUtils() {
    }

    /**
     * Fisher-Yates 洗牌，就地打乱列表。
     */
    public static <T> void shuffle(List<T> list, RandomSource random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    /**
     * 从源列表随机取出 count 个（不重复，保持随机顺序）。
     */
    public static <T> List<T> pickRandom(List<T> source, int count, RandomSource random) {
        List<T> pool = new ArrayList<>(source);
        shuffle(pool, random);
        return pool.subList(0, Math.min(count, pool.size()));
    }
}