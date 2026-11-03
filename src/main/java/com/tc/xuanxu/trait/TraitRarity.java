package com.tc.xuanxu.trait;

/**
 * 词条稀有度：白、绿、蓝、紫、橙。
 */
public enum TraitRarity {
    COMMON("白色", "common", 0xFFFFFF),
    UNCOMMON("绿色", "uncommon", 0x55FF55),
    RARE("蓝色", "rare", 0x55AAFF),
    EPIC("紫色", "epic", 0xAA55FF),
    LEGENDARY("橙色", "legendary", 0xFFAA00);

    private final String displayName;
    private final String serializedName;
    private final int color;

    TraitRarity(String displayName, String serializedName, int color) {
        this.displayName = displayName;
        this.serializedName = serializedName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public int getColor() {
        return color;
    }

    public static TraitRarity fromName(String name) {
        for (TraitRarity rarity : values()) {
            if (rarity.serializedName.equalsIgnoreCase(name) || rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON;
    }
}
