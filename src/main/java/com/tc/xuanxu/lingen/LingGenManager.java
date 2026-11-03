package com.tc.xuanxu.lingen;

import com.tc.xuanxu.util.RandomUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 灵根生成与管理。
 * <p>
 * 实现文档 3.3 / 3.4 的杂质生成、分配模式与狄利克雷分布。
 */
public final class LingGenManager {
    public static final String DATA_KEY = "xuanxu_lingen";

    private LingGenManager() {
    }

    public static LingGenData get(Player player) {
        CompoundTag tag = player.getPersistentData().getCompound(DATA_KEY);
        if (tag.isEmpty()) {
            return null;
        }
        return LingGenData.load(tag);
    }

    public static LingGenData getOrCreate(Player player) {
        LingGenData data = get(player);
        if (data == null) {
            data = generate(player.getRandom());
            set(player, data);
        }
        return data;
    }

    public static void set(Player player, LingGenData data) {
        CompoundTag tag = data.save();
        player.getPersistentData().put(DATA_KEY, tag);
    }

    /**
     * 清空灵根数据，回到未觉醒状态（OP 调试用）。
     */
    public static void reset(Player player) {
        player.getPersistentData().remove(DATA_KEY);
    }

    /**
     * 按照文档规则生成一份新的灵根数据。
     */
    public static LingGenData generate(RandomSource random) {
        LingGenConfig.init();
        double impurity = random.nextDouble() * LingGenConfig.maxImpurity();
        double available = 100.0 - impurity;

        List<LingGenType> allRoots = new ArrayList<>(LingGenRootRegistry.all());
        Map<String, Double> values = new LinkedHashMap<>();
        for (LingGenType type : allRoots) {
            values.put(type.id(), 0.0);
        }

        if (allRoots.isEmpty()) {
            return new LingGenData(impurity, values);
        }
        if (allRoots.size() == 1) {
            values.put(allRoots.get(0).id(), available);
            return new LingGenData(impurity, values);
        }

        if (available >= LingGenConfig.balancedMin()) {
            // 均衡分配：可用部分 >= 85%（或配置值）
            assignDirichlet(values, allRoots, available, random);
        } else if (available >= LingGenConfig.threeMin()) {
            // 三系分散
            List<LingGenType> primary = RandomUtils.pickRandom(allRoots, Math.min(3, allRoots.size()), random);
            double primaryRatio = LingGenConfig.primaryRatio3();
            double primaryTotal = available * primaryRatio;
            assignDirichlet(values, primary, primaryTotal, random);
            assignSecondary(values, allRoots, primary, available * (1.0 - primaryRatio), random);
        } else if (available >= LingGenConfig.twoMin()) {
            // 双系专精
            List<LingGenType> primary = RandomUtils.pickRandom(allRoots, Math.min(2, allRoots.size()), random);
            double primaryRatio = LingGenConfig.primaryRatio2();
            double primaryTotal = available * primaryRatio;
            assignDirichlet(values, primary, primaryTotal, random);
            assignSecondary(values, allRoots, primary, available * (1.0 - primaryRatio), random);
        } else if (available >= LingGenConfig.oneMin()) {
            // 单系极致
            List<LingGenType> primary = RandomUtils.pickRandom(allRoots, 1, random);
            double primaryRatio = LingGenConfig.primaryRatio1();
            values.put(primary.get(0).id(), available * primaryRatio);
            assignSecondary(values, allRoots, primary, available * (1.0 - primaryRatio), random);
        } else {
            // 孤注一掷：可用部分 < 10%（或配置值）
            List<LingGenType> primary = RandomUtils.pickRandom(allRoots, 1, random);
            values.put(primary.get(0).id(), available);
        }

        return new LingGenData(impurity, values);
    }

    /**
     * 把 secondaryTotal 均分给不在 primary 中的灵根。若没有剩余灵根，则忽略。
     */
    private static void assignSecondary(Map<String, Double> values, List<LingGenType> allRoots,
                                        List<LingGenType> primary, double secondaryTotal, RandomSource random) {
        List<LingGenType> secondary = new ArrayList<>();
        for (LingGenType type : allRoots) {
            if (!primary.contains(type)) {
                secondary.add(type);
            }
        }
        if (secondary.isEmpty()) {
            // 没有非主系灵根，把比例并入主系，保证总占比守恒
            if (!primary.isEmpty()) {
                double bonus = secondaryTotal / primary.size();
                for (LingGenType type : primary) {
                    values.put(type.id(), values.get(type.id()) + bonus);
                }
            }
            return;
        }
        double secondaryEach = secondaryTotal / secondary.size();
        for (LingGenType type : secondary) {
            values.put(type.id(), secondaryEach);
        }
    }

    private static void assignDirichlet(Map<String, Double> values, List<LingGenType> roots, double total, RandomSource random) {
        double[] proportions = dirichlet(roots.size(), random);
        for (int i = 0; i < roots.size(); i++) {
            values.put(roots.get(i).id(), proportions[i] * total);
        }
    }

    private static double[] dirichlet(int count, RandomSource random) {
        double[] values = new double[count];
        double sum = 0.0;
        for (int i = 0; i < count; i++) {
            // alpha = 1.0 的 Gamma 分布等价于指数分布
            double v = -Math.log(1.0 - random.nextDouble());
            values[i] = v;
            sum += v;
        }
        if (sum <= 0.0) {
            // 极不可能发生，但为避免除零，退化为均匀分布
            for (int i = 0; i < count; i++) {
                values[i] = 1.0 / count;
            }
            return values;
        }
        for (int i = 0; i < count; i++) {
            values[i] /= sum;
        }
        return values;
    }
}
