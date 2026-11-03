package com.tc.xuanxu.trait;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tc.xuanxu.util.JsonConfigUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 词条池注册表，加载 config/cultivation/trait_pools/*.json。
 */
public final class TraitPoolRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, TraitPool> POOLS = new LinkedHashMap<>();

    private TraitPoolRegistry() {
    }

    public static void init() {
        if (!POOLS.isEmpty()) {
            return;
        }
        // 先注册默认词条池，确保 starting_pool 始终存在
        register(defaultPool());
        loadFromConfig();
        // 若配置被覆盖导致 starting_pool 缺失，重新补回默认
        if (!POOLS.containsKey("starting_pool")) {
            POOLS.put("starting_pool", defaultPool());
        }
    }

    private static TraitPool defaultPool() {
        return new TraitPool(
                "starting_pool", 1,
                Map.of(
                        TraitRarity.COMMON, 30.0,
                        TraitRarity.UNCOMMON, 25.0,
                        TraitRarity.RARE, 25.0,
                        TraitRarity.EPIC, 15.0,
                        TraitRarity.LEGENDARY, 5.0
                ),
                TraitRarity.COMMON,
                4,
                List.of()
        );
    }

    public static void reload() {
        POOLS.clear();
        init();
    }

    public static void loadFromConfig() {
        JsonConfigUtils.forEachJsonInConfigDir("cultivation/trait_pools", TraitPoolRegistry::parseJson);
    }

    private static void parseJson(JsonObject json) {
        String id = json.get("id").getAsString();
        int version = json.has("version") ? json.get("version").getAsInt() : 1;
        Map<TraitRarity, Double> weights = parseWeights(json.has("pools") && json.get("pools").isJsonObject()
                ? json.getAsJsonObject("pools") : null);
        TraitRarity guaranteed = TraitRarity.fromName(json.has("guaranteed_rarity")
                ? json.get("guaranteed_rarity").getAsString() : "common");
        int maxTraits = json.has("max_traits") ? json.get("max_traits").getAsInt() : 4;
        List<String> excludes = JsonConfigUtils.parseStringList(json, "exclude_traits");
        register(new TraitPool(id, version, weights, guaranteed, maxTraits, excludes));
        LOGGER.debug("Registered trait pool '{}'", id);
    }

    private static Map<TraitRarity, Double> parseWeights(JsonObject pools) {
        Map<TraitRarity, Double> weights = new LinkedHashMap<>();
        if (pools != null) {
            for (Map.Entry<String, JsonElement> entry : pools.entrySet()) {
                TraitRarity rarity = TraitRarity.fromName(entry.getKey());
                weights.put(rarity, entry.getValue().getAsDouble());
            }
        }
        return weights;
    }

    public static void register(TraitPool pool) {
        POOLS.put(pool.id(), pool);
    }

    public static Optional<TraitPool> get(String id) {
        return Optional.ofNullable(POOLS.get(id));
    }

    public static TraitPool getOrStarting(String id) {
        TraitPool pool = get(id).orElse(null);
        if (pool != null) {
            return pool;
        }
        TraitPool starting = POOLS.get("starting_pool");
        if (starting != null) {
            return starting;
        }
        // 极端兜底：返回一个默认池
        return defaultPool();
    }

    public static Map<String, TraitPool> all() {
        return Map.copyOf(POOLS);
    }
}