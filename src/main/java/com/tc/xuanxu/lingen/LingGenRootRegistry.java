package com.tc.xuanxu.lingen;

import com.google.gson.JsonObject;
import com.tc.xuanxu.util.JsonConfigUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 灵根类型注册表。
 * <p>
 * 默认注册金木水火土五种灵根。
 * 同时会加载 {@code config/cultivation/roots/*.json} 中定义的新灵根（风、雷、冰等）。
 */
public final class LingGenRootRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, LingGenType> ROOTS = new LinkedHashMap<>();

    private LingGenRootRegistry() {
    }

    public static void init() {
        if (!ROOTS.isEmpty()) {
            return;
        }
        register(new LingGenType("gold", "金灵根", 0xFFD700));
        register(new LingGenType("wood", "木灵根", 0x55AA33));
        register(new LingGenType("water", "水灵根", 0x3399FF));
        register(new LingGenType("fire", "火灵根", 0xFF5533));
        register(new LingGenType("earth", "土灵根", 0xAA7733));
        loadFromConfig();
    }

    /**
     * 从运行配置目录加载灵根 JSON。
     * 格式示例：
     * <pre>
     * {
     *   "id": "wind",
     *   "name": "风灵根",
     *   "color": 16777215
     * }
     * </pre>
     */
    public static void loadFromConfig() {
        JsonConfigUtils.forEachJsonInConfigDir("cultivation/roots", LingGenRootRegistry::parseJson);
    }

    private static void parseJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        int color = json.has("color") ? json.get("color").getAsInt() : 0xFFFFFF;
        register(new LingGenType(id, name, color));
        LOGGER.debug("Registered LingGen root '{}'", id);
    }

    public static void register(LingGenType type) {
        ROOTS.put(type.id(), type);
    }

    public static Optional<LingGenType> get(String id) {
        return Optional.ofNullable(ROOTS.get(id));
    }

    public static Collection<LingGenType> all() {
        return ROOTS.values();
    }

    public static int size() {
        return ROOTS.size();
    }
}