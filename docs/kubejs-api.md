# KubeJS API

`RecipeViewerEvents.fold` defines collapsible EMI index groups. Fold groups are client-side UI abstractions; they do not add fake items.

```js
RecipeViewerEvents.fold(event => {
  event.fold('broknowmyemifolder:ores', 'translate:emi_group.broknowmyemifolder.ores', '#c:ores')

  event.foldId('broknowmyemifolder:boats', 'translate:emi_group.broknowmyemifolder.boats', [
    'minecraft:oak_boat',
    'minecraft:spruce_boat'
  ], {
    spread: 4,
    color: 'rainbow'
  })

  event.foldFluid('broknowmyemifolder:fluids', 'translate:emi_group.broknowmyemifolder.fluids', '#c:water')
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
```

`id` is a stable group id, for example `'my_pack:ores'`.

`name` may be plain text, a text component, or a string prefixed with `translate:`.

`filter` for `fold` uses KubeJS item ingredient syntax, such as an item id, tag, or ingredient array.

`filter` for `foldFluid` uses KubeJS fluid ingredient syntax.

`ids` for `foldId` is a single id or an array of ids matched against `EmiStack#getId()`.

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
