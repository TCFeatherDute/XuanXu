package com.tc.xuanxu.lingen;

/**
 * 灵根类型定义。
 * 默认包含金、木、水、火、土五种五行灵根，后续可通过 JSON 扩展风、雷、冰等。
 */
public record LingGenType(String id, String displayName, int color) {
}
