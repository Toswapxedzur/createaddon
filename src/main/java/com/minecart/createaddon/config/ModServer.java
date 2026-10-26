package com.minecart.createaddon.config;

import net.createmod.catnip.config.ConfigBase;

public class ModServer extends ConfigBase {
    @Override
    public String getName() {
        return "server";
    }

    public ModStress stress = nested(0, ModStress::new, "stress");
}
