package com.minecart.createaddon.block_entities.bigPress;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CompressingBehaviour extends BeltProcessingBehaviour {
    public static final int BREAKTIME = 320;

    public Phase phase = Phase.IDLE;
    public int runningTicks;
    public int prevRunningTicks;
    public int currentRecipeDuration;
    public boolean running;
    public int suspendTicks;

    public Mode mode = Mode.BELT;
    public int scanCooldown;
    public ItemEntity targetEntity;
    public static final int SCAN_RATE = 10;
    public UUID targetUUID;

    public List<ItemStack> particleItems = new ArrayList<>();
    public CompressingBehaviourSpecifics specifics;

    public interface CompressingBehaviourSpecifics {
        boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate);

        boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate);

        int getProcessingTime(ItemStack input);

        boolean canProcessInBulk();

        void onPressingCompleted();

        int getParticleAmount();

        float getKineticSpeed();
    }

    public enum Phase {
        IDLE, EXTEND, COMPRESS, CONTRACT
    }

    public enum Mode {
        WORLD(10f / 16f), BELT(13f / 16f);

        public float headOffset;

        Mode(float headOffset) {
            this.headOffset = headOffset;
        }
    }


    public <T extends SmartBlockEntity & CompressingBehaviourSpecifics> CompressingBehaviour(T be) {
        super(be);
        this.specifics = be;
        runningTicks = 0;
        suspendTicks = 0;
        prevRunningTicks = 0;
        whenItemEnters((s, i) -> onItemReceived(s, i, this));
        whileItemHeld((s, i) -> whenItemHeld(s, i, this));
    }

    private static ProcessingResult onItemReceived(TransportedItemStack transported,
                                                   TransportedItemStackHandlerBehaviour handler, CompressingBehaviour behaviour) {
        float currentSpeed = behaviour.specifics.getKineticSpeed();

        if (currentSpeed == 0 || behaviour.running)
            return ProcessingResult.PASS;

        if (!behaviour.specifics.tryProcessOnBelt(transported, null, true))
            return ProcessingResult.PASS;

        int time = behaviour.specifics.getProcessingTime(transported.stack);
        if (time <= 0)
            return ProcessingResult.PASS;

        behaviour.start(Mode.BELT, time);
        behaviour.particleItems.add(transported.stack.copy());
        return ProcessingResult.HOLD;
    }

    private static ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
                                                 CompressingBehaviour behaviour) {
        if (behaviour.running && behaviour.mode == Mode.BELT) {
            if (!behaviour.blockEntity.isVirtual() && !behaviour.blockEntity.getLevel().isClientSide())
                behaviour.suspendTicks = 0;

            if (behaviour.phase == Phase.COMPRESS && behaviour.runningTicks >= behaviour.currentRecipeDuration) {

                ArrayList<ItemStack> results = new ArrayList<>();
                behaviour.specifics.tryProcessOnBelt(transported, results, false);

                boolean bulk = behaviour.specifics.canProcessInBulk() || transported.stack.getCount() == 1;
                transported.clearFanProcessingData();

                List<TransportedItemStack> collect = results.stream()
                        .map(stack -> {
                            TransportedItemStack copy = transported.copy();
                            boolean centered = BeltHelper.isItemUpright(stack);
                            copy.stack = stack;
                            copy.locked = true;
                            copy.angle = centered ? 180 : Create.RANDOM.nextInt(360);
                            return copy;
                        })
                        .collect(Collectors.toList());

                if (bulk) {
                    if (collect.isEmpty())
                        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
                    else
                        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(collect));
                } else {
                    TransportedItemStack left = transported.copy();
                    left.stack.shrink(1);
                    if (collect.isEmpty())
                        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(left));
                    else
                        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(collect, left));
                }

                behaviour.setPhase(Phase.CONTRACT);
            }
            return ProcessingResult.HOLD;
        }
        return ProcessingResult.PASS;
    }

    public void start(Mode mode, int recipeDuration) {
        this.mode = mode;
        this.currentRecipeDuration = recipeDuration;
        this.running = true;
        this.particleItems.clear();
        this.suspendTicks = 0;
        setPhase(Phase.EXTEND);
    }

    public void setPhase(Phase newPhase) {
        this.phase = newPhase;
        this.runningTicks = 0;
        this.suspendTicks = 0;
        this.prevRunningTicks = 0;
        blockEntity.sendData();
    }

    @Override
    public void tick() {
        super.tick();

        Level level = getWorld();
        BlockPos worldPosition = getPos();
        if (level == null) return;

        if (!level.isClientSide && mode == Mode.WORLD && targetEntity == null && targetUUID != null) {
            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(targetUUID);
                if (entity instanceof ItemEntity item) {
                    targetEntity = item;
                } else {
                    setPhase(Phase.CONTRACT);
                    targetUUID = null;
                }
            }
        }

        prevRunningTicks = runningTicks;

        if (!running || phase == Phase.IDLE) {
            runningTicks = 0;
            if (!level.isClientSide) {
                if (scanCooldown-- <= 0) {
                    scanCooldown = SCAN_RATE;
                    scanForWorldItems(level);
                }
            }
            return;
        }

        float speed = specifics.getKineticSpeed();
        if (speed == 0) return;

        int movementSpeed = (int) Mth.lerp(Mth.clamp(Math.abs(speed) / 512f, 0, 1), 1, 60);

        runningTicks += movementSpeed;
        switch (phase) {
            case EXTEND:
                if (runningTicks >= BREAKTIME / 2) {
                    setPhase(Phase.COMPRESS);
                    if (level.getBlockState(worldPosition.below(1)).getSoundType() == SoundType.WOOL)
                        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION_ON_BELT.playOnServer(level, worldPosition);
                    else
                        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, worldPosition, 1f,
                                .625f + (Math.abs(specifics.getKineticSpeed()) / 1024f));
                    spawnParticles();
                }
                break;
            case COMPRESS:
                if (!level.isClientSide) {
                    if (mode == Mode.BELT) {
                        suspendTicks++;
                        if (suspendTicks > 20) setPhase(Phase.CONTRACT);
                    } else if (mode == Mode.WORLD) {
                        if (targetEntity == null || !targetEntity.isAlive() || !targetEntity.onGround()) {
                            setPhase(Phase.CONTRACT);
                        } else if (runningTicks >= currentRecipeDuration) {
                            specifics.tryProcessInWorld(targetEntity, false);
                            setPhase(Phase.CONTRACT);
                        }
                    }
                }
                break;
            case CONTRACT:
                if (runningTicks >= BREAKTIME / 2) {
                    running = false;
                    particleItems.clear();
                    setPhase(Phase.IDLE);
                    specifics.onPressingCompleted();
                    targetEntity = null;
                }
                break;
        }
    }

    private void scanForWorldItems(Level level) {
        BlockPos pos = getPos().below(1);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).deflate(0.025));

        for (ItemEntity item : items) {
            if (!item.isAlive() || !item.onGround()) continue;

            if (specifics.tryProcessInWorld(item, true)) {
                int duration = specifics.getProcessingTime(item.getItem());
                if (duration > 0) {
                    this.targetEntity = item;
                    this.particleItems.clear();
                    this.particleItems.add(item.getItem().copy());
                    start(Mode.WORLD, duration);
                    return;
                }
            }
        }
    }

    public float getRenderedHeadOffset(float partialTicks) {
        if (!running || phase == Phase.IDLE) return 0f;
        if (phase == Phase.COMPRESS) return 1f;

        float currentTick = Mth.lerp(partialTicks, prevRunningTicks, runningTicks);
        float target = BREAKTIME / 2f;

        if (phase == Phase.EXTEND) {
            float progress = Mth.clamp(currentTick / target, 0, 1);
            return (float) Math.pow(progress, 3);
        } else if (phase == Phase.CONTRACT) {
            float progress = Mth.clamp(currentTick / target, 0, 1);
            return 1f - progress;
        }
        return 0f;
    }

    public float getExtensionLength(float partialTicks) {
        return mode.headOffset;
    }

    protected void spawnParticles() {
        if (particleItems.isEmpty()) return;

        BlockPos pos = getPos();
        Vec3 vec = VecHelper.getCenterOf(pos).add(0, -1.5f, 0);

        for (ItemStack stack : particleItems) {
            ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, stack);
            for (int i = 0; i < specifics.getParticleAmount(); i++) {
                Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, getWorld().random, .25f);
                getWorld().addParticle(data, vec.x, vec.y, vec.z, m.x, m.y, m.z);
            }
        }
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        nbt.putInt("Mode", mode.ordinal());
        nbt.putInt("Phase", phase.ordinal());
        nbt.putInt("RunningTicks", runningTicks);
        nbt.putInt("suspendTicks", suspendTicks);
        nbt.putInt("RecipeDuration", currentRecipeDuration);
        nbt.putBoolean("Running", running);

        if (clientPacket) {
            // 1.20.1: ItemStack#save(CompoundTag) returns the tag.
            nbt.put("ParticleItems", NBTHelper.writeCompoundList(particleItems, s -> s.save(new CompoundTag())));
            particleItems.clear();
        }

        if (targetEntity != null) {
            nbt.putUUID("TargetUUID", targetEntity.getUUID());
        } else if (targetUUID != null) {
            nbt.putUUID("TargetUUID", targetUUID);
        }
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        mode = Mode.values()[nbt.getInt("Mode")];
        phase = Phase.values()[nbt.getInt("Phase")];
        runningTicks = nbt.getInt("RunningTicks");
        suspendTicks = nbt.getInt("suspendTicks");
        prevRunningTicks = runningTicks;
        currentRecipeDuration = nbt.getInt("RecipeDuration");
        running = nbt.getBoolean("Running");
        if (clientPacket) {
            NBTHelper.iterateCompoundList(nbt.getList("ParticleItems", Tag.TAG_COMPOUND),
                    c -> particleItems.add(ItemStack.of(c)));
        }

        if (nbt.hasUUID("TargetUUID")) {
            targetUUID = nbt.getUUID("TargetUUID");
        }
    }
}
