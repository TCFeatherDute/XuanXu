package com.tc.xuanxu.lingen;

/**
 * 境界系数，用于计算灵根对功法威力的固定加成。
 * 对应文档 3.7：练气 20，筑基 40，结丹 80，元婴 150，化神 250。
 */
public enum Realm {
    QI_REFINING("练气期", 20),
    FOUNDATION("筑基期", 40),
    CORE_FORMATION("结丹期", 80),
    NASCENT_SOUL("元婴期", 150),
    SPIRIT_TRANSFORMATION("化神期", 250);

    private final String displayName;
    private final int coefficient;

    Realm(String displayName, int coefficient) {
        this.displayName = displayName;
        this.coefficient = coefficient;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCoefficient() {
        return coefficient;
    }
}
