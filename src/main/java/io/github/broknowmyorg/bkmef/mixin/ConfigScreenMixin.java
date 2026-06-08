package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.config.BooleanWidget;
import dev.emi.emi.screen.widget.config.ConfigEntryWidget;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.GroupNameWidget;
import dev.emi.emi.screen.widget.config.ListWidget;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.BkmefClientConfig;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ConfigScreen.class)
public class ConfigScreenMixin {
    @Shadow
    private ConfigSearch search;

    @Shadow
    public ListWidget list;

    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private void bkmef$addConfigEntries(CallbackInfo ci) {
        ConfigScreen screen = (ConfigScreen) (Object) this;
        Supplier<String> searchSupplier = () -> search.getSearch();

        GroupNameWidget group = new GroupNameWidget("broknowmyemifolder", EmiPort.translatable("config.emi.group.broknowmyemifolder"));
        list.addEntry(group);

        ConfigEntryWidget folding = new BooleanWidget(
            EmiPort.translatable("config.emi.broknowmyemifolder.folding_enabled"),
            tooltip("config.emi.tooltip.broknowmyemifolder.folding_enabled"),
            searchSupplier,
            screen.new Mutator<Boolean>() {
                @Override
                protected Boolean getValue() {
                    return BkmefClientConfig.isFoldingEnabled();
                }

                @Override
                protected void setValue(Boolean value) {
                    BkmefClientConfig.setFoldingEnabled(value);
                    EmiScreenManager.repopulatePanels(SidebarType.INDEX);
                    EmiSearch.update();
                }
            }
        );
        addChild(group, folding);

        ConfigEntryWidget reloadMessages = new BooleanWidget(
            EmiPort.translatable("config.emi.broknowmyemifolder.reload_messages_enabled"),
            tooltip("config.emi.tooltip.broknowmyemifolder.reload_messages_enabled"),
            searchSupplier,
            screen.new Mutator<Boolean>() {
                @Override
                protected Boolean getValue() {
                    return BkmefClientConfig.isReloadMessagesEnabled();
                }

                @Override
                protected void setValue(Boolean value) {
                    BkmefClientConfig.setReloadMessagesEnabled(value);
                }
            }
        );
        addChild(group, reloadMessages);
    }

    private void addChild(GroupNameWidget group, ConfigEntryWidget entry) {
        list.addEntry(entry);
        group.children.add(entry);
        entry.parentGroups.add(group);
    }

    private static List<ClientTooltipComponent> tooltip(String key) {
        return List.of(EmiTooltipComponents.of(Component.translatable(key)));
    }
}
