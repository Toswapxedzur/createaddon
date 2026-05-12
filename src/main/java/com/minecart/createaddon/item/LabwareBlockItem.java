package com.minecart.createaddon.item;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 1.20.1 Forge port: Block fluid handlers are queried via {@code BlockEntity.getCapability(...)}
 * (no {@code Level.getCapability}). Item NBT replaces data components.
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
            // Disabled until tested; mirror of 1.21.1 source comment.
//            if (tryScoopInfiniteFromWorld(player, context.getHand(), level, context.getClickedPos(), context.getClickedFace())) {
//                return InteractionResult.sidedSuccess(level.isClientSide);
//            }
        }
        return super.useOn(context);
    }

    private static boolean tryDirectedLabwareFluidExchange(Player player, InteractionHand hand, Level level, BlockPos pos, Direction clickedFace) {
        if (level.isClientSide) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return false;
        IFluidHandler blockHandler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, clickedFace).orElse(null);
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

        LabwareFluidContents contents = stack.getTag() == null || !stack.getTag().contains("LabwareContents")
                ? LabwareFluidContents.EMPTY
                : LabwareFluidContents.read(stack.getTag().getCompound("LabwareContents"));
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
        if (inClicked.isSource() && inNeighbor.isSource()) {
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
