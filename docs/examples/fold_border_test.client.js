// 折叠卡片描边目测测试，仅使用原版物品。
// 放到 kubejs/client_scripts/fold_border_test.js 后启动客户端。
// 清空 EMI 搜索框，在原版混凝土、纸/宝石、石头附近查看；悬停显示测试组名。
// 搜索会展开匹配物品，因此不要用搜索结果检查折叠描边。
// 左键展开，Alt + 左键收起。测试结束后删除此脚本。
RecipeViewerEvents.fold(event => {
  const blocks = [
    'minecraft:white_concrete', 'minecraft:red_concrete',
    'minecraft:lime_concrete', 'minecraft:blue_concrete'
  ];
  const options = { spread: 4 };

  // 整组共用一个长条外框，中间没有竖线；方块错位堆叠，明暗保持正常。
  event.foldId('bkmef_border_test:blocks', '描边测试：重叠方块 / 间距 4', blocks, options);

  // 透明图标间共用连续背景，后排物品可从前排物品的透明区域露出。
  event.foldId('bkmef_border_test:items', '描边测试：平面物品 / 间距 4', [
    'minecraft:paper', 'minecraft:book', 'minecraft:diamond',
    'minecraft:emerald', 'minecraft:iron_ingot', 'minecraft:gold_ingot'
  ], options);

  // 用足够多的物品强制换行，仍保持和短组相同的 4px 堆叠间距。
  const colors = [
    'white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
    'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'
  ];
  const manyBlocks = [];
  colors.forEach(color => ['wool', 'concrete', 'terracotta'].forEach(kind =>
    manyBlocks.push(`minecraft:${color}_${kind}`)));
  event.foldId('bkmef_border_test:wrap_many', '描边测试：48 项跨行堆叠 / 间距 4', manyBlocks, options);

  event.foldId('bkmef_border_test:overlap', '描边测试：完全重叠 / 间距 0', blocks, {
    spread: 0
  });

  // 无相邻物品遮挡时，四边应完整显示。
  event.foldId('bkmef_border_test:single', '描边测试：单张卡片', ['minecraft:stone'], options);
});
