package net.unseenhand.rezerogenesismod.logging;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.unseenhand.rezerogenesismod.block.MultiblockBlock;
import org.slf4j.Logger;

public class MultiblockLogUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void logStateChangeSuccessInfo(BlockPos pPos, BlockState oldState, BlockState newState) {
        // Log the message
        LOGGER.info(String.format("Block at the position: %s, was assigned with the new custom block state", pPos));
        LOGGER.info(String.format("Old value: %s, New value: %s",
                oldState.getValue(MultiblockBlock.MULTIBLOCK_PART_TYPE),
                newState.getValue(MultiblockBlock.MULTIBLOCK_PART_TYPE)));
    }

    public static void logBlockEntityNotMatchesError(BlockPos pPos) {
        // If unexpected behaviour (BUG) writes the message to the logs and throws an exception
        LOGGER.warn(String.format("Unexpected instance of the BlockEntity a the position: %s", pPos));
        LOGGER.error("BlockState was not changed! Error occurred!");
        throw new IllegalStateException(
                String.format("The block entity was not found for the multiblock part. Block position: %s", pPos));
    }
}
