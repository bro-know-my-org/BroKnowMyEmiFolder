package io.github.broknowmyorg.bkmef;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Broknowmyemifolder.MODID)
public class Broknowmyemifolder {
    public static final String MODID = "broknowmyemifolder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Broknowmyemifolder() {
        BkmefClientConfig.load();
    }
}
