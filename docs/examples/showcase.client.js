// README screenshot setup: vanilla items only; copy into kubejs/client_scripts/.
// Copy showcase-assets/bkmef_showcase into kubejs/assets/ for EN / 中文 names.
RecipeViewerEvents.fold(event => {
  const name = key => `translate:bkmef_showcase.${key}`
  const colors = [
    'white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
    'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'
  ]

  event.fold('showcase:planks', name('planks'), '#minecraft:planks', {
    spread: 4, color: '#E9B872'
  })
  event.fold('showcase:logs', name('logs'), '#minecraft:logs', {
    spread: 2, color: '#68B984'
  })
  // Keep this building material visible outside the log folder.
  event.unfold('showcase:logs', 'minecraft:stripped_oak_log')

  event.foldId('showcase:stone', name('stone'), [
    'minecraft:stone', 'minecraft:cobblestone', 'minecraft:mossy_cobblestone',
    'minecraft:granite', 'minecraft:polished_granite', 'minecraft:diorite',
    'minecraft:polished_diorite', 'minecraft:andesite', 'minecraft:polished_andesite'
  ], { spread: 5, color: '#A7B6C8' })

  event.foldId('showcase:wool', name('wool'),
    colors.map(color => `minecraft:${color}_wool`),
    { spread: 4, color: '#E683B6' })
  event.foldId('showcase:concrete', name('concrete'),
    colors.map(color => `minecraft:${color}_concrete`),
    { spread: 7, color: 'rainbow' })
  event.foldId('showcase:glass', name('glass'),
    ['minecraft:glass'].concat(colors.map(color => `minecraft:${color}_stained_glass`)),
    { spread: 3, color: '#71B7DE' })

  event.foldId('showcase:redstone', name('redstone'), [
    'minecraft:redstone', 'minecraft:repeater', 'minecraft:comparator',
    'minecraft:observer', 'minecraft:piston', 'minecraft:sticky_piston',
    'minecraft:dispenser', 'minecraft:dropper', 'minecraft:hopper'
  ], { spread: 4, color: '#EB716B' })
  event.foldId('showcase:valuables', name('valuables'), [
    'minecraft:diamond', 'minecraft:emerald', 'minecraft:amethyst_shard',
    'minecraft:iron_ingot', 'minecraft:gold_ingot', 'minecraft:copper_ingot'
  ], { spread: 6, color: '#A78BDA' })

  // Intentional overlap: diamond belongs to both folders. Search shows it once.
  event.foldId('showcase:travel', name('travel'), [
    'minecraft:diamond', 'minecraft:compass', 'minecraft:clock',
    'minecraft:spyglass', 'minecraft:map', 'minecraft:elytra'
  ], { spread: 4, color: '#52BFC0' })
  event.foldSpawnEggs('showcase:spawn_eggs', name('spawn_eggs'), {
    spread: 2, color: '#91AE67'
  })
})
