package com.tc.xuanxu.trait;

import java.util.List;
import java.util.Optional;

/**
 * 词条定义。
 * <p>
 * 对应文档 7.1 词条数据存储结构。
 * 为了兼容旧存档/旧 JSON，所有新增字段都有默认值。
 */
public record TraitDefinition(
        String id,
        String name,
        TraitRarity rarity,
        String description,
        String conflictGroup,
        String icon,
        List<String> tags,
        boolean enabled,
        boolean deprecated,
        String replaceBy,
        List<String> requirements,
        List<EffectDefinition> effects
) {
    public TraitDefinition {
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
        tags = tags == null ? List.of() : List.copyOf(tags);
        effects = effects == null ? List.of() : List.copyOf(effects);
        icon = icon == null ? "" : icon;
        replaceBy = replaceBy == null ? "" : replaceBy;
    }

    /**
     * 旧版兼容构造：仅保留核心字段。
     */
    public TraitDefinition(
            String id, String name, TraitRarity rarity, String description,
            String conflictGroup, boolean enabled
    ) {
        this(id, name, rarity, description, conflictGroup, "", List.of(), enabled,
                false, "", List.of(), List.of());
    }

    /**
     * 若 deprecated 且指定了 replaceBy，则说明应该迁移到新词条。
     */
    public Optional<String> migrationTarget() {
        return deprecated && replaceBy != null && !replaceBy.isBlank()
                ? Optional.of(replaceBy)
                : Optional.empty();
    }

    /**
     * 效果定义：对应 JSON effects 数组里的每一项。
     */
    public record EffectDefinition(
            String type,
            String condition,
            double value,
            String operation
    ) {
        public EffectDefinition {
            condition = condition == null ? "" : condition;
            operation = operation == null ? "add" : operation;
        }
    }
}
