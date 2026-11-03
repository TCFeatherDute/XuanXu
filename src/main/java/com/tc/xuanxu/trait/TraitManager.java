package com.tc.xuanxu.trait;

import com.tc.xuanxu.util.RandomUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 天命词条管理：抽取、选择、固定、刷新、确认。
 */
public final class TraitManager {
    public static final String DATA_KEY = "xuanxu_traits";
    public static final String PENDING_KEY = "xuanxu_trait_pending";
    public static final int MAX_TRAITS = 4;
    public static final int MAX_REROLLS = 5;

    private TraitManager() {
    }

    public static TraitData getData(Player player) {
        CompoundTag tag = player.getPersistentData().getCompound(DATA_KEY);
        if (tag.isEmpty()) {
            return new TraitData();
        }
        return TraitData.load(tag);
    }

    public static void saveData(Player player, TraitData data) {
        player.getPersistentData().put(DATA_KEY, data.save());
    }

    /**
     * 清空词条数据与抽取中的临时状态，回到未觉醒状态（OP 调试用）。
     */
    public static void reset(Player player) {
        player.getPersistentData().remove(DATA_KEY);
        clearPending(player);
    }

    public static List<String> getCurrentTraits(Player player) {
        return getData(player).getTraitIds();
    }

    public static TraitSelectionData getPending(Player player) {
        CompoundTag tag = player.getPersistentData().getCompound(PENDING_KEY);
        if (tag.isEmpty()) {
            return null;
        }
        return TraitSelectionData.load(tag);
    }

    public static void savePending(Player player, TraitSelectionData data) {
        player.getPersistentData().put(PENDING_KEY, data.save());
    }

    public static void clearPending(Player player) {
        player.getPersistentData().remove(PENDING_KEY);
    }

    public static TraitSelectionData startSelection(Player player) {
        TraitSelectionData data = new TraitSelectionData(
                generateOptions(player.getRandom(), List.of()),
                new ArrayList<>(),
                null,
                MAX_REROLLS
        );
        savePending(player, data);
        return data;
    }

    public static boolean selectOption(Player player, int optionIndex) {
        TraitSelectionData data = getPending(player);
        if (data == null || optionIndex < 0 || optionIndex >= data.getOptions().size()) {
            return false;
        }
        String id = data.getOptions().get(optionIndex);
        if (data.getSelected().size() >= MAX_TRAITS || data.getSelected().contains(id)) {
            return false;
        }
        if (hasConflict(data, id)) {
            return false;
        }
        List<String> selected = new ArrayList<>(data.getSelected());
        selected.add(id);
        data.setSelected(selected);
        savePending(player, data);
        return true;
    }

    public static boolean deselectOption(Player player, int optionIndex) {
        TraitSelectionData data = getPending(player);
        if (data == null || optionIndex < 0 || optionIndex >= data.getOptions().size()) {
            return false;
        }
        String id = data.getOptions().get(optionIndex);
        if (!data.getSelected().contains(id)) {
            return false;
        }
        List<String> selected = new ArrayList<>(data.getSelected());
        selected.remove(id);
        data.setSelected(selected);
        if (id.equals(data.getFixedId())) {
            data.setFixedId(null);
        }
        savePending(player, data);
        return true;
    }

    public static boolean fixOption(Player player, int optionIndex) {
        TraitSelectionData data = getPending(player);
        if (data == null || optionIndex < 0 || optionIndex >= data.getOptions().size()) {
            return false;
        }
        String id = data.getOptions().get(optionIndex);
        if (!data.getSelected().contains(id)) {
            return false;
        }
        data.setFixedId(id);
        savePending(player, data);
        return true;
    }

    public static boolean fixFirstSelected(Player player) {
        TraitSelectionData data = getPending(player);
        if (data == null || data.getSelected().isEmpty()) {
            return false;
        }
        // 全局只能固定 1 个词条，已经固定过就不能再固定第二个
        if (data.getFixedId() != null) {
            return false;
        }
        data.setFixedId(data.getSelected().get(0));
        savePending(player, data);
        return true;
    }

    public static boolean reroll(Player player) {
        TraitSelectionData data = getPending(player);
        if (data == null || data.getRerollsLeft() <= 0) {
            return false;
        }

        List<String> excluded = new ArrayList<>(data.getSelected());
        if (data.getFixedId() != null) {
            excluded.remove(data.getFixedId());
        }

        List<String> options = generateOptions(player.getRandom(), excluded);
        String fixed = data.getFixedId();
        if (fixed != null && !options.contains(fixed)) {
            options.add(0, fixed);
        }

        data.setOptions(options);
        data.setSelected(fixed != null ? new ArrayList<>(List.of(fixed)) : new ArrayList<>());
        data.setRerollsLeft(data.getRerollsLeft() - 1);
        savePending(player, data);
        return true;
    }

    public static boolean confirm(Player player) {
        TraitSelectionData data = getPending(player);
        if (data == null || data.getSelected().size() != MAX_TRAITS) {
            return false;
        }
        saveData(player, new TraitData(data.getSelected()));
        clearPending(player);
        return true;
    }

    /**
     * 从词条池抽取选项。
     * <p>
     * 支持按词条池的稀有度权重随机分配池子，并保证 guaranteedRarity 至少出现一次（若存在该类词条）。
     * 权重缺省或全为 0 时退化为均匀抽取。
     */
    private static List<String> generateOptions(RandomSource random, List<String> excluded) {
        Set<String> excludedIds = new HashSet<>(excluded);
        TraitPool traitPool = TraitPoolRegistry.getOrStarting("starting_pool");
        Set<String> poolExcluded = new HashSet<>(traitPool.excludeTraits());

        // 候选词条：启用、未被排除
        List<TraitDefinition> candidates = new ArrayList<>();
        for (TraitDefinition trait : TraitRegistry.enabledTraits()) {
            if (excludedIds.contains(trait.id()) || poolExcluded.contains(trait.id())) {
                continue;
            }
            candidates.add(trait);
        }
        if (candidates.isEmpty()) {
            return List.of();
        }

        // 按稀有度分组
        Map<TraitRarity, List<TraitDefinition>> byRarity = new HashMap<>();
        for (TraitDefinition trait : candidates) {
            byRarity.computeIfAbsent(trait.rarity(), k -> new ArrayList<>()).add(trait);
        }

        int optionCount = Math.min(traitPool.maxTraits() * 2 + 2, candidates.size());
        if (optionCount < 10) {
            optionCount = Math.min(10, candidates.size());
        }

        Map<TraitRarity, Double> weights = traitPool.rarityWeights();
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        // 若权重缺失/全 0，退化为均匀权重
        boolean weighted = totalWeight > 0.0;

        List<String> result = new ArrayList<>();
        Set<String> used = new HashSet<>();

        // 保证 guaranteedRarity 至少一次
        TraitRarity guaranteed = traitPool.guaranteedRarity();
        if (guaranteed != null) {
            List<TraitDefinition> pool = byRarity.get(guaranteed);
            if (pool != null && !pool.isEmpty()) {
                addUnique(result, used, pool.get(random.nextInt(pool.size())));
            }
        }

        while (result.size() < optionCount) {
            TraitRarity rarity;
            if (weighted) {
                rarity = pickRarity(weights, totalWeight, random);
            } else {
                rarity = byRarity.keySet().stream()
                        .skip(random.nextInt(byRarity.size()))
                        .findFirst()
                        .orElse(TraitRarity.COMMON);
            }
            List<TraitDefinition> pool = byRarity.get(rarity);
            if (pool == null || pool.isEmpty()) {
                continue;
            }
            List<TraitDefinition> remaining = new ArrayList<>(pool);
            RandomUtils.shuffle(remaining, random);
            boolean added = false;
            for (TraitDefinition trait : remaining) {
                if (!used.contains(trait.id())) {
                    addUnique(result, used, trait);
                    added = true;
                    break;
                }
            }
            // 该稀有度词条已全部用完：从全候选补足，避免死循环
            if (!added) {
                List<TraitDefinition> allRemaining = new ArrayList<>(candidates);
                RandomUtils.shuffle(allRemaining, random);
                for (TraitDefinition trait : allRemaining) {
                    if (!used.contains(trait.id())) {
                        addUnique(result, used, trait);
                        break;
                    }
                }
            }
            if (used.size() >= candidates.size() || result.size() >= optionCount) {
                break;
            }
        }
        return result;
    }

    private static void addUnique(List<String> result, Set<String> used, TraitDefinition trait) {
        if (used.add(trait.id())) {
            result.add(trait.id());
        }
    }

    /**
     * 按权重随机一个稀有度。
     */
    private static TraitRarity pickRarity(Map<TraitRarity, Double> weights, double totalWeight, RandomSource random) {
        double point = random.nextDouble() * totalWeight;
        for (Map.Entry<TraitRarity, Double> entry : weights.entrySet()) {
            point -= entry.getValue();
            if (point < 0) {
                return entry.getKey();
            }
        }
        return TraitRarity.COMMON;
    }

    private static boolean hasConflict(TraitSelectionData data, String newId) {
        TraitDefinition newTrait = TraitRegistry.get(newId).orElse(null);
        if (newTrait == null || newTrait.conflictGroup().isEmpty()) {
            return false;
        }
        for (String selectedId : data.getSelected()) {
            TraitDefinition selected = TraitRegistry.get(selectedId).orElse(null);
            if (selected != null && newTrait.conflictGroup().equals(selected.conflictGroup())) {
                return true;
            }
        }
        return false;
    }
}