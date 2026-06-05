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
  event.foldSpawnEggs('broknowmyemifolder:spawn_eggs', 'Spawn Eggs', {
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

`filter` for `foldFluid` uses KubeJS fluid ingredient syntax.

`ids` for `foldId` is a single id or an array of ids matched against `EmiStack#getId()`.

`foldSpawnEggs` matches item entries whose item class is Minecraft `SpawnEggItem`. This does not depend on item id naming, so modded spawn eggs with non-standard ids can still be folded. It does not match custom spawn-like items that are not implemented as `SpawnEggItem`; use `fold` or `foldId` for those.

`unfold` prevents matching item entries from being folded by one specific group.

`unfoldFluid` prevents matching fluid entries from being folded by one specific group.

`unfoldId` prevents exact EMI stack ids from being folded by one specific group.

`unfoldAll`, `unfoldAllFluid`, and `unfoldAllId` prevent matching entries from being folded by any group.

`unfold` rules are order-independent. They may be written before or after the `fold` call for the same group.

`foldId` and `unfoldId` match `EmiStack#getId()`, not item stack components. For item variants with the same id but different data components, such as filled seed pouches, this matches every variant with that item id.

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
