// Bro Know My Emi Folder example client script.
// Bro Know My Emi Folder 示例客户端脚本。
//
// Put this file under:
// 放到这个位置：
//
//   kubejs/client_scripts/emi_folder.js
//
// This example is based on a real Create Delight Project Rebirth script. It shows
// item/tag folds, exact EMI id folds, spawn egg folding, color-variant folding,
// and pack-specific bucket groups.
// 这个示例来自实际的 Create Delight Project Rebirth 脚本。它演示了物品/标签折叠、
// 精确 EMI id 折叠、刷怪蛋折叠、染色变体折叠，以及整合包专用桶物品分组。

// Namespace used by this pack's fold group ids and translation keys.
// 这个整合包用于折叠组 id 和翻译键的命名空间。
const FOLD_NAMESPACE = 'createdelightcore';

// Builds a stable fold group id.
// 生成稳定的折叠组 id。
const groupId = (key) => `${FOLD_NAMESPACE}:${key}`;

// Builds a translatable group name. Add matching keys to a lang file such as:
//   kubejs/assets/createdelightcore/lang/en_us.json
//   kubejs/assets/createdelightcore/lang/zh_cn.json
// Example lang entries:
//   en_us.json: "emi_group.createdelightcore.spawn_eggs": "Spawn Eggs"
//   zh_cn.json: "emi_group.createdelightcore.spawn_eggs": "刷怪蛋"
//
// 生成可翻译的折叠组名称。请在 lang 文件里补对应 key，例如：
//   kubejs/assets/createdelightcore/lang/en_us.json
//   kubejs/assets/createdelightcore/lang/zh_cn.json
// 示例语言条目：
//   en_us.json: "emi_group.createdelightcore.spawn_eggs": "Spawn Eggs"
//   zh_cn.json: "emi_group.createdelightcore.spawn_eggs": "刷怪蛋"
const groupName = (key) => `translate:emi_group.${FOLD_NAMESPACE}.${key}`;

// Normalizes one id or an id array. Bare paths default to the minecraft namespace.
// 规范化单个 id 或 id 数组。没有命名空间的路径会默认补成 minecraft 命名空间。
const ids = (paths) =>
  (Array.isArray(paths) ? paths : [paths]).map((path) =>
    path.includes(':') ? path : `minecraft:${path}`
  );

// Minecraft's dye color order. Reusing this list keeps color families predictable.
// Minecraft 染料颜色顺序。复用同一份列表可以让染色家族分组保持稳定。
const COLORS = [
  'white',
  'light_gray',
  'gray',
  'black',
  'brown',
  'red',
  'orange',
  'yellow',
  'lime',
  'green',
  'cyan',
  'light_blue',
  'blue',
  'purple',
  'magenta',
  'pink',
];

// Builds ids like minecraft:white_wool, minecraft:red_wool, ...
// 生成类似 minecraft:white_wool、minecraft:red_wool 的 id 列表。
const colorIds = (suffix, mod) => {
  mod = mod || 'minecraft';
  return COLORS.map((variantColor) => `${mod}:${variantColor}_${suffix}`);
};

// Builds ids for mods that use suffix colors, for example:
// lightmanscurrency:freezer, lightmanscurrency:freezer_white, ...
// 为把颜色放在末尾的模组生成 id，例如：
// lightmanscurrency:freezer、lightmanscurrency:freezer_white 等。
const colorSuffixIds = (base) => {
  const result = [base];
  COLORS.forEach((variantColor) => result.push(`${base}_${variantColor}`));
  return result;
};

// Match longer color names first so "light_gray" wins before "gray".
// 先匹配更长的颜色名，避免 "light_gray" 被错误识别成 "gray"。
const COLOR_MATCHES = COLORS.slice().sort((a, b) => b.length - a.length);

// Splits an id into namespace and path. Bare paths default to minecraft.
// 拆分 id 的命名空间和路径。没有命名空间时默认 minecraft。
const splitId = (id) => {
  const value = id.includes(':') ? id : `minecraft:${id}`;
  const index = value.indexOf(':');
  return {
    namespace: value.substring(0, index),
    path: value.substring(index + 1),
  };
};

// Replaces a color token in common id layouts:
//   white_wool            -> __COLOR___wool
//   super_candle_red      -> super_candle___COLOR__
//   framed_white_glass    -> framed___COLOR___glass
//
// 替换常见 id 结构里的颜色片段：
//   white_wool            -> __COLOR___wool
//   super_candle_red      -> super_candle___COLOR__
//   framed_white_glass    -> framed___COLOR___glass
const replaceColor = (path, color, replacement) => {
  if (path.indexOf(`${color}_`) === 0) {
    return replacement + path.substring(color.length);
  }

  const suffix = `_${color}`;
  const suffixIndex = path.lastIndexOf(suffix);
  if (suffixIndex > 0 && suffixIndex === path.length - suffix.length) {
    return path.substring(0, path.length - color.length) + replacement;
  }

  const middle = `_${color}_`;
  const middleIndex = path.indexOf(middle);
  if (middleIndex >= 0) {
    return (
      path.substring(0, middleIndex + 1) +
      replacement +
      path.substring(middleIndex + 1 + color.length)
    );
  }

  return null;
};

// Given one sample id, generate the whole color family when a dye color is found.
// Special case: minecraft:shulker_box has an uncolored base plus colored variants.
//
// 给一个样本 id，如果能识别出染料颜色，就生成完整染色家族。
// 特例：minecraft:shulker_box 同时存在无色基础版本和染色版本。
const colorFamilyIds = (sample) => {
  const parsed = splitId(sample);
  if (parsed.namespace === 'minecraft' && parsed.path === 'shulker_box') {
    return [sample].concat(colorIds('shulker_box'));
  }

  let matchedColor;
  let replacedPath;
  for (let i = 0; i < COLOR_MATCHES.length; i++) {
    matchedColor = COLOR_MATCHES[i];
    replacedPath = replaceColor(parsed.path, matchedColor, '__COLOR__');
    if (replacedPath != null) {
      return COLORS.map(
        (targetColor) => `${parsed.namespace}:${replacedPath.replace('__COLOR__', targetColor)}`
      );
    }
  }

  return [sample];
};

// Builds a stable fold key from one sample id by removing the color token.
// Example: minecraft:white_wool -> minecraft_wool.
//
// 从样本 id 生成稳定的折叠组 key，做法是移除颜色片段。
// 例如：minecraft:white_wool -> minecraft_wool。
const colorFamilyKey = (sample) => {
  const parsed = splitId(sample);
  let path = parsed.path;
  let keyPath;
  for (let i = 0; i < COLOR_MATCHES.length; i++) {
    keyPath = replaceColor(path, COLOR_MATCHES[i], '');
    if (keyPath != null) {
      path = keyPath.replace(/__+/g, '_').replace(/^_+|_+$/g, '');
      break;
    }
  }
  return `${parsed.namespace}_${path}`;
};

// One representative id for each color family you want to fold.
// The helper expands each sample into all dye-color variants.
//
// 每个需要折叠的染色家族只写一个代表性 id。
// helper 会把样本展开成所有染料颜色变体。
const COLOR_FOLD_SAMPLES = [
  'minecraft:white_wool',
  'minecraft:white_carpet',
  'minecraft:white_concrete',
  'minecraft:white_glazed_terracotta',
  'minecraft:red_concrete_powder',
  'minecraft:black_stained_glass',
  'quark:pink_framed_glass',
  'minecraft:light_gray_stained_glass_pane',
  'minecraft:shulker_box',
  'minecraft:light_gray_bed',
  'minecraft:orange_candle',
  'supplementaries:bunting_white',
  'minecraft:cyan_banner',
  'the_bumblezone:super_candle_red',
  'the_bumblezone:string_curtain_white',
  'lightmanscurrency:gacha_machine_white',
  'comforts:sleeping_bag_white',
  'comforts:hammock_white',
  'minecraft:white_terracotta',
  'quark:white_shingles',
  'quark:white_shingles_stairs',
  'quark:white_shingles_slab',
  'quark:white_shingles_vertical_slab',
  'quark:white_framed_glass_pane',
  'quark:white_shard',
  'supplementaries:candle_holder_white',
  'supplementaries:trapped_present_white',
  'supplementaries:present_white',
  'supplementaries:awning_white',
  'refurbished_furniture:white_sofa',
  'refurbished_furniture:white_stool',
  'refurbished_furniture:white_lamp',
  'refurbished_furniture:white_kitchen_cabinetry',
  'refurbished_furniture:white_kitchen_drawer',
  'refurbished_furniture:white_kitchen_sink',
  'refurbished_furniture:white_kitchen_storage_cabinet',
  'refurbished_furniture:white_grill',
  'refurbished_furniture:white_cooler',
  'refurbished_furniture:white_trampoline',
  'refurbished_furniture:white_toilet',
  'refurbished_furniture:white_basin',
  'refurbished_furniture:white_bath',
  'createdeco:white_shipping_container',
  'vintagedelight:salt_lamp_white',
  'vintagedelight:white_chefs_hat',
  'bakeries:sofa_white',
  'create:white_seat',
  'createadditionallogistics:white_short_seat',
  'createadditionallogistics:white_tall_seat',
  'interiors:white_floor_chair',
  'interiors:white_chair',
  'interiors:white_cushion',
  'alexscaves:radon_lamp_orange',
  'create_dragons_plus:white_dye_bucket',
  'create:white_postbox',
];

// Pack-specific bucket ids. These use foldId because the exact EMI stack id is
// the most stable way to collect this custom bucket set.
//
// 整合包专用桶 id。这里使用 foldId，因为用精确 EMI stack id 收集这组自定义桶最稳定。
const CREATED_DELIGHT_BUCKETS = [
  'createdelightcore:fuel_mixtures_bucket',
  'createdelightcore:light_crude_oil_bucket',
  'createdelightcore:ethylene_fluid_bucket',
  'createdelightcore:spent_liquor_bucket',
  'createdelightcore:paper_pulp_bucket',
  'createdelightcore:unrefined_sugar_bucket',
  'createdelightcore:cryo_fuel_bucket',
  'createdelightcore:nut_milk_bucket',
  'createdelightcore:vinegar_bucket',
  'createdelightcore:radon_bucket',
  'createdelightcore:soya_milk_bucket',
  'createdelightcore:ancient_coffee_bucket',
  'createdelightcore:torchflower_tea_bucket',
  'createdelightcore:cherry_petal_tea_bucket',
  'createdelightcore:pitcher_plant_tea_bucket',
  'createdelightcore:fiddlehead_tea_bucket',
  'createdelightcore:scarlet_tea_bucket',
  'createdelightcore:lemon_black_tea_bucket',
  'createdelightcore:tea_mocha_bucket',
  'createdelightcore:saidi_tea_bucket',
  'createdelightcore:cornflower_tea_bucket',
  'createdelightcore:sakura_honey_tea_bucket',
  'createdelightcore:genmai_tea_bucket',
  'createdelightcore:green_water_bucket',
  'createdelightcore:white_tea_bucket',
  'createdelightcore:spring_soda_bucket',
  'createdelightcore:summer_cordial_bucket',
  'createdelightcore:autumn_tea_bucket',
  'createdelightcore:winter_glogg_bucket',
  'createdelightcore:apple_juice_bucket',
  'createdelightcore:mead_bucket',
  'createdelightcore:apple_cider_bucket',
  'createdelightcore:apple_wine_bucket',
  'createdelightcore:mellohi_wine_bucket',
  'createdelightcore:glowing_wine_bucket',
  'createdelightcore:solaris_wine_bucket',
  'createdelightcore:cherry_wine_bucket',
  'createdelightcore:creepers_crush_bucket',
  'createdelightcore:lilitu_wine_bucket',
  'createdelightcore:kelp_cider_bucket',
  'createdelightcore:eiswein_bucket',
  'createdelightcore:aegis_wine_bucket',
  'createdelightcore:chorus_wine_bucket',
  'createdelightcore:clark_wine_bucket',
  'createdelightcore:magnetic_wine_bucket',
  'createdelightcore:chenet_wine_bucket',
  'createdelightcore:nether_fizz_bucket',
  'createdelightcore:ice_lubricating_oil_bucket',
  'createdelightcore:cake_batter_bucket',
  'createdelightcore:sky_solution_bucket',
  'createdelightcore:jo_special_mixture_bucket',
  'createdelightcore:villagers_fright_bucket',
  'createdelightcore:jellie_wine_bucket',
  'createdelightcore:bottle_mojang_noir_bucket',
  'createdelightcore:netherite_nectar_bucket',
  'createdelightcore:lubricating_oil_bucket',
  'createdelightcore:egg_yolk_bucket',
  'createdelightcore:fire_dragon_blood_bucket',
  'createdelightcore:lightning_dragon_blood_bucket',
  'createdelightcore:unfermented_paper_pulp_bucket',
  'createdelightcore:yeast_bucket',
  'createdelightcore:noir_wine_bucket',
  'createdelightcore:red_wine_bucket',
  'createdelightcore:strad_wine_bucket',
  'createdelightcore:cristel_wine_bucket',
  'createdelightcore:bolvar_wine_bucket',
  'createdelightcore:stal_wine_bucket',
  'createdelightcore:blazewine_pinot_bucket',
  'createdelightcore:ghastly_grenache_bucket',
  'createdelightcore:lava_fizz_bucket',
  'createdelightcore:artificial_egg_yolk_bucket',
  'createdelightcore:ice_dragon_blood_bucket',
  'createdelightcore:malice_solution_bucket',
];

RecipeViewerEvents.fold((event) => {
  // Shared display options for every fold below.
  // 所有折叠组共用的显示参数。
  const options = {
    // Horizontal pixel distance between preview cards.
    // 预览卡片之间的横向像素偏移。
    spread: 4,

    // Uses the addon palette instead of a hard-coded ARGB color.
    // 使用插件内置调色板，而不是写死 ARGB 颜色。
    color: 'rainbow',
  };

  // Helper for KubeJS item ingredients: ids, tags, arrays, and component-aware
  // ingredient strings all work here.
  //
  // KubeJS 物品 ingredient helper：这里可以传物品 id、标签、数组，以及带组件的
  // ingredient 字符串。
  const fold = (key, filter) =>
    event.fold(groupId(key), groupName(key), filter, options);

  // Helper for exact EMI stack ids. Use this when the item ingredient matcher is
  // too broad or when you want to fold a fixed set of ids.
  //
  // 精确 EMI stack id helper。当物品 ingredient 匹配过宽，或你只想折叠固定 id 集合时使用。
  const foldIds = (key, paths) =>
    event.foldId(groupId(key), groupName(key), ids(paths), options);

  // Tag and item ingredient examples.
  // 标签和物品 ingredient 示例。
  fold('test', '#alexscaves:rock_candies');
  fold('glass_cables', '#ae2:glass_cable');
  fold('potions_drinkable', 'minecraft:potion');
  fold('potions_splash', 'minecraft:splash_potion');
  fold('potions_lingering', 'minecraft:lingering_potion');
  fold('suspicious_stews', 'minecraft:suspicious_stew');
  fold('goat_horns', 'minecraft:goat_horn');
  fold('paintings', 'minecraft:painting');
  fold('chests', '#c:chests');

  // Component-aware ingredient example. This folds only the tipped bamboo spikes
  // with the specified potion contents.
  //
  // 带组件的 ingredient 示例。这里只折叠带指定药水内容的淬毒竹刺。
  fold(
    'long_sundering_bamboo_spikes',
    'supplementaries:bamboo_spikes_tipped[potion_contents={potion:"apothic_attributes:long_sundering"}]'
  );

  fold('smart_cables', '#ae2:smart_cable');
  fold('smart_dense_cables', '#ae2:smart_dense_cable');
  fold('accessories_hats', '#accessories:hat');

  // Exact id examples. These match EmiStack#getId(), not just item ids.
  // 精确 id 示例。这里匹配的是 EmiStack#getId()，不只是物品 id。
  foldIds('enchanted_books', ['enchanted_book']);
  foldIds('seed_pouches', 'quark:seed_pouch');

  // Built-in helper API. This catches items implemented as SpawnEggItem even if
  // a mod uses unusual id naming.
  //
  // 插件内置 helper API。只要底层物品实现为 SpawnEggItem，即使模组 id 命名不规范也能折叠。
  event.foldSpawnEggs(groupId('spawn_eggs'), groupName('spawn_eggs'), options);

  // Whole-mod namespace folding. This accepts "citadel" or EMI-search-style
  // "@citadel"; the matcher compares against EmiStack#getId().getNamespace().
  //
  // 整个模组命名空间折叠。这里既可以写 "citadel"，也可以写 EMI 搜索风格的
  // "@citadel"；匹配时比较的是 EmiStack#getId().getNamespace()。
  event.foldMod(groupId('citadel'), groupName('citadel'), '@citadel', options);

  fold('arrows', '#minecraft:arrows');
  fold('card_display', '#lightmanscurrency:traders/card_display');
  fold('jelly_bean', 'alexscaves:jelly_bean');

  // Mods with suffix color variants.
  // 颜色写在末尾的模组变体。
  foldIds('freezers', colorSuffixIds('lightmanscurrency:freezer'));
  foldIds('vending_machines', colorSuffixIds('lightmanscurrency:vending_machine'));
  foldIds('large_vending_machines', colorSuffixIds('lightmanscurrency:vending_machine_large'));

  // A large pack-specific fixed id group.
  // 大型整合包专用固定 id 分组。
  foldIds('createdelightcore_buckets', CREATED_DELIGHT_BUCKETS);

  // Expand every sample color family into a fold group.
  // 把每个染色样本展开成一个折叠组。
  COLOR_FOLD_SAMPLES.forEach((sample) => foldIds(colorFamilyKey(sample), colorFamilyIds(sample)));
});
