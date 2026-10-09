package com.minecart.createaddon.item;

import com.minecart.createaddon.ModDataComponents;
import com.minecart.createaddon.client.renderer.LabwareWithoutLevelRenderer;
import com.minecart.createaddon.fluid.LabwareFluidContents;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Labware uses a directed exchange with block fluid handlers: compares the block's total fluid amount to this stack's
 * {@link LabwareFluidContents#targetMb()}. If the block holds more, fluid is moved from the block into the labware;
 * if the block holds less, excess fluid is moved from the labware into the block. Infinite overworld water sources
 * are handled by {@link #tryScoopInfiniteFromWorld}.
 */
public class LabwareBlockItem extends BlockItem {
    public LabwareBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new LabwareWithoutLevelRenderer()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            if (tryDirectedLabwareFluidExchange(player, context.getHand(), level, context.getClickedPos(), context.getClickedFace())) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (tryScoopInfiniteFromWorld(player, context.getHand(), level, context.getClickedPos(), context.getClickedFace())) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK
                && tryScoopInfiniteFromWorld(player, hand, level, hit.getBlockPos(), hit.getDirection())) {
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
        return super.use(level, player, hand);
    }

    /**
     * If the clicked block exposes a fluid handler, compare its stored amount to {@link LabwareFluidContents#targetMb()}.
     * Block amount &gt; target: fill labware from the block. Block amount &lt; target: drain excess labware fluid into the block.
     */
    private static boolean tryDirectedLabwareFluidExchange(Player player, InteractionHand hand, Level level, BlockPos pos, Direction clickedFace) {
        if (level.isClientSide) {
            return false;
        }
        IFluidHandler blockHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, clickedFace);
        if (blockHandler == null) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCount() != 1) {
            return false;
        }
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (itemHandler == null) {
            return false;
        }

        LabwareFluidContents contents = stack.getOrDefault(ModDataComponents.LABWARE_CONTENTS.get(), LabwareFluidContents.EMPTY);
        int targetMb = contents.targetMb();
        int blockMb = totalFluidAmount(blockHandler);

        FluidStack moved;
        if (blockMb > targetMb) {
            moved = FluidUtil.tryFluidTransfer(blockHandler, itemHandler, blockMb - targetMb, true);
        } else if (blockMb < targetMb) {
            moved = FluidUtil.tryFluidTransfer(itemHandler, blockHandler, targetMb - blockMb, true);
        } else {
            return false;
        }
        if (moved.isEmpty()) {
            return false;
        }
        player.setItemInHand(hand, itemHandler.getContainer());
        return true;
    }

    private static int totalFluidAmount(IFluidHandler handler) {
        int total = 0;
        for (int t = 0; t < handler.getTanks(); t++) {
            total += handler.getFluidInTank(t).getAmount();
        }
        return total;
    }

    /**
     * Fills a fluid-container item from an infinite source: either the clicked block is that source (e.g. water),
     * or the neighbor in {@code clickedFace} direction is (clicking a solid face toward water).
     */
    public static boolean tryScoopInfiniteFromWorld(Player player, InteractionHand hand, Level level, BlockPos clickedPos, Direction clickedFace) {
        if (level.isClientSide) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCount() != 1) {
            return false;
        }
        BlockPos fluidPos = resolveInfiniteSourceFluidPos(level, clickedPos, clickedFace);
        if (fluidPos == null) {
            return false;
        }
        FluidState fluidState = level.getFluidState(fluidPos);

        FluidStack offer = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
        if (offer.isEmpty()) {
            return false;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (handler == null) {
            return false;
        }
        int filled = handler.fill(offer, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return false;
        }

        player.setItemInHand(hand, handler.getContainer());
        playFillSound(level, fluidPos, fluidState, player);
        return true;
    }

    @Nullable
    private static BlockPos resolveInfiniteSourceFluidPos(Level level, BlockPos clickedPos, Direction clickedFace) {
        FluidState inClicked = level.getFluidState(clickedPos);
        BlockPos neighbor = clickedPos.relative(clickedFace);
        FluidState inNeighbor = level.getFluidState(neighbor);
        if (inClicked.isSource()) {
            return clickedPos;
        }
        if (inNeighbor.isSource()) {
            return neighbor;
        }
        return null;
    }

    private static void playFillSound(Level level, BlockPos warePos, FluidState fluidState, @Nullable Player player) {
        SoundEvent sound = fluidState.getType().getFluidType().getSound(player, level, warePos, SoundActions.BUCKET_FILL);
        if (sound == null) {
            sound = SoundEvents.BUCKET_FILL;
        }
        level.playSound(player, warePos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
