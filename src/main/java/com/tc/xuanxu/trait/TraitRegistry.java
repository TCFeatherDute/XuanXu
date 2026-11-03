package com.tc.xuanxu.trait;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tc.xuanxu.util.JsonConfigUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 词条注册表，支持从 config/cultivation/traits/*.json 加载。
 */
public final class TraitRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, TraitDefinition> TRAITS = new LinkedHashMap<>();

    private TraitRegistry() {
    }

    public static void init() {
        registerDefaults();
        loadFromConfig();
    }

    /**
     * 重新加载所有词条配置（含重新注册内置默认词条）。
     */
    public static void reload() {
        TRAITS.clear();
        init();
    }

    private static void registerDefaults() {
        // 兼容旧代码：核心字段旧构造
        register(new TraitDefinition("sword_genius", "剑道天才", TraitRarity.RARE,
                "剑伤害+20%，攻速+10%", "weapon", true));
        register(new TraitDefinition("blade_master", "刀法宗师", TraitRarity.RARE,
                "刀伤害+25%，破甲+15%", "weapon", true));
        register(new TraitDefinition("fire_star", "火德之星", TraitRarity.RARE,
                "火系法术伤害+15%", "", true));
        register(new TraitDefinition("bloodthirsty", "嗜血狂战", TraitRarity.RARE,
                "击杀回血5%最大生命值", "", true));
        register(new TraitDefinition("gale_swift", "疾风迅雷", TraitRarity.EPIC,
                "连续攻击时攻速递增，最高+30%", "", true));
        register(new TraitDefinition("immovable", "不动如山", TraitRarity.RARE,
                "站立不动时防御+20%", "", true));
        register(new TraitDefinition("enlightenment", "顿悟之才", TraitRarity.EPIC,
                "突破瓶颈成功率+15%", "", true));
        register(new TraitDefinition("poison_immunity", "百毒不侵", TraitRarity.RARE,
                "中毒伤害-50%", "", true));
        register(new TraitDefinition("lone_star", "天煞孤星", TraitRarity.EPIC,
                "无队友时伤害+30%，有队友时伤害-20%", "", true));
        register(new TraitDefinition("weak_body", "虚弱之体", TraitRarity.RARE,
                "最大生命-10%，灵气上限+20%", "", true));
    }

    public static void loadFromConfig() {
        JsonConfigUtils.forEachJsonInConfigDir("cultivation/traits", TraitRegistry::parseJson);
    }

    private static void parseJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        TraitRarity rarity = TraitRarity.fromName(json.get("rarity").getAsString());
        String description = json.has("description") ? json.get("description").getAsString() : "";
        String conflictGroup = json.has("conflict_group") ? json.get("conflict_group").getAsString() : "";
        String icon = json.has("icon") ? json.get("icon").getAsString() : "";
        boolean enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
        boolean deprecated = json.has("deprecated") && json.get("deprecated").getAsBoolean();
        String replaceBy = json.has("replace_by") ? json.get("replace_by").getAsString() : "";
        List<String> tags = JsonConfigUtils.parseStringList(json, "tags");
        List<String> requirements = JsonConfigUtils.parseStringList(json, "requirements");
        List<TraitDefinition.EffectDefinition> effects = parseEffects(json);
        register(new TraitDefinition(id, name, rarity, description, conflictGroup,
                icon, tags, enabled, deprecated, replaceBy, requirements, effects));
        LOGGER.debug("Registered trait '{}'", id);
    }

    private static List<TraitDefinition.EffectDefinition> parseEffects(JsonObject json) {
        List<TraitDefinition.EffectDefinition> result = new ArrayList<>();
        JsonArray effects = JsonConfigUtils.optJsonArray(json, "effects");
        for (JsonElement element : effects) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            String type = obj.has("type") ? obj.get("type").getAsString() : "";
            String condition = obj.has("condition") ? obj.get("condition").getAsString() : "";
            double value = obj.has("value") ? obj.get("value").getAsDouble() : 0.0;
            String operation = obj.has("operation") ? obj.get("operation").getAsString() : "add";
            result.add(new TraitDefinition.EffectDefinition(type, condition, value, operation));
        }
        return result;
    }

    public static void register(TraitDefinition trait) {
        TRAITS.put(trait.id(), trait);
    }

    public static Optional<TraitDefinition> get(String id) {
        return Optional.ofNullable(TRAITS.get(id));
    }

    public static List<TraitDefinition> enabledTraits() {
        List<TraitDefinition> list = new ArrayList<>();
        for (TraitDefinition trait : TRAITS.values()) {
            if (trait.enabled()) {
                list.add(trait);
            }
        }
        return list;
    }

    public static Collection<TraitDefinition> all() {
        return TRAITS.values();
    }
}