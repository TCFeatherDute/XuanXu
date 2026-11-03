package com.tc.xuanxu.physique;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 灵体觉醒管理。
 * <p>
 * 实现文档 4.2 / 4.3：随机抽取 3 个灵体，品质独立判定，玩家三选一或选择凡体。
 */
public final class PhysiqueManager {
    public static final String DATA_KEY = "xuanxu_physique";
    public static final String PENDING_KEY = "xuanxu_physique_pending";

    private PhysiqueManager() {
    }

    public static PhysiqueData getData(Player player) {
        CompoundTag tag = player.getPersistentData().getCompound(DATA_KEY);
        if (tag.isEmpty()) {
            return new PhysiqueData();
        }
        return PhysiqueData.load(tag);
    }

    public static void saveData(Player player, PhysiqueData data) {
        player.getPersistentData().put(DATA_KEY, data.save());
    }

    /**
     * 清空灵体数据与待选选项，回到未觉醒状态（OP 调试用）。
     */
    public static void reset(Player player) {
        player.getPersistentData().remove(DATA_KEY);
        clearPending(player);
    }

    public static boolean isResolved(Player player) {
        return getData(player).isResolved();
    }

    /**
     * 当前灵体；返回 null 表示凡体或尚未觉醒。
     */
    public static PhysiqueType getCurrent(Player player) {
        String id = getData(player).getPhysiqueId();
        if (id == null) {
            return null;
        }
        return PhysiqueRegistry.get(id).orElse(null);
    }

    public static void setCurrent(Player player, PhysiqueType physique) {
        saveData(player, new PhysiqueData(true, physique.id()));
        clearPending(player);
    }

    public static void setMortal(Player player) {
        saveData(player, new PhysiqueData(true, null));
        clearPending(player);
    }

    /**
     * 随机抽取 3 个灵体选项。每个选项的品质独立按概率判定。
     */
    public static List<PhysiqueType> rollOptions(RandomSource random) {
        List<PhysiqueType> pool = new ArrayList<>(PhysiqueRegistry.all());
        List<PhysiqueType> options = new ArrayList<>();

        if (pool.isEmpty()) {
            return options;
        }

        for (int i = 0; i < 3; i++) {
            PhysiqueRarity rarity = PhysiqueRarity.roll(random);

            List<PhysiqueType> candidates = pool.stream()
                    .filter(type -> type.rarity() == rarity)
                    .filter(type -> !options.contains(type))
                    .toList();

            if (candidates.isEmpty()) {
                candidates = pool.stream()
                        .filter(type -> !options.contains(type))
                        .toList();
            }

            if (candidates.isEmpty()) {
                options.add(pool.get(random.nextInt(pool.size())));
            } else {
                options.add(candidates.get(random.nextInt(candidates.size())));
            }
        }

        return options;
    }

    public static void setPendingOptions(Player player, List<PhysiqueType> options) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (PhysiqueType type : options) {
            list.add(StringTag.valueOf(type.id()));
        }
        tag.put("options", list);
        player.getPersistentData().put(PENDING_KEY, tag);
    }

    public static List<PhysiqueType> getPendingOptions(Player player) {
        List<PhysiqueType> result = new ArrayList<>();
        CompoundTag tag = player.getPersistentData().getCompound(PENDING_KEY);
        ListTag list = tag.getList("options", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            PhysiqueRegistry.get(id).ifPresent(result::add);
        }
        return result;
    }

    public static void clearPending(Player player) {
        player.getPersistentData().remove(PENDING_KEY);
    }
}
