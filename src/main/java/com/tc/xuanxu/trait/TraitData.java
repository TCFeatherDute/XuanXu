package com.tc.xuanxu.trait;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 玩家最终拥有的词条数据，最多 4 个。
 */
public final class TraitData {
    private final List<String> traitIds;

    public TraitData() {
        this(new ArrayList<>());
    }

    public TraitData(List<String> traitIds) {
        this.traitIds = new ArrayList<>(traitIds);
    }

    public List<String> getTraitIds() {
        return Collections.unmodifiableList(traitIds);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String id : traitIds) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("traits", list);
        return tag;
    }

    public static TraitData load(CompoundTag tag) {
        List<String> ids = new ArrayList<>();
        ListTag list = tag.getList("traits", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            // deprecated 词条自动迁移到 replace_by
            TraitDefinition trait = TraitRegistry.get(id).orElse(null);
            String migrated = trait != null ? trait.migrationTarget().orElse(id) : id;
            if (!ids.contains(migrated)) {
                ids.add(migrated);
            }
        }
        return new TraitData(ids);
    }
}
