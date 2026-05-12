package com.minecart.createaddon;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class ModPartialModel {
    public static final PartialModel QUATERED_SHAFT = block("kinetic_sculk_sensor/shaft_quartered");

    public static final PartialModel BIG_PRESS_HEAD = block("big_mechanical_press/head");

    public static final PartialModel MECHANICAL_SIEVE_SHAFT = block("mechanical_sieve/shaft");
    public static final PartialModel MECHANICAL_SIEVE_NET = block("mechanical_sieve/sieve");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateAddon.modLoc("block/" + path));
    }

    public static void register() {
    }
}
