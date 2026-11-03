package com.tc.xuanxu.trait;

import java.util.ArrayList;
import java.util.List;

/**
 * 词条效果聚合/应用工具。
 * <p>
 * 根据玩家拥有的词条，汇总同名 type 的效果数值与运算方式，供战斗/修炼系统统一查询。
 * 当前提供纯查询接口，不直接修改游戏数据；后续可由攻击/修炼事件调用。
 */
public final class TraitEffectApplier {
    private TraitEffectApplier() {
    }

    /**
     * 聚合所有词条里指定 type 的效果值。
     *
     * @return 返回所有匹配 type 的效果对象列表（已去除 null）
     */
    public static List<TraitDefinition.EffectDefinition> collectEffects(List<String> traitIds, String type) {
        List<TraitDefinition.EffectDefinition> result = new ArrayList<>();
        if (traitIds == null || type == null) {
            return result;
        }
        for (String id : traitIds) {
            TraitDefinition trait = TraitRegistry.get(id).orElse(null);
            if (trait == null) {
                continue;
            }
            for (TraitDefinition.EffectDefinition effect : trait.effects()) {
                if (type.equals(effect.type())) {
                    result.add(effect);
                }
            }
        }
        return result;
    }

    /**
     * 按 operation 类型计算某个 type 的最终倍率/加值。
     * <p>
     * 规则：
     * - multiply：多个值相乘（如 1.2 × 1.15）
     * - add / 默认：多个值相加
     */
    public static double computeTotal(List<TraitDefinition.EffectDefinition> effects) {
        double multiply = 1.0;
        double add = 0.0;
        for (TraitDefinition.EffectDefinition effect : effects) {
            String op = effect.operation() == null ? "add" : effect.operation();
            if ("multiply".equalsIgnoreCase(op) || "multiply_cap".equalsIgnoreCase(op)) {
                multiply *= (1.0 + effect.value());
            } else {
                add += effect.value();
            }
        }
        return (multiply - 1.0) + add;
    }

    /**
     * 便捷方法：直接传入词条 ID 列表与效果 type，得到聚合后的总加成值。
     */
    public static double computeFor(List<String> traitIds, String type) {
        return computeTotal(collectEffects(traitIds, type));
    }

    /**
     * 便捷方法：展示某个词条的 effects 格式化文本，可用于 tooltip / 命令输出。
     */
    public static String formatEffects(TraitDefinition trait) {
        if (trait == null || trait.effects() == null || trait.effects().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TraitDefinition.EffectDefinition effect : trait.effects()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(effect.type())
                    .append(" ")
                    .append(effect.value() >= 0 ? "+" : "")
                    .append(String.format("%.0f%%", effect.value() * 100))
                    .append(" [")
                    .append(effect.operation())
                    .append("]");
            if (effect.condition() != null && !effect.condition().isBlank()) {
                sb.append(" 条件:").append(effect.condition());
            }
        }
        return sb.toString();
    }
}