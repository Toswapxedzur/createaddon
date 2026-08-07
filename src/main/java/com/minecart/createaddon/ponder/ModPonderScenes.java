package com.minecart.createaddon.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * 1.20.1 port: identical to the 1.21.1 source — the Ponder scene API
 * ({@link CreateSceneBuilder}, {@link SceneBuilder}, {@link SceneBuildingUtil}) is
 * unchanged between Ponder 1.0.91 (1.20.1) and the 1.21.1 release.
 */
public class ModPonderScenes {

    public static void encasedNoteblock(SceneBuilder sceneBuilder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);

        scene.title("noteblock_encased_cogwheel", "The Noteblock Encased Cogwheel");
        scene.configureBasePlate(0, 1, 5);
        scene.showBasePlate();
        scene.idle(5);

        BlockPos bottomCog = util.grid().at(1, 0, 0);
        BlockPos upCog = util.grid().at(1, 1, 0);

        BlockPos connCogStd = util.grid().at(1, 1, 1);
        BlockPos noteCog1 = util.grid().at(1, 1, 2);
        BlockPos noteCog2 = util.grid().at(1, 1, 3);

        BlockPos connCogGear = util.grid().at(1, 1, 4);
        BlockPos shaftG1 = util.grid().at(2, 1, 4);
        BlockPos shaftG2 = util.grid().at(3, 1, 4);
        BlockPos largeCogG = util.grid().at(4, 1, 4);
        BlockPos smallCogG = util.grid().at(4, 2, 3);
        BlockPos noteShaftG = util.grid().at(3, 2, 3);

        BlockPos shaftCtrl = util.grid().at(2, 1, 2);
        BlockPos controller = util.grid().at(3, 1, 2);
        BlockPos largeCogCtrl = util.grid().at(3, 2, 2);
        BlockPos noteShaftCtrl = util.grid().at(3, 2, 1);

        Selection powerSource = util.select().position(bottomCog).add(util.select().position(upCog));

        Selection groupStandard = util.select().position(connCogStd)
                .add(util.select().position(noteCog1))
                .add(util.select().position(noteCog2));

        Selection groupGearShift = util.select().position(connCogGear)
                .add(util.select().position(shaftG1))
                .add(util.select().position(shaftG2))
                .add(util.select().position(largeCogG))
                .add(util.select().position(smallCogG))
                .add(util.select().position(noteShaftG));

        Selection groupController = util.select().position(shaftCtrl)
                .add(util.select().position(controller))
                .add(util.select().position(largeCogCtrl))
                .add(util.select().position(noteShaftCtrl));

        ParticleEmitter noteParticle = scene.effects().simpleParticleEmitter(ParticleTypes.NOTE, new Vec3(30, 30, 30));

        scene.world().setKineticSpeed(util.select().everywhere(), 32);

        scene.world().setKineticSpeed(util.select().position(smallCogG), -64);
        scene.world().setKineticSpeed(util.select().position(noteShaftG), -64);

        scene.world().setKineticSpeed(util.select().position(largeCogCtrl), 128);
        scene.world().setKineticSpeed(util.select().position(noteShaftCtrl), -128);

        scene.world().showSection(powerSource, Direction.UP);
        scene.idle(5);

        scene.world().showSection(util.select().position(connCogStd), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(noteCog1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(noteCog2), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("When powered, it will emit a note beat 4 times per second.")
                .pointAt(util.vector().topOf(noteCog1))
                .placeNearTarget();

        Vec3 loc1 = util.vector().topOf(noteCog1);
        Vec3 loc2 = util.vector().topOf(noteCog2);

        for (int i = 0; i < 4; i++) {
            scene.effects().emitParticles(loc1, noteParticle, 1.0f, 1);
            scene.effects().emitParticles(loc2, noteParticle, 1.0f, 1);
            scene.idle(5);
        }

        scene.idle(20);

        scene.addKeyframe();

        scene.world().showSection(util.select().position(connCogGear), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(shaftG1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(shaftG2), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(largeCogG), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(smallCogG), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(noteShaftG), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The amplitude of the beats is fixed, but the pitch depends on rotation speed.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(noteShaftG))
                .placeNearTarget();

        Vec3 locG = util.vector().topOf(noteShaftG);
        for (int i = 0; i < 6; i++) {
            scene.effects().emitParticles(locG, noteParticle, 1.0f, 1);
            scene.idle(4);
        }

        scene.idle(20);

        scene.addKeyframe();

        scene.world().showSection(util.select().position(shaftCtrl), Direction.NORTH);
        scene.idle(3);
        scene.world().showSection(util.select().position(controller), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(largeCogCtrl), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(noteShaftCtrl), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Higher speed gives you higher pitches.")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(noteShaftCtrl))
                .placeNearTarget();

        Vec3 locCtrl = util.vector().topOf(noteShaftCtrl);
        for (int i = 0; i < 10; i++) {
            scene.effects().emitParticles(locCtrl, noteParticle, 1.0f, 1);
            scene.idle(2);
        }

        scene.idle(20);
        scene.world().setKineticSpeed(util.select().everywhere(), 0);
    }
}
