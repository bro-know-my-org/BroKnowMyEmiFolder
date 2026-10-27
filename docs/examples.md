# Examples

Place this script in your instance's `kubejs/client_scripts/` directory to group logs and keep stripped oak logs separate:

<<< @/examples/quick-start.client.js

For a larger script based on a real modpack, see [the example on GitHub](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/HEAD/docs/examples/emi_folder.client.js). Adjust item IDs, tags, and namespaces to match the mods installed in your pack.

## README showcase

The README screenshots use only vanilla items, with different colors and card spacing, an exclusion for stripped oak logs, and a diamond shared between two groups.

1. Copy [showcase.client.js](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/HEAD/docs/examples/showcase.client.js) into `kubejs/client_scripts/`.
2. Copy the [bkmef_showcase asset directory](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/tree/HEAD/docs/examples/showcase-assets/bkmef_showcase) into `kubejs/assets/` for English and Simplified Chinese folder names.
3. Launch the game, open an inventory, and clear EMI's search field to see the folded groups. Left-click a folder to expand it; Alt + left-click a member to collapse it.
4. Search for `diamond` (or `钻石` in Simplified Chinese) to see overlapping membership without duplicate results. Switch the game language to compare the translations.

These are demonstration groups; they are not installed automatically with BKMEF.
