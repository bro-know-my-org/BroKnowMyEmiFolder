# KubeJS API

`RecipeViewerEvents.fold` defines collapsible EMI index groups. Fold groups are client-side UI abstractions; they do not add fake items.

```js
RecipeViewerEvents.fold(event => {
  event.fold('broknowmyemifolder:ores', 'translate:emi_group.broknowmyemifolder.ores', '#c:ores')
  event.unfold('broknowmyemifolder:ores', 'minecraft:raw_iron')

  event.foldId('broknowmyemifolder:boats', 'translate:emi_group.broknowmyemifolder.boats', [
    'minecraft:oak_boat',
    'minecraft:spruce_boat'
  ], {
    spread: 4,
    color: 'rainbow'
  })

  event.foldFluid('broknowmyemifolder:fluids', 'translate:emi_group.broknowmyemifolder.fluids', '#c:water')
  event.foldMod('broknowmyemifolder:citadel', 'translate:emi_group.broknowmyemifolder.citadel', '@citadel', {
    color: 'random'
  })
  event.foldSpawnEggs('broknowmyemifolder:spawn_eggs', 'translate:emi_group.broknowmyemifolder.spawn_eggs', {
    spread: 4,
    color: 'random'
  })
  event.unfoldAll('quark:seed_pouch')
})
```

## Methods

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

`id` is a stable group id, for example `'my_pack:ores'`.

`name` may be plain text, a text component, or a string prefixed with `translate:`.

`filter` for `fold` uses KubeJS item ingredient syntax, such as an item id, tag, or ingredient array.

`#tag` item ingredient syntax matches item tags. It does not automatically match the block tags of block items. Use the structured `blockTag` filter when you want to match the block represented by a `BlockItem`.

`filter` may also be a structured object. Keys in the same object are combined with AND:

```js
event.fold('my_pack:create_wooden_blocks', 'Create Wooden Blocks', {
  mod: 'create',
  blockTag: '#minecraft:mineable/axe'
})
```

Supported structured item filter keys:

```js
{
  all: ['#c:tools', { mod: 'create' }],     // every nested filter must match
  any: ['#c:ores', '#c:raw_materials'],     // at least one nested filter must match
  none: ['#c:tools/axes'],                  // no nested filter may match
  not: '#minecraft:arrows',                 // shorthand for none
  item: '#c:tools',                         // KubeJS item ingredient
  ingredient: 'minecraft:shears',           // alias of item
  id: 'minecraft:potion',                   // exact EMI stack id
  ids: ['minecraft:potion'],
  mod: 'create',                            // EMI stack id namespace
  mods: ['create', '@citadel'],
  tag: '#c:tools',                          // item tag
  tags: ['#c:tools', '#minecraft:arrows'],
  block: 'minecraft:oak_log',               // represented block id for BlockItem
  blocks: ['minecraft:oak_log'],
  blockTag: '#minecraft:logs',              // represented block tag for BlockItem
  blockTags: ['#minecraft:logs'],
  spawnEggs: true
}
```

Examples:

```js
event.fold('my_pack:tools_without_axes', 'Tools Without Axes', {
  tag: '#c:tools',
  none: '#c:tools/axes'
})

event.fold('my_pack:tag_intersection', 'Tag Intersection', {
  all: ['#c:tools', '#minecraft:breaks_decorated_pots']
})

event.fold('my_pack:wooden_block_items', 'Wooden Block Items', {
  blockTag: '#minecraft:logs'
})
```

`filter` for `foldFluid` uses KubeJS fluid ingredient syntax.

`ids` for `foldId` is a single id or an array of ids matched against `EmiStack#getId()`.

`foldMod` matches item entries by the namespace of `EmiStack#getId()`. It accepts a single mod id, an EMI-search-style string with a leading `@`, or an array, for example `'citadel'`, `'@citadel'`, or `['@citadel', 'alexsmobs']`.

`foldSpawnEggs` matches item entries whose item class is Minecraft `SpawnEggItem`. This does not depend on item id naming, so modded spawn eggs with non-standard ids can still be folded. It does not match custom spawn-like items that are not implemented as `SpawnEggItem`; use `fold` or `foldId` for those.

`unfold` prevents matching item entries from being folded by one specific group.

`unfoldFluid` prevents matching fluid entries from being folded by one specific group.

`unfoldId` prevents exact EMI stack ids from being folded by one specific group.

`unfoldAll`, `unfoldAllFluid`, and `unfoldAllId` prevent matching entries from being folded by any group.

`unfold` rules are order-independent. They may be written before or after the `fold` call for the same group.

`foldId` and `unfoldId` match `EmiStack#getId()`, not item stack components. For item variants with the same id but different data components, such as filled seed pouches, this matches every variant with that item id.

## Performance Notes

BKMEF intentionally does not provide regular-expression filters. Prefer deterministic filters such as `id`, `ids`, `mod`, `mods`, `tag`, `block`, and `blockTag`; they are cheaper, easier to read, and harder to accidentally make pathological.

Structured filters are evaluated with cheap checks first and expensive nested or ingredient checks later. Fold definitions are also cached after EMI's index is folded, so normal sidebar rendering does not rescan every entry every frame.

BKMEF writes fold rebuild diagnostics to the log:

- KubeJS fold reload logs the loaded group count and reload time.
- More than 100 fold groups logs a warning because broad or deeply nested filters can slow index rebuilds.
- EMI index fold rebuilds log source entry count, folded entry count, matched entry count, membership count, and elapsed time at debug level.
- Slow index fold rebuilds log a warning.

## Options

`options` is optional.

```js
{
  spread: 4,
  color: 'rainbow'
}
```

`spread` controls the horizontal pixel offset between preview cards. The default is `4`.

`color` controls the folded card background. It accepts a number, a hex string such as `'#244E82'`, `'rainbow'` for sequential palette assignment, or `'random'` for a stable id-based palette color.

## Behavior

Left-click a collapsed group to expand it. Alt-left-click an expanded member to collapse its group.

Entries may belong to multiple fold groups. In the normal EMI index, each matching group may contain the same entry. Search results show a matching entry once and list all owning fold groups in the tooltip.

KubeJS `RecipeViewerEvents.groupEntries` definitions are also consumed as fold groups where practical.

## Full Example

See [`docs/examples/emi_folder.client.js`](examples/emi_folder.client.js) for a heavily commented bilingual client script based on a real modpack setup.
