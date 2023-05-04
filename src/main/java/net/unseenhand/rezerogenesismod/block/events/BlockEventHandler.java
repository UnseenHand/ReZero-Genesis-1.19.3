package net.unseenhand.rezerogenesismod.block.events;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import net.unseenhand.rezerogenesismod.blockentity.MixingApparatusController;
import net.unseenhand.rezerogenesismod.common.managers.MultiblockManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.UUID;

public class BlockEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onBlockPlaced(@NotNull BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            BlockState state = event.getPlacedBlock();
            Block block = state.getBlock();

            if (block == ModBlocks.MIXING_APPARATUS.get() ||
                    block == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                Level level = player.getLevel();
                BlockPos pos = event.getPos();

                // Closes and Connects blocks to form a single unit (structure)
                UUID Id = MultiblockManager.constraintTargetAndNeighbours(event, level, block, pos);

                MixingApparatusController multiblock = MixingApparatusController.MAP.get(Id);

                // Something like increasing the counter by one when the block is added
                multiblock.addBlock();

                // If enough blocks -> structure should be formed
                // Maybe should be called every some ticks

                if (multiblock.isMultiblockFull()) {
                    // the block placed is part of a multi-block structure
                    // do something here, e.g. mark the block as part of the structure
                    multiblock.markAsFormed(); // Should trigger the multiblock creation
                    // Creation process...
                    multiblock.createStructure();
                    // Logging that the multiblock was successfully created
                    LOGGER.info(String.format("Multiblock with the ID: \"%s\", has been successfully created!!!", Id));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(@NotNull PlayerInteractEvent.RightClickBlock event) {
        // Logic here
        Level level = event.getLevel();

        // If the block is multiblock block and logical side maybe ( or physical )
        if (level.isClientSide() &&
                level.getBlockState(event.getPos()).getBlock() == ModBlocks.MIXING_APPARATUS.get()) {
            // Get the ID of the block

            // Get the multiblock by the block ID

            // Check if the multiblock is formed

            // If it does show the GUI of the block (needs further development)
        }
    }
}
