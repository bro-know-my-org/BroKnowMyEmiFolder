# Bro Know My EMI Folder

[![Latest release](https://img.shields.io/github/v/release/bro-know-my-org/BroKnowMyEmiFolder?style=flat-square)](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/releases/latest)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1564728?style=flat-square&logo=curseforge&color=F16436)](https://www.curseforge.com/projects/1564728)
[![MIT license](https://flat.badgen.net/github/license/bro-know-my-org/BroKnowMyEmiFolder)](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/master/LICENSE)

[English](#english) · [简体中文](#简体中文)

**[Documentation & changelog / 文档与更新记录 →](https://emi-folder.bro-know-my.org/)**

## English

Tired of color variants, endless combinations, and similar-looking entries filling page after page of EMI? Meet BKMEF, an EMI addon for **Minecraft 1.21.1 · NeoForge**. Use **KubeJS** to group entries your way and customize how each group looks.

### Fold it your way

Your groups, your rules. Customize what folds together and how it looks with KubeJS.

- **Choose the contents.** Group items and fluids by ID, tag, mod, or combined filters. Exclude individual entries, or let an item belong to several folders.
- **Choose the look.** Set folder names, translations, colors, and card spacing. Use fixed colors, a rainbow, or randomly assigned colors.
- **Keep browsing naturally.** Left-click to expand; Alt + left-click a member to collapse. Search finds individual items inside folders and deduplicates overlapping results.
- **Performance and polish.** Indexed matching and cached layouts reduce repeated work; group reloads take effect together, with careful handling of row boundaries and colors. [Under the hood](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/master/src/main/java/io/github/broknowmyorg/bkmef/emi/FoldRegistry.java).

![Custom folder colors and card spacing in the English game client](https://raw.githubusercontent.com/bro-know-my-org/BroKnowMyEmiFolder/master/docs/public/images/folders-en.webp)

*A vanilla-only example with custom names, colors, and card spacing. Folders can stay folded while others are expanded.*

![English search result showing a diamond shared by two custom folders](https://raw.githubusercontent.com/bro-know-my-org/BroKnowMyEmiFolder/master/docs/public/images/search-en.webp)

*One diamond, two folders: search shows the item once and lists both memberships in its tooltip.*

**[Scripts & examples →](https://emi-folder.bro-know-my.org/examples)** · [Filters, styling & API](https://emi-folder.bro-know-my.org/kubejs-api)

## 简体中文

厌烦了 EMI 里一页又一页的换色变体、组合变体和其他相似条目？BKMEF 来了！作为适用于 **Minecraft 1.21.1 · NeoForge** 的 EMI 附属模组，它让你通过 **KubeJS** 自由定义分组内容和外观。

### 怎么折，都有面

想怎么折，就怎么折：分组规则和外观都能用 KubeJS 自定义。

- **内容自由组合。** 按 ID、标签、模组或组合条件归类物品与流体；可以排除指定条目，也可以让一个物品同时属于多个组。
- **外观自己决定。** 自定义组名、翻译、颜色和卡片间距，支持固定色、彩虹色和随机配色。
- **照常浏览和搜索。** 左键展开，Alt + 左键点击成员收起。搜索直接显示组内匹配的物品，跨组重复的结果只显示一次。
- **性能与细节也有打磨。** 索引与缓存减少重复计算，分组重载整体生效，跨行布局与配色也经过专门处理。[了解实现](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/master/src/main/java/io/github/broknowmyorg/bkmef/emi/FoldRegistry.java)。

![中文客户端中的自定义分组颜色与卡片间距](https://raw.githubusercontent.com/bro-know-my-org/BroKnowMyEmiFolder/master/docs/public/images/folders-zh.webp)

*仅用原版物品演示自定义组名、配色和卡片间距；不同分组可以分别展开或保持折叠。*

![中文搜索结果：钻石同时属于两个自定义分组](https://raw.githubusercontent.com/bro-know-my-org/BroKnowMyEmiFolder/master/docs/public/images/search-zh.webp)

*一颗钻石，两个分组：搜索结果只出现一次，悬停提示列出它所属的两个组。*

**[脚本与示例 →](https://emi-folder.bro-know-my.org/examples)** · [过滤、外观与 API](https://emi-folder.bro-know-my.org/kubejs-api)

---

[Report an issue / 反馈问题](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/issues) · [MIT](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/master/LICENSE) · Inspired by / 折叠交互灵感来自 [REI](https://github.com/shedaniel/RoughlyEnoughItems)
