package com.tc.xuanxu.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 配置目录 JSON 加载工具，消除各注册表里重复的目录遍历/解析样板代码。
 */
public final class JsonConfigUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private JsonConfigUtils() {
    }

    /**
     * 遍历 config 目录（如 cultivation/roots）下所有 .json 文件并逐个交给 consumer 解析。
     * 目录不存在时静默返回。
     */
    public static void forEachJsonInConfigDir(String subPath, Consumer<JsonObject> consumer) {
        Path dir = FMLPaths.CONFIGDIR.get().resolve(subPath);
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> parseFile(path, consumer));
        } catch (IOException e) {
            LOGGER.error("Failed to list config directory: {}", dir, e);
        }
    }

    private static void parseFile(Path path, Consumer<JsonObject> consumer) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            consumer.accept(json);
            LOGGER.info("Loaded config '{}'", path);
        } catch (Exception e) {
            LOGGER.error("Failed to parse config: {}", path, e);
        }
    }

    /**
     * 解析 JSON 中的字符串数组字段（缺省或非数组时返回空列表）。
     */
    public static List<String> parseStringList(JsonObject json, String key) {
        List<String> result = new ArrayList<>();
        if (json.has(key) && json.get(key).isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray(key)) {
                if (element.isJsonPrimitive()) {
                    result.add(element.getAsString());
                }
            }
        }
        return result;
    }

    /**
     * 解析 JSON 中的 JsonArray 字段；缺省或非数组时返回空数组。
     */
    public static JsonArray optJsonArray(JsonObject json, String key) {
        if (json.has(key) && json.get(key).isJsonArray()) {
            return json.getAsJsonArray(key);
        }
        return new JsonArray();
    }
}