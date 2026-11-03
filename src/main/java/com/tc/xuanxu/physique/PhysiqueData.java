package com.tc.xuanxu.physique;

import net.minecraft.nbt.CompoundTag;

/**
 * 玩家灵体数据。
 * <p>
 * resolved 表示是否已经完成灵体觉醒选择。
 * physiqueId 为 null 且 resolved 为 true 时代表凡体。
 */
public final class PhysiqueData {
    private static final String TAG_RESOLVED = "resolved";
    private static final String TAG_ID = "id";

    private boolean resolved;
    private String physiqueId;

    public PhysiqueData() {
        this(false, null);
    }

    public PhysiqueData(boolean resolved, String physiqueId) {
        this.resolved = resolved;
        this.physiqueId = physiqueId;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getPhysiqueId() {
        return physiqueId;
    }

    public boolean isMortal() {
        return resolved && physiqueId == null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_RESOLVED, resolved);
        if (physiqueId != null) {
            tag.putString(TAG_ID, physiqueId);
        }
        return tag;
    }

    public static PhysiqueData load(CompoundTag tag) {
        boolean resolved = tag.getBoolean(TAG_RESOLVED);
        String id = tag.contains(TAG_ID) ? tag.getString(TAG_ID) : null;
        return new PhysiqueData(resolved, id);
    }
}
