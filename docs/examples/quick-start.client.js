RecipeViewerEvents.fold(event => {
  event.fold('my_pack:logs', 'Logs', '#minecraft:logs', {
    spread: 4,
    color: '#244E82'
  })
  event.unfold('my_pack:logs', 'minecraft:stripped_oak_log')
})
