# XuanXu Mod（修仙模组）

这是一个基于 **NeoForge 1.21.1** 的 Minecraft 修仙模组开发环境。

## 环境要求

- **JDK 21**（已检测到 `C:\Program Files\Java\latest\jdk-21`）
- **IntelliJ IDEA** 或 Eclipse（推荐 IntelliJ IDEA）
- 可访问 Maven 以自动下载 NeoForge / ModDevGradle 依赖

## 快速开始

1. 用 IntelliJ IDEA 打开本文件夹：`D:\我的世界\XiuXianMod`
2. 等待 Gradle 同步完成（首次会下载依赖，可能需要几分钟；项目未附带 Gradle Wrapper 的二进制 jar，IDEA 会自动使用内置 Gradle 或提示你进行配置）
3. 运行 Gradle 任务：
   - `runClient`：启动 Minecraft 客户端
   - `runServer`：启动专用服务器
   - `data`：生成数据
   - `build`：构建 mod 的 jar 文件
4. 构建产物位于 `build/libs/`

## 当前进度：灵根系统（第一部分）

已实现：

- 玩家首次进入世界自动觉醒灵根
- 杂质随机生成：0% ~ 99%
- 五种分配模式：均衡 / 三系分散 / 双系专精 / 单系极致 / 孤注一掷
- 狄利克雷分布分配灵根占比
- 灵根品级：上品 / 中品 / 下品 / 杂灵根 / 废灵根 / 无灵根
- 修炼速度倍率：`1 - 杂质% * 0.3`
- 功法威力固定加成公式：`灵根占比% / 100 * 境界系数`
- 灵根 JSON 扩展注册：`run/config/cultivation/roots/*.json`
- 游戏内指令：
  - `/cultivation lingen`：打开灵根 UI 面板
  - `/cultivation lingen reroll`：重新随机灵根（仅 OP/创造权限测试用）

## 当前进度：灵体系统（第二部分）

已实现：

- 首次进入世界且完成灵根觉醒后，自动进入灵体觉醒
- 随机生成 3 个灵体选项，品质独立判定
- 品质概率：绿色 65% / 蓝色 20% / 紫色 10% / 橙色 4% / 红色 1%
- 玩家三选一，也可以选择放弃成为凡体
- 灵体最多同时拥有一个，后续新灵体会覆盖旧灵体
- 灵体 JSON 扩展注册：`run/config/cultivation/physiques/*.json`
- 游戏内指令：
  - `/cultivation physique`：查看当前灵体
  - `/cultivation physique roll`：重新生成 3 个灵体选项（OP 测试用）
  - `/cultivation physique choose <1|2|3>`：选择对应灵体
  - `/cultivation physique choose none`：放弃灵体，成为凡体

## 项目结构

```
XuanXuMod/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── run/config/cultivation/roots/
│   ├── gold.json
│   ├── wood.json
│   ├── water.json
│   ├── fire.json
│   └── earth.json
├── run/config/cultivation/physiques/
│   ├── sword_bone.json
│   ├── spirit_eye.json
│   ├── jade_bone.json
│   ├── thunder_body.json
│   └── mystic_vein.json
├── run/config/cultivation/traits/
│   ├── sword_genius.json
│   ├── fire_star.json
│   └── ...
├── run/config/cultivation/trait_pools/
│   ├── starting_pool.json
│   └── ...
├── run/config/cultivation/lingen_config.json
└── src/
    └── main/
        ├── java/com/tc/xuanxu/
        │   ├── XuanXuMod.java
        │   ├── command/CultivationCommands.java
        │   ├── lingen/
        │   │   ├── LingGenConfig.java
        │   │   ├── LingGenData.java
        │   │   ├── LingGenEventHandler.java
        │   │   ├── LingGenManager.java
        │   │   ├── LingGenRank.java
        │   │   ├── LingGenRootRegistry.java
        │   │   ├── LingGenType.java
        │   │   └── Realm.java
        │   ├── physique/
        │   │   ├── PhysiqueData.java
        │   │   ├── PhysiqueEventHandler.java
        │   │   ├── PhysiqueManager.java
        │   │   ├── PhysiqueRarity.java
        │   │   ├── PhysiqueRegistry.java
        │   │   └── PhysiqueType.java
        │   ├── trait/
        │   │   ├── TraitData.java
        │   │   ├── TraitDefinition.java
        │   │   ├── TraitManager.java
        │   │   ├── TraitPool.java
        │   │   ├── TraitPoolRegistry.java
        │   │   ├── TraitRarity.java
        │   │   ├── TraitRegistry.java
        │   │   └── TraitSelectionData.java
        │   ├── util/
        │   │   ├── JsonConfigUtils.java
        │   │   └── RandomUtils.java
        │   └── network/（网络包）
        └── resources/
            ├── META-INF/neoforge.mods.toml
            ├── pack.mcmeta
            └── assets/xuanxu/lang/
                ├── en_us.json
                └── zh_cn.json
```

## 常用信息

- Mod ID：`xuanxu`
- 主包名：`com.tc.xuanxu`
- NeoForge 版本：`21.1.234`
- Minecraft 版本：`1.21.1`

## 优化记录

### 修复

- **B 键状态面板失效**：`ClientKeyHandler` 此前从未被注册，按键绑定和按键事件均未挂载；现已在客户端正确注册按键映射与事件监听。
- **灵根/灵体无限重抽**：网络包路径此前可直接重抽灵根与灵体（绕过 OP 权限），现已加入一次性守卫，重抽需走 OP 指令。
- **灵根品级阈值魔法数字**：`LingGenRank.fromPercent` 改为基于枚举 `minPercent` 字段推导，消除魔法数字。
- **语言文件**：修正 `zh_cn.json` 中"玄虚"→"玄墟"的错字，并补充按键绑定翻译（`key.xuanxu.open_status` / `key.categories.xuanxu`）。
- **登录流程**：灵体觉醒现在会直接弹出三选一 GUI，而非仅聊天提示，与文档描述的"自动进入灵体觉醒"一致。

### 代码质量

- 抽取共享工具类：`com.tc.xuanxu.util.RandomUtils`（洗牌/随机挑取）、`com.tc.xuanxu.util.JsonConfigUtils`（配置目录 JSON 加载），消除 4 个注册表与 2 个 Manager 中的重复样板代码。
- **词条稀有度权重生效**：`TraitManager.generateOptions` 现在真正使用词条池的 `rarityWeights` 与 `guaranteedRarity`（此前加载了但从未使用），并按权重随机稀有度抽取、保证保底词条出现。
- **GUI 渲染优化**：三个 GUI 屏幕的背景纹理最近邻过滤从每帧设置改为 `init()` 一次性设置；tooltip 改为直接渲染 `List<Component>`（保留颜色样式），不再拼成单行字符串。
- 清理死代码：未使用的 `formatPercent`、未使用的 import、废弃的 `com.xiuxian` stub 包与 `assets/xiuxian` 资源已删除。

### 觉醒流程锁定与界面优化

- **觉醒期间禁止 ESC 退出**：新增 `FlowLockScreen` 基类，灵根 / 灵体 / 词条三个抽取界面在流程进行中会吞掉 ESC 键，并在底部显示黄字提示；全部抽取完成后面板自动关闭、ESC 恢复可用。命令查看灵根（`/cultivation lingen`）属于查看模式，不受锁定影响。
- **流程可恢复**：中途退出游戏后重新登录，会按进度自动重新弹出对应界面（灵体未选则弹灵体，词条未确认则弹词条），保证流程必须走完。
- **灵体 / 词条界面品质着色**：选项按钮文字按品质着色（绿 / 蓝 / 紫 / 橙 / 红、词条为白 / 绿 / 蓝 / 紫 / 橙），与 tooltip 颜色一致。
- **词条界面按钮过大导致显示不全**：原布局硬编码 5 列 × 150px = 790px，GUI 缩放较大（逻辑宽度较小）时会溢出屏幕；现改为按屏幕宽度自适应列数，并将卡片高度由 48 缩减为 20。
- **提示文字与底部按钮重叠**：锁定提示改到屏幕最底部（`height-12`），避开 `height-40` 的按钮行。

## OP 调试面板

管理员或单人创造模式玩家可使用命令打开调试面板：

```
/cultivation op
```

面板会显示当前灵根（含品级与颜色）、灵体、天命词条，并提供四个操作：

| 按钮 | 作用 |
|---|---|
| 重抽灵根 | 重新随机整份灵根数据（杂质 + 五行占比） |
| 重抽灵体 | 从灵体池中随机抽取一个并直接替换当前灵体 |
| 重抽词条 | 随机抽取 4 个天命词条（自动跳过冲突组重复）并直接替换 |
| 重置全部 | 清空灵根 / 灵体 / 词条数据，并立即重新开始觉醒流程 |

所有操作均可无限次点击，每次操作后服务端会回发最新状态就地刷新面板（不重建界面）。

**权限说明**：满足以下任一条件即可使用（服务端校验，客户端不可信）：
- 权限等级 ≥ 2（多人服务器 OP、单人世界开启作弊）
- 单人存档中的创造模式玩家

> 注意：普通玩家的灵根 / 灵体抽取仍受一次性守卫限制，无法通过重发网络包刷取；只有上述权限才可绕过。


