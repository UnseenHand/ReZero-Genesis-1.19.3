package net.unseenhand.rezerogenesismod.block.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.unseenhand.rezerogenesismod.block.MultiblockBlock;
import net.unseenhand.rezerogenesismod.block.entity.EnumMultiblockPartType;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import net.unseenhand.rezerogenesismod.block.entity.MultiblockBlockEntity;
import net.unseenhand.rezerogenesismod.common.handler.MultiblockDirectionStateHandler;
import net.unseenhand.rezerogenesismod.common.handler.MultiblockDirectionStateHandler.Action;
import org.jetbrains.annotations.NotNull;

public class BlockEventsHandler {
    @SubscribeEvent
    public static void onBlockPlaced(@NotNull BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            BlockState placedBlock = event.getPlacedBlock();

            Block block = placedBlock.getBlock();

            // Mixing Apparatus Or Mixing Apparatus Controller
            if (MultiblockDirectionStateHandler.isMAPart(block)) {
                Level level = player.getLevel();
                BlockPos pos = event.getPos();

                // Handling
                MultiblockDirectionStateHandler.handle(level, pos, Action.SET);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRemoved(BlockEvent.BreakEvent event) {
        BlockState removedBlock = event.getState();
        Block block = removedBlock.getBlock();

        if (MultiblockDirectionStateHandler.isMAPart(block)) {
            Player player = event.getPlayer();

            Level level = player.getLevel();
            BlockPos pos = event.getPos();

            // Handling
            MultiblockDirectionStateHandler.handle(level, pos, Action.REMOVE);

            // TODO: Handle drops
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MultiblockBlockEntity multiblockBlock) {
                multiblockBlock.drops(level);

                // TODO: Improve for fluid drops
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(@NotNull PlayerInteractEvent.RightClickBlock event) {
        // Logic here
        Level level = event.getLevel();

        // If the block is multiblock block and logical side maybe ( or physical )
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Player player = event.getEntity();

        if (block instanceof MultiblockBlock) {
            if (EnumMultiblockPartType.BLOCK == state.getValue(MultiblockBlock.MULTIBLOCK_PART_TYPE)) {
                // If it does show the GUI of the block (needs further development)
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof MixingApparatus mixingApparatus) {
                    NetworkHooks.openScreen((ServerPlayer) player, mixingApparatus, pos);
                } else {
                    throw new IllegalStateException("Custom container provider is missing!");
                }
            }
        }
    }
}