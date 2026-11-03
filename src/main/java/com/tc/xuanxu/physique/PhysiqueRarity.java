package com.tc.xuanxu.physique;

import net.minecraft.util.RandomSource;

/**
 * 灵体品质，由低到高：绿、蓝、紫、橙、红。
 * 对应文档 4.2 的抽取概率。
 */
public enum PhysiqueRarity {
    GREEN("绿色", 0.65, 0x55FF55),
    BLUE("蓝色", 0.20, 0x55AAFF),
    PURPLE("紫色", 0.10, 0xAA55FF),
    ORANGE("橙色", 0.04, 0xFFAA00),
    RED("红色", 0.01, 0xFF5555);

    private final String displayName;
    private final double weight;
    private final int color;

    PhysiqueRarity(String displayName, double weight, int color) {
        this.displayName = displayName;
        this.weight = weight;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWeight() {
        return weight;
    }

    public int getColor() {
        return color;
    }

    /**
     * 按品质权重随机抽取一个品质。
     */
    public static PhysiqueRarity roll(RandomSource random) {
        double total = 0.0;
        for (PhysiqueRarity rarity : values()) {
            total += rarity.weight;
        }
        double point = random.nextDouble() * total;
        for (PhysiqueRarity rarity : values()) {
            point -= rarity.weight;
            if (point < 0) {
                return rarity;
            }
        }
        return GREEN;
    }

    public static PhysiqueRarity fromName(String name) {
        for (PhysiqueRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return GREEN;
    }
}
