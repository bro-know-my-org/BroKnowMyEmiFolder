package io.github.broknowmyorg.bkmef.kubejs;

import dev.latvian.mods.kubejs.recipe.viewer.server.RemoteRecipeViewerDataUpdatedEvent;
import io.github.broknowmyorg.bkmef.Broknowmyemifolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Broknowmyemifolder.MODID, value = Dist.CLIENT)
public class KubeRemoteDataEvents {
    @SubscribeEvent
    public static void remoteRecipeViewerDataUpdated(RemoteRecipeViewerDataUpdatedEvent event) {
        KubeFoldRegistrar.rebuildFromKubeAndRefresh();
    }
}
