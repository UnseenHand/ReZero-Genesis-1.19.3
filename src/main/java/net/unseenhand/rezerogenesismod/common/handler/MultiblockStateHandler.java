package net.unseenhand.rezerogenesismod.common.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.unseenhand.rezerogenesismod.block.MultiblockBlock;
import net.unseenhand.rezerogenesismod.block.entity.EnumMultiblockPartType;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatusController;
import net.unseenhand.rezerogenesismod.block.entity.MultiblockBlockEntity;
import net.unseenhand.rezerogenesismod.logging.MultiblockLogUtil;
import net.unseenhand.rezerogenesismod.saveddata.MultiblocksData;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Use this class for handling multiblock blocks state change <br>
 * Under the hood it involves changing block <b>TEXTURES</b> so it is visible to the client <br>
 * This also include its dynamic change, so consider its use in the <br>
 * #tick method
 * {@link BlockEntityTicker#tick(Level, BlockPos, BlockState, BlockEntity)}
 * <br>Method below can use that method reference as the <i>`Ticker`</i><br>
 * #createTickerHelper method
 * {@link BaseEntityBlock#createTickerHelper(BlockEntityType, BlockEntityType, BlockEntityTicker)}
 *
 * @// FIXME: 20.05.2023
 * @// CLEAN-UP 0 0
 */
public class MultiblockStateHandler {
    private static boolean shouldReset = false;

    private static BlockState getNewState(BlockState state, EnumMultiblockPartType newType) {
        return state.setValue(MultiblockBlock.MULTIBLOCK_PART_TYPE, newType);
    }

    public static void refreshMultiblockState(Level pLevel,
                                              BlockState pState,
                                              MixingApparatusController controller,
                                              boolean formed) {
        if (formed) {
            update(pLevel, controller); // Always true if FORMED is true
        } else {
            resetOrKeep(pLevel, pState, controller); // May have no need in a state change
        }
    }

    private static void resetOrKeep(Level pLevel, BlockState controllerState, MixingApparatusController controller) {
        // A single check on CONTROLLER for the need of additional actions
        if (shouldReset(controllerState)) {
            set(pLevel, controller, MultiblockStateHandler::setCustomBlockState);

            // Set as default
            // That means each call will use the default value, set it to `true` if needed, and repeat the cycle again
            shouldReset = false;
        }
        // Keep means to avoid redundant action like reassigning the same properties
    }

    private static boolean shouldReset(BlockState controllerState) {
        boolean reset = controllerState.getValue(MultiblockBlock.MULTIBLOCK_PART_TYPE) != EnumMultiblockPartType.NORMAL;
        shouldReset = reset;
        return reset;
    }

    private static void set(Level pLevel,
                            MixingApparatusController controller,
                            BiConsumer<Level, BlockPos> updateMethod) {
        UUID uuid = controller.getID();
        MultiblocksData data = MultiblocksData.retrieveData(pLevel);
        BlockPos[] blockPositions = MultiblocksData.MAP.get(uuid);
        for (BlockPos pos : blockPositions) {
            updateMethod.accept(pLevel, pos);
        }

        // Make sure the changes that were made will be saved to the SavedData
        data.setDirty();
    }

    /**
     * <b>Sets</b> the new {@link BlockState}'s {@link EnumProperty} for the block at the given position<br>
     * This method expects only {@link MultiblockBlockEntity} subtypes<br>
     * Thus it will <i>throw an exception</i> if the instance of {@link BlockEntity} <b>is unexpected<b/>
     *
     * @param pLevel the current world level
     * @param pPos   the multiblock block position
     * @throws IllegalStateException unexpected block position provided
     * @apiNote Check {@link MultiblockBlock} to see the expected block
     */
    public static void setCustomBlockState(Level pLevel, BlockPos pPos) {
        BlockState state = pLevel.getBlockState(pPos);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

        // Reset (set the default) Custom Block Property for the current BE at the given BlockPos
        BlockState newState;
        if (blockEntity instanceof MultiblockBlockEntity) {
            if (!shouldReset) {
                if (blockEntity instanceof MixingApparatus) {
                    newState = getNewState(state, EnumMultiblockPartType.BLOCK);
                } else if (blockEntity instanceof MixingApparatusController) {
                    newState = getNewState(state, EnumMultiblockPartType.CONTROLLER);
                } else {
                    // Log error
                    MultiblockLogUtil.logBlockEntityNotMatchesError(pPos);
                    return;
                }
            } else {
                newState = getNewState(state, EnumMultiblockPartType.NORMAL);
            }
        } else {
            // Log error
            MultiblockLogUtil.logBlockEntityNotMatchesError(pPos);
            return;
        }

        // Update block state
        pLevel.setBlockAndUpdate(pPos, newState);

        // Log info
        MultiblockLogUtil.logStateChangeSuccessInfo(pPos, state, newState);
    }

    private static void update(Level pLevel, MixingApparatusController controller) {
        set(pLevel, controller, MultiblockStateHandler::setCustomBlockState);
    }
}