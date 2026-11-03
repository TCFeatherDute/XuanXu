package com.tc.xuanxu.lingen;

/**
 * 灵根品级，按占比划分。
 * 对应文档 3.6：上品 / 中品 / 下品 / 杂灵根 / 废灵根 / 无灵根。
 * <p>
 * 判定规则：占比严格为 0（一点都没有）才算「无灵根」；
 * 只要占比大于 0 就至少是「废灵根」，再按阈值逐级提升。
 */
public enum LingGenRank {
    NONE("无灵根", 0.0, 0, 0xFFFFFF),
    // minPercent 为 0 表示「任意正数占比」即可，具体由 fromPercent 处理
    WASTE("废灵根", 0.0, 0, 0xAAAAAA),
    MIXED("杂灵根", 0.03, 10, 0x55FF55),
    LOWER("下品灵根", 0.08, 20, 0x55AAFF),
    MIDDLE("中品灵根", 0.15, 30, 0xAA55FF),
    UPPER("上品灵根", 0.25, 40, 0xFFD700);

    private final String displayName;
    private final double speedBonus;
    private final int minPercent;
    private final int color;

    LingGenRank(String displayName, double speedBonus, int minPercent, int color) {
        this.displayName = displayName;
        this.speedBonus = speedBonus;
        this.minPercent = minPercent;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 该品级带来的修炼速度加成（百分比数值，例如 0.25 = 25%）。
     */
    public double getSpeedBonus() {
        return speedBonus;
    }

    public int getMinPercent() {
        return minPercent;
    }

    /**
     * 品级展示色（RGB，不带透明度），用于 GUI / 聊天展示。
     */
    public int getColor() {
        return color;
    }

    /**
     * 品级展示色（ARGB），可直接用于 drawString 等需要 alpha 的渲染 API。
     */
    public int getArgbColor() {
        return 0xFF000000 | color;
    }

    /**
     * 根据某系灵根占比判定品级。
     * <p>
     * 占比严格为 0 时才是「无灵根」；只要大于 0 就至少是「废灵根」，
     * 之后按各品级阈值逐级提升。
     */
    public static LingGenRank fromPercent(double percent) {
        if (percent <= 0.0) {
            return NONE;
        }
        LingGenRank best = WASTE;
        for (LingGenRank rank : values()) {
            if (rank.minPercent > 0 && percent >= rank.minPercent) {
                best = rank;
            }
        }
        return best;
    }
}