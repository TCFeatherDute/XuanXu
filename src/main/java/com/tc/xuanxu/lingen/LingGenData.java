package com.tc.xuanxu.lingen;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 玩家灵根数据。
 * <p>
 * 存储杂质百分比以及各系灵根占比。
 * 所有灵根占比之和 = 100% - 杂质。
 */
public final class LingGenData {
    public static final String TAG_IMPURITY = "impurity";

    private final double impurity;
    private final Map<String, Double> roots;

    public LingGenData(double impurity, Map<String, Double> roots) {
        this.impurity = impurity;
        this.roots = new LinkedHashMap<>(roots);
    }

    public double getImpurity() {
        return impurity;
    }

    /**
     * 可用灵根空间，等于 100% - 杂质。
     */
    public double getAvailablePercent() {
        return 100.0 - impurity;
    }

    public Map<String, Double> getRoots() {
        return Collections.unmodifiableMap(roots);
    }

    /**
     * 当前所有灵根占比之和，正常应等于 100% - 杂质。
     */
    public double getRootSum() {
        double sum = 0.0;
        for (double value : roots.values()) {
            sum += value;
        }
        return sum;
    }

    public double getRoot(String rootId) {
        return roots.getOrDefault(rootId, 0.0);
    }

    /**
     * 修炼速度倍率。
     * 公式：1 - 杂质% * 0.3
     * 例如杂质 20% 时，效率 = 1 - 0.2 * 0.3 = 0.94。
     */
    public double getCultivationSpeedMultiplier() {
        return 1.0 - (impurity / 100.0) * 0.3;
    }

    public LingGenRank getRank(String rootId) {
        return LingGenRank.fromPercent(getRoot(rootId));
    }

    /**
     * 灵根为 0 时无法学习/使用该系功法。
     */
    public boolean canLearn(String rootId) {
        return getRoot(rootId) > 0.0;
    }

    /**
     * 计算该系灵根在指定境界下提供的功法固定威力加成。
     * 公式：灵根占比% / 100 * 境界系数。
     */
    public double getSpellPowerBonus(String rootId, Realm realm) {
        return getRoot(rootId) / 100.0 * realm.getCoefficient();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(TAG_IMPURITY, impurity);
        for (Map.Entry<String, Double> entry : roots.entrySet()) {
            tag.putDouble(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    public static LingGenData load(CompoundTag tag) {
        double impurity = tag.getDouble(TAG_IMPURITY);
        Map<String, Double> roots = new LinkedHashMap<>();
        for (LingGenType type : LingGenRootRegistry.all()) {
            roots.put(type.id(), tag.contains(type.id()) ? tag.getDouble(type.id()) : 0.0);
        }
        return new LingGenData(impurity, roots);
    }

    @Override
    public String toString() {
        return "LingGenData[impurity=" + impurity + ", roots=" + roots + "]";
    }
}
