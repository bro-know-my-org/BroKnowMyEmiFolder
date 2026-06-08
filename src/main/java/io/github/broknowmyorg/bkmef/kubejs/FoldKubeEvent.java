package io.github.broknowmyorg.bkmef.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.Context;

@Info("""
Defines collapsible EMI index groups. Groups are client-side UI abstractions; entries may belong to more than one group.
Options are optional. Supported options: { spread?: number, color?: number | string }, where spread is the horizontal pixel offset between preview cards and color controls the folded card background.
""")
public interface FoldKubeEvent extends KubeEvent {
    @Info(value = "Defines an item fold group with an automatic id generated from the name.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS item ingredient, item id, item tag, ingredient array, predicate, or structured filter object.")
        })
    void fold(Context cx, Object name, Object filter);

    @Info(value = "Defines an item fold group with optional display settings.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS item ingredient, item id, item tag, ingredient array, predicate, or structured filter object."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string, id?: string }.")
        })
    void fold(Context cx, Object name, Object filter, Object options);

    @Info(value = "Defines an item fold group with an explicit stable group id.",
        params = {
            @Param(name = "id", value = "Stable group id, for example 'modid:ores'."),
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS item ingredient, item id, item tag, ingredient array, predicate, or structured filter object."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string }.")
        })
    void fold(Context cx, Object id, Object name, Object filter, Object options);

    @Info(value = "Defines a fluid fold group with an automatic id generated from the name.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS fluid ingredient, fluid id, or tag.")
        })
    void foldFluid(Context cx, Object name, Object filter);

    @Info(value = "Defines a fluid fold group with optional display settings.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS fluid ingredient, fluid id, or tag."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string, id?: string }.")
        })
    void foldFluid(Context cx, Object name, Object filter, Object options);

    @Info(value = "Defines a fluid fold group with an explicit stable group id.",
        params = {
            @Param(name = "id", value = "Stable group id, for example 'modid:fluids'."),
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "filter", value = "KubeJS fluid ingredient, fluid id, or tag."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string }.")
        })
    void foldFluid(Context cx, Object id, Object name, Object filter, Object options);

    @Info(value = "Defines a fold group by exact EMI stack ids with an automatic id generated from the name.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "ids", value = "Single id or array of ids matched against EmiStack#getId().")
        })
    void foldId(Context cx, Object name, Object ids);

    @Info(value = "Defines a fold group by exact EMI stack ids with optional display settings.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "ids", value = "Single id or array of ids matched against EmiStack#getId()."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string, id?: string }.")
        })
    void foldId(Context cx, Object name, Object ids, Object options);

    @Info(value = "Defines a fold group by exact EMI stack ids with an explicit stable group id.",
        params = {
            @Param(name = "id", value = "Stable group id, for example 'modid:boats'."),
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "ids", value = "Single id or array of ids matched against EmiStack#getId()."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string }.")
        })
    void foldId(Context cx, Object id, Object name, Object ids, Object options);

    @Info(value = "Defines an item fold group for every EMI stack whose id namespace matches a mod id. A leading @ is accepted, for example '@citadel'.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "mods", value = "Single mod id, @modid search-style string, or array of mod ids.")
        })
    void foldMod(Context cx, Object name, Object mods);

    @Info(value = "Defines a mod namespace fold group with optional display settings.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "mods", value = "Single mod id, @modid search-style string, or array of mod ids."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string, id?: string }.")
        })
    void foldMod(Context cx, Object name, Object mods, Object options);

    @Info(value = "Defines a mod namespace fold group with an explicit stable group id.",
        params = {
            @Param(name = "id", value = "Stable group id, for example 'modid:hidden_citadel'."),
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "mods", value = "Single mod id, @modid search-style string, or array of mod ids."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string }.")
        })
    void foldMod(Context cx, Object id, Object name, Object mods, Object options);

    @Info(value = "Defines an item fold group for all items implemented as Minecraft SpawnEggItem, independent of item id naming.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:.")
        })
    void foldSpawnEggs(Context cx, Object name);

    @Info(value = "Defines a SpawnEggItem fold group with optional display settings.",
        params = {
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string, id?: string }.")
        })
    void foldSpawnEggs(Context cx, Object name, Object options);

    @Info(value = "Defines a SpawnEggItem fold group with an explicit stable group id.",
        params = {
            @Param(name = "id", value = "Stable group id, for example 'modid:spawn_eggs'."),
            @Param(name = "name", value = "Literal text, Component, or a string prefixed with translate:."),
            @Param(name = "options", value = "Optional settings. Supports { spread?: number, color?: number | string }.")
        })
    void foldSpawnEggs(Context cx, Object id, Object name, Object options);

    @Info(value = "Prevents matching item entries from being folded by the specified fold group.",
        params = {
            @Param(name = "groupId", value = "Stable fold group id, for example 'modid:tools'."),
            @Param(name = "filter", value = "KubeJS item ingredient, item id, item tag, ingredient array, predicate, or structured filter object.")
        })
    void unfold(Context cx, Object groupId, Object filter);

    @Info(value = "Prevents matching fluid entries from being folded by the specified fold group.",
        params = {
            @Param(name = "groupId", value = "Stable fold group id, for example 'modid:fluids'."),
            @Param(name = "filter", value = "KubeJS fluid ingredient, fluid id, or tag.")
        })
    void unfoldFluid(Context cx, Object groupId, Object filter);

    @Info(value = "Prevents exact EMI stack ids from being folded by the specified fold group.",
        params = {
            @Param(name = "groupId", value = "Stable fold group id, for example 'modid:books'."),
            @Param(name = "ids", value = "Single id or array of ids matched against EmiStack#getId().")
        })
    void unfoldId(Context cx, Object groupId, Object ids);

    @Info(value = "Prevents matching item entries from being folded by any fold group.",
        params = {
            @Param(name = "filter", value = "KubeJS item ingredient, item id, item tag, ingredient array, predicate, or structured filter object.")
        })
    void unfoldAll(Context cx, Object filter);

    @Info(value = "Prevents matching fluid entries from being folded by any fold group.",
        params = {
            @Param(name = "filter", value = "KubeJS fluid ingredient, fluid id, or tag.")
        })
    void unfoldAllFluid(Context cx, Object filter);

    @Info(value = "Prevents exact EMI stack ids from being folded by any fold group.",
        params = {
            @Param(name = "ids", value = "Single id or array of ids matched against EmiStack#getId().")
        })
    void unfoldAllId(Context cx, Object ids);
}
