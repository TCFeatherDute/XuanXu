package com.tc.xuanxu.physique;

import com.google.gson.JsonObject;
import com.tc.xuanxu.util.JsonConfigUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 灵体注册表。
 * <p>
 * 默认注册几个基础灵体，并加载 {@code config/cultivation/physiques/*.json} 中的扩展灵体。
 */
public final class PhysiqueRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, PhysiqueType> PHYSIQUES = new LinkedHashMap<>();

    private PhysiqueRegistry() {
    }

    public static void init() {
        if (!PHYSIQUES.isEmpty()) {
            return;
        }
        register(new PhysiqueType("sword_bone", "剑骨", PhysiqueRarity.BLUE,
                "剑类伤害提升，剑意领悟更加容易。"));
        register(new PhysiqueType("spirit_eye", "灵眼", PhysiqueRarity.PURPLE,
                "洞察先机，暴击率提升，更容易发现弱点。"));
        register(new PhysiqueType("jade_bone", "玉骨", PhysiqueRarity.GREEN,
                "根骨清奇，修炼速度小幅提升。"));
        register(new PhysiqueType("thunder_body", "雷体", PhysiqueRarity.ORANGE,
                "移速+20%，攻速+25%，概率触发雷击特效。"));
        register(new PhysiqueType("mystic_vein", "仙脉", PhysiqueRarity.RED,
                "先天仙脉，全属性大幅提升。"));
        loadFromConfig();
    }

    public static void loadFromConfig() {
        JsonConfigUtils.forEachJsonInConfigDir("cultivation/physiques", PhysiqueRegistry::parseJson);
    }

    private static void parseJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        PhysiqueRarity rarity = PhysiqueRarity.fromName(json.get("rarity").getAsString());
        String description = json.has("description") ? json.get("description").getAsString() : "";
        register(new PhysiqueType(id, name, rarity, description));
        LOGGER.debug("Registered physique '{}'", id);
    }

    public static void register(PhysiqueType type) {
        PHYSIQUES.put(type.id(), type);
    }

    public static Optional<PhysiqueType> get(String id) {
        return Optional.ofNullable(PHYSIQUES.get(id));
    }

    public static Collection<PhysiqueType> all() {
        return PHYSIQUES.values();
    }

    public static int size() {
        return PHYSIQUES.size();
    }
}