package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import io.github.broknowmyorg.bkmef.kubejs.KubeFoldRegistrar;

@EmiEntrypoint
public class BkmefEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        KubeFoldRegistrar.rebuildFromKube();
    }
}
