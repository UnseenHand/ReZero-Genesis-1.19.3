package net.unseenhand.rezerogenesismod.common.manager;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MultiblockManagerClean {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static BlockPos[] getRelativeNeighbours(Level pLevel, BlockPos pPos) {
        // New array of positions to fill
        List<BlockPos> list = new ArrayList<>();
        // Does the block has neighbours which are the same blocks
        for (Direction direction : Direction.values()) {
            // Get the relative BlockPos to the specific Direction towards the 'target' given one
            BlockPos relativeBlockPos = pPos.relative(direction);
            // Simply gets the relative Block
            Block relativeBlock = pLevel.getBlockState(relativeBlockPos).getBlock();
            if (relativeBlock == ModBlocks.MIXING_APPARATUS.get() ||
                    relativeBlock == ModBlocks.MIXING_APPARATUS_CONTROLLER.get()) {
                list.add(relativeBlockPos);
            }
        }
        // TODO:
        // Return the List as an Array, also think about optimization
        return list.toArray(new BlockPos[0]);
    }

    public static void setCustomPropForRelativeNeighbours(Level pLevel, BlockPos pPos) {
        BlockPos[] positions = getRelativeNeighbours(pLevel, pPos);

        if (positions.length == 0) {

        }
    }
}
