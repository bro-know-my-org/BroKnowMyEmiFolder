# Bro Know My EMI Folder

[![Latest release](https://img.shields.io/github/v/release/bro-know-my-org/BroKnowMyEmiFolder?style=flat-square&logo=github)](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/releases/latest)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1564728?style=flat-square&logo=curseforge&label=CurseForge%20downloads&color=F16436)](https://www.curseforge.com/projects/1564728)
[![MIT license](https://flat.badgen.net/github/license/bro-know-my-org/BroKnowMyEmiFolder)](LICENSE)

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-F99A36?style=flat-square)](https://neoforged.net/)
[![Java 21](https://img.shields.io/badge/Java-21-007396?style=flat-square)](https://adoptium.net/temurin/releases/?version=21)

[English](#bro-know-my-emi-folder) | [中文](#中文)

Bro Know My EMI Folder is an EMI addon for Minecraft 1.21.1 / NeoForge. It adds collapsible groups to EMI's index sidebar, with group definitions driven by KubeJS.

Read the [documentation](https://emi-folder.bro-know-my.org/) for guides and the generated KubeJS API reference.

The folding behavior is inspired by REI's collapsible entries. Friendly link: [RoughlyEnoughItems](https://github.com/shedaniel/RoughlyEnoughItems).

## Features

- Collapse EMI index entries into KubeJS-defined groups.
- Search still works for folded entries. Searching for a folded member shows the matching stack.
- Search results are automatically deduplicated. If one stack belongs to multiple fold groups, it appears once and its tooltip lists every owning group.
- One item can belong to multiple fold groups in the normal EMI index.
- Left-click a collapsed group to expand it. Alt-left-click an expanded member to collapse its group.
- Folded previews use a card spread layout with configurable spacing and background color.
- KubeJS `RecipeViewerEvents.groupEntries` definitions are also consumed as fold groups where practical.

## Usage

Add a KubeJS client script:

```js
RecipeViewerEvents.fold(event => {
  event.fold('broknowmyemifolder:ores', 'translate:emi_group.broknowmyemifolder.ores', '#c:ores')
  event.fold('broknowmyemifolder:tools', 'translate:emi_group.broknowmyemifolder.tools', ['#c:tools', 'minecraft:shears'])
  event.foldId('broknowmyemifolder:boats', 'translate:emi_group.broknowmyemifolder.boats', ['minecraft:oak_boat', 'minecraft:spruce_boat'], {
    spread: 4,
    color: 'rainbow'
  })
  event.foldMod('broknowmyemifolder:citadel', 'translate:emi_group.broknowmyemifolder.citadel', '@citadel', {
    color: 'random'
  })
  event.foldSpawnEggs('broknowmyemifolder:spawn_eggs', 'Spawn Eggs', {
    spread: 4,
    color: 'random'
  })
  event.unfoldAll('quark:seed_pouch')
})
```

Group names may be literal text, components, or strings prefixed with `translate:`.

Available methods:

```js
event.fold(id, name, filter, options?)
event.fold(name, filter, options?)
event.foldFluid(id, name, filter, options?)
event.foldFluid(name, filter, options?)
event.foldId(id, name, ids, options?)
event.foldId(name, ids, options?)
event.foldMod(id, name, mods, options?)
event.foldMod(name, mods, options?)
event.foldSpawnEggs(id, name, options?)
event.foldSpawnEggs(name, options?)
event.unfold(groupId, filter)
event.unfoldFluid(groupId, filter)
event.unfoldId(groupId, ids)
event.unfoldAll(filter)
event.unfoldAllFluid(filter)
event.unfoldAllId(ids)
```

`unfold` removes matching entries from one fold group. `unfoldAll` removes matching entries from every fold group.

`foldMod` folds entries by EMI stack id namespace. It accepts mod ids with or without a leading `@`, such as `'@citadel'`.

`foldSpawnEggs` folds items implemented as Minecraft `SpawnEggItem`, independent of item id naming.

For combined filters, pass a structured object. Plain `#tag` matches item tags; use `blockTag` for the block tags of block items.

```js
event.fold('my_pack:tools_without_axes', 'Tools Without Axes', {
  tag: '#c:tools',
  none: '#c:tools/axes'
})

event.fold('my_pack:wooden_block_items', 'Wooden Block Items', {
  mod: 'minecraft',
  blockTag: '#minecraft:logs'
})
```

Options:

```js
{
  spread: 4,
  color: 'rainbow'
}
```

`spread` controls the horizontal pixel offset between preview cards. `color` accepts a number, `'#RRGGBB'`, `'rainbow'`, or `'random'`.

Omitting `color` assigns new groups colors from a muted rainbow palette; redefining an existing group id preserves its current color. Backgrounds stay dark; borders and tooltip accents follow the group color in both collapsed and expanded views.

See [KubeJS API](docs/kubejs-api.md) for the supported script methods and options.

## In-game Settings

Open EMI's settings from the bottom-left EMI button. The `BKMEF` group adds:

- `Enable EMI index folding`: disables or enables the folded index entries without removing KubeJS scripts.
- `Show fold reload messages`: disables or enables the BKMEF chat message shown after fold groups are loaded or reloaded.

## 中文

[English](#bro-know-my-emi-folder) | [中文](#中文)

Bro Know My EMI Folder 是一个适用于 Minecraft 1.21.1 / NeoForge 的 EMI 附属模组。它为 EMI 的索引侧边栏添加可折叠分组，并通过 KubeJS 定义分组内容。

使用指南和自动生成的 KubeJS 接口参考见[文档站](https://emi-folder.bro-know-my.org/)。

这个折叠行为受到 REI collapsible entries 的启发。友情链接：[RoughlyEnoughItems](https://github.com/shedaniel/RoughlyEnoughItems)。

## 功能

- 将 EMI 索引侧边栏中的条目折叠成由 KubeJS 定义的分组。
- 折叠后仍支持正常搜索。搜索组内物品时，会显示匹配到的具体条目。
- 搜索结果会自动去重。同一个物品属于多个折叠组时，只显示一次，并在 tooltip 中列出它所属的所有组。
- 普通 EMI 索引中允许同一个物品进入多个折叠组。
- 左键点击折叠组展开；Alt+左键点击展开后的成员收起对应组。
- 折叠预览使用扑克牌式平铺，支持自定义间距和背景颜色。
- 会尽量兼容 KubeJS 原有的 `RecipeViewerEvents.groupEntries` 定义。

## 使用

添加一个 KubeJS client script：

```js
RecipeViewerEvents.fold(event => {
  event.fold('broknowmyemifolder:ores', 'translate:emi_group.broknowmyemifolder.ores', '#c:ores')
  event.fold('broknowmyemifolder:tools', 'translate:emi_group.broknowmyemifolder.tools', ['#c:tools', 'minecraft:shears'])
  event.foldId('broknowmyemifolder:boats', 'translate:emi_group.broknowmyemifolder.boats', ['minecraft:oak_boat', 'minecraft:spruce_boat'], {
    spread: 4,
    color: 'rainbow'
  })
  event.foldMod('broknowmyemifolder:citadel', 'translate:emi_group.broknowmyemifolder.citadel', '@citadel', {
    color: 'random'
  })
  event.foldSpawnEggs('broknowmyemifolder:spawn_eggs', '刷怪蛋', {
    spread: 4,
    color: 'random'
  })
  event.unfoldAll('quark:seed_pouch')
})
```

组名可以是普通文本、文本组件，或带 `translate:` 前缀的翻译键字符串。

可用方法：

```js
event.fold(id, name, filter, options?)
event.fold(name, filter, options?)
event.foldFluid(id, name, filter, options?)
event.foldFluid(name, filter, options?)
event.foldId(id, name, ids, options?)
event.foldId(name, ids, options?)
event.foldMod(id, name, mods, options?)
event.foldMod(name, mods, options?)
event.foldSpawnEggs(id, name, options?)
event.foldSpawnEggs(name, options?)
event.unfold(groupId, filter)
event.unfoldFluid(groupId, filter)
event.unfoldId(groupId, ids)
event.unfoldAll(filter)
event.unfoldAllFluid(filter)
event.unfoldAllId(ids)
```

`unfold` 会把匹配条目从指定折叠组里拿出来。`unfoldAll` 会把匹配条目从所有折叠组里拿出来。

`foldMod` 会按 EMI stack id 的命名空间折叠条目。它接受带或不带 `@` 前缀的 mod id，例如 `'@citadel'`。

`foldSpawnEggs` 会折叠底层实现为 Minecraft `SpawnEggItem` 的物品，不依赖物品 id 是否以 `_spawn_egg` 结尾。

复杂过滤可以传结构化对象。普通 `#tag` 匹配的是物品 tag；如果要按方块物品对应方块的方块 tag 过滤，用 `blockTag`。

```js
event.fold('my_pack:tools_without_axes', '非斧工具', {
  tag: '#c:tools',
  none: '#c:tools/axes'
})

event.fold('my_pack:wooden_block_items', '木质方块物品', {
  mod: 'minecraft',
  blockTag: '#minecraft:logs'
})
```

可选参数：

```js
{
  spread: 4,
  color: 'rainbow'
}
```

`spread` 控制预览卡片之间的横向像素偏移。`color` 支持数字、`'#RRGGBB'`、`'rainbow'` 或 `'random'`。

省略 `color` 时，新分组按注册顺序分配柔和彩虹色；重新定义已有分组 id 时保留原配色。背景保持深色，折叠框、展开框和提示文字的强调色跟随所属分组。

支持的脚本方法和参数见 [KubeJS API](docs/kubejs-api.md)。

## 游戏内设置

从左下角 EMI 按钮打开 EMI 设置。`BKMEF` 分组里有：

- `启用 EMI 索引折叠`：关闭或开启折叠索引，不需要删除 KubeJS 脚本。
- `显示折叠重载播报`：关闭或开启折叠分组加载/重载后的聊天栏提示。

## 文档开发

需要 JDK 21、Node.js 24 LTS 和 `package.json` 指定的 pnpm 版本。

```sh
pnpm install --frozen-lockfile
pnpm docs:dev
```

- `pnpm docs:generate`：Gradle 从 `FoldKubeEvent.java` 的 `@Info` / `@Param` 导出 `build/docs/kubejs-api.json`，再生成 `docs/api/kubejs.md`。
- `pnpm docs:build`：重新生成接口参考，检查导出契约并构建 VitePress；输出为 `docs/.vitepress/dist/`。
- `pnpm docs:preview`：预览已构建的站点。

接口注解的描述使用英文原文；方法和参数说明必须是非空字符串字面量或 Java 文本块，`params` 支持注解数组或单个 `@Param` 简写。新增公开方法及参数时补齐注解，过期或缺失的参数说明会阻止构建。Rhino 自动注入的首位 `Context` 参数不展示在脚本签名里；`Object` 的具体可传值通过参数说明解释。

生成产物无需提交。教程维护在 `docs/`，页面通过代码片段导入引用示例脚本。修改接口注解后，重新运行 `pnpm docs:generate` 即可更新本地开发页面。

CI 在 PR 中校验文档，`master` 更新后自动部署到 GitHub Pages。站点域名为 `emi-folder.bro-know-my.org`，使用默认根路径；`docs/public/CNAME` 随静态站点一同输出。仓库 Pages 的 Source 使用 GitHub Actions，Custom domain 与 CNAME 保持一致。

Cloudflare DNS 添加 CNAME：名称 `emi-folder`，目标 `bro-know-my-org.github.io`，代理状态为 DNS only，TTL 为 Auto。DNS 生效且 GitHub 证书签发完成后，在仓库 Pages 设置中启用 Enforce HTTPS。

首次部署及证书验证通过后，可启用 Cloudflare 代理（橙云），SSL/TLS 使用 Full (strict)。缓存规则仅匹配文档子域名：`/assets/` 中的构建资源在边缘和浏览器缓存一年，其余页面在边缘缓存 5 分钟、浏览器不缓存；400–599 响应不缓存。更新页面最多需要等待 5 分钟，带哈希的资源文件名会随内容变化。
