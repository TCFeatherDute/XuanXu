package com.tc.xuanxu.trait;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 词条抽取过程中的临时状态。
 */
public final class TraitSelectionData {
    private static final String TAG_OPTIONS = "options";
    private static final String TAG_SELECTED = "selected";
    private static final String TAG_FIXED = "fixed";
    private static final String TAG_REROLLS = "rerolls";

    private List<String> options;
    private List<String> selected;
    private String fixedId;
    private int rerollsLeft;

    public TraitSelectionData() {
        this(new ArrayList<>(), new ArrayList<>(), null, 5);
    }

    public TraitSelectionData(List<String> options, List<String> selected, String fixedId, int rerollsLeft) {
        this.options = new ArrayList<>(options);
        this.selected = new ArrayList<>(selected);
        this.fixedId = fixedId;
        this.rerollsLeft = rerollsLeft;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<String> getSelected() {
        return selected;
    }

    public String getFixedId() {
        return fixedId;
    }

    public int getRerollsLeft() {
        return rerollsLeft;
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
    }

    public void setSelected(List<String> selected) {
        this.selected = new ArrayList<>(selected);
    }

    public void setFixedId(String fixedId) {
        this.fixedId = fixedId;
    }

    public void setRerollsLeft(int rerollsLeft) {
        this.rerollsLeft = rerollsLeft;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("options", toListTag(options));
        tag.put("selected", toListTag(selected));
        if (fixedId != null) {
            tag.putString(TAG_FIXED, fixedId);
        }
        tag.putInt(TAG_REROLLS, rerollsLeft);
        return tag;
    }

    public static TraitSelectionData load(CompoundTag tag) {
        List<String> options = fromListTag(tag.getList(TAG_OPTIONS, Tag.TAG_STRING));
        List<String> selected = fromListTag(tag.getList(TAG_SELECTED, Tag.TAG_STRING));
        String fixed = tag.contains(TAG_FIXED) ? tag.getString(TAG_FIXED) : null;
        int rerolls = tag.getInt(TAG_REROLLS);
        return new TraitSelectionData(options, selected, fixed, rerolls);
    }

    private static ListTag toListTag(List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private static List<String> fromListTag(ListTag list) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            values.add(list.getString(i));
        }
        return values;
    }
}
