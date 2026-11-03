package com.tc.xuanxu.trait;

import java.util.List;
import java.util.Map;

/**
 * 词条池配置，对应文档 7.5。
 */
public record TraitPool(
        String id,
        int version,
        Map<TraitRarity, Double> rarityWeights,
        TraitRarity guaranteedRarity,
        int maxTraits,
        List<String> excludeTraits
) {
    public TraitPool {
        rarityWeights = rarityWeights == null ? Map.of() : Map.copyOf(rarityWeights);
        excludeTraits = excludeTraits == null ? List.of() : List.copyOf(excludeTraits);
    }
}