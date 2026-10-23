# Getting Started

BKMEF adds collapsible groups to EMI's index sidebar on Minecraft 1.21.1 / NeoForge.

## Installation

Install Java 21, Minecraft 1.21.1, and NeoForge 21.1.228 or a compatible version. Add EMI 1.1.22+, KubeJS 2101.7.2+, and their dependencies to your instance's `mods` directory, then install [BKMEF](https://github.com/bro-know-my-org/BroKnowMyEmiFolder/releases).

## Create Your First Group

Create `emi_folder.js` in your instance's `kubejs/client_scripts/` directory:

<<< @/examples/quick-start.client.js

When you enter the game, the EMI index groups matching logs under "Logs", while stripped oak logs remain separate. This is a client-side UI script; install it on each client that should display the groups.

Left-click a collapsed group to expand it. Alt-left-click an expanded member to collapse its group. You can also toggle index folding and reload messages in the `BKMEF` section of EMI's settings.

Read the [KubeJS guide](./kubejs-api.md) for structured filters, fluid groups, and display options.
