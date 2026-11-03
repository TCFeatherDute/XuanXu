package com.tc.xuanxu.physique;

/**
 * 灵体类型定义。
 * 后续可扩展为 JSON 驱动，从 config/cultivation/physiques/*.json 加载。
 */
public record PhysiqueType(String id, String name, PhysiqueRarity rarity, String description) {
}
