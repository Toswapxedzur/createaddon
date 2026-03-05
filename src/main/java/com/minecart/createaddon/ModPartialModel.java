package com.minecart.createaddon;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class ModPartialModel {
    public static final PartialModel QUATERED_SHAFT = block("kinetic_sculk_sensor/shaft_quartered");

    public static final PartialModel BIG_PRESS_HEAD = block("big_mechanical_press/head");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateAddon.modLoc("block/" + path));
    }

    public static void register(){

    }
}
