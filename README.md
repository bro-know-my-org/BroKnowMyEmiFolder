# Bro Know My EMI Folder

[English](#bro-know-my-emi-folder) | [中文](#中文)

Bro Know My EMI Folder is an EMI addon for Minecraft 1.21.1 / NeoForge. It adds collapsible groups to EMI's index sidebar, with group definitions driven by KubeJS.

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

Options:

```js
{
  spread: 4,
  color: 'rainbow'
}
```

`spread` controls the horizontal pixel offset between preview cards. `color` accepts a number, `'#RRGGBB'`, `'rainbow'`, or `'random'`.

See [KubeJS API](docs/kubejs-api.md) for the supported script methods and options.

## 中文

[English](#bro-know-my-emi-folder) | [中文](#中文)

Bro Know My EMI Folder 是一个适用于 Minecraft 1.21.1 / NeoForge 的 EMI 附属模组。它为 EMI 的索引侧边栏添加可折叠分组，并通过 KubeJS 定义分组内容。

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

可选参数：

```js
{
  spread: 4,
  color: 'rainbow'
}
```

`spread` 控制预览卡片之间的横向像素偏移。`color` 支持数字、`'#RRGGBB'`、`'rainbow'` 或 `'random'`。

支持的脚本方法和参数见 [KubeJS API](docs/kubejs-api.md)。
