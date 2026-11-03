package com.tc.xuanxu.lingen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 灵根生成参数配置，对应文档 3.4 / 3.8。
 * 支持从 config/cultivation/lingen_config.json 覆盖默认值。
 */
public final class LingGenConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final double DEFAULT_MAX_IMPURITY = 99.0;
    private static final double DEFAULT_BALANCED_MIN = 85.0;
    private static final double DEFAULT_THREE_MIN = 70.0;
    private static final double DEFAULT_TWO_MIN = 40.0;
    private static final double DEFAULT_ONE_MIN = 10.0;

    private static final double DEFAULT_PRIMARY_RATIO_3 = 0.75;
    private static final double DEFAULT_PRIMARY_RATIO_2 = 0.90;
    private static final double DEFAULT_PRIMARY_RATIO_1 = 0.95;

    private static double maxImpurity = DEFAULT_MAX_IMPURITY;
    private static double balancedMin = DEFAULT_BALANCED_MIN;
    private static double threeMin = DEFAULT_THREE_MIN;
    private static double twoMin = DEFAULT_TWO_MIN;
    private static double oneMin = DEFAULT_ONE_MIN;

    private static double primaryRatio3 = DEFAULT_PRIMARY_RATIO_3;
    private static double primaryRatio2 = DEFAULT_PRIMARY_RATIO_2;
    private static double primaryRatio1 = DEFAULT_PRIMARY_RATIO_1;

    private static boolean loaded = false;

    private LingGenConfig() {
    }

    public static void init() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path config = FMLPaths.CONFIGDIR.get().resolve("cultivation/lingen_config.json");
        if (!Files.exists(config)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(config)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            maxImpurity = getDouble(json, "max_impurity", DEFAULT_MAX_IMPURITY);
            balancedMin = getDouble(json, "balanced_min_available", DEFAULT_BALANCED_MIN);
            threeMin = getDouble(json, "three_min_available", DEFAULT_THREE_MIN);
            twoMin = getDouble(json, "two_min_available", DEFAULT_TWO_MIN);
            oneMin = getDouble(json, "one_min_available", DEFAULT_ONE_MIN);
            primaryRatio3 = getDouble(json, "three_primary_ratio", DEFAULT_PRIMARY_RATIO_3);
            primaryRatio2 = getDouble(json, "two_primary_ratio", DEFAULT_PRIMARY_RATIO_2);
            primaryRatio1 = getDouble(json, "one_primary_ratio", DEFAULT_PRIMARY_RATIO_1);
            LOGGER.info("LingGen config loaded from {}", config);
        } catch (Exception e) {
            LOGGER.error("Failed to load lingen config: {}", config, e);
        }
    }

    public static void reload() {
        loaded = false;
        maxImpurity = DEFAULT_MAX_IMPURITY;
        balancedMin = DEFAULT_BALANCED_MIN;
        threeMin = DEFAULT_THREE_MIN;
        twoMin = DEFAULT_TWO_MIN;
        oneMin = DEFAULT_ONE_MIN;
        primaryRatio3 = DEFAULT_PRIMARY_RATIO_3;
        primaryRatio2 = DEFAULT_PRIMARY_RATIO_2;
        primaryRatio1 = DEFAULT_PRIMARY_RATIO_1;
        init();
    }

    private static double getDouble(JsonObject json, String key, double def) {
        return json.has(key) ? json.get(key).getAsDouble() : def;
    }

    public static double maxImpurity() {
        return maxImpurity;
    }

    public static double balancedMin() {
        return balancedMin;
    }

    public static double threeMin() {
        return threeMin;
    }

    public static double twoMin() {
        return twoMin;
    }

    public static double oneMin() {
        return oneMin;
    }

    public static double primaryRatio3() {
        return primaryRatio3;
    }

    public static double primaryRatio2() {
        return primaryRatio2;
    }

    public static double primaryRatio1() {
        return primaryRatio1;
    }
}