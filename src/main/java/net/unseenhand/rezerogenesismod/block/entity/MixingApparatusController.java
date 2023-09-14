package net.unseenhand.rezerogenesismod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.unseenhand.rezerogenesismod.common.handler.MultiblockStateHandler;
import net.unseenhand.rezerogenesismod.saveddata.MultiblocksData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MixingApparatusController extends MultiblockBlockEntity {
    /**
     * The number of blocks to form the multiblock
     */
    public static final int BLOCKS_IN_MULTIBLOCK_MAX = 8;
    public static final String STRUCTURE_BLOCK_COUNT_KEY = "count";
    public static final String STRUCTURE_STATE_KEY = "state";
    private int blockCount = 0;
    private boolean isFormed;

    public MixingApparatusController(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.MIXING_APPARATUS_CONTROLLER.get(), blockPos, blockState);
    }

    /**
     * This method <i>tracks</i> the change of the multiblock state and handles it appropriately
     *
     * @param pLevel  world level where the tick happened
     * @param pPos    position of the block
     * @param pState  state of the block
     * @param pEntity CONTROLLER attached BlockEntity
     * @// TODO: 19.05.2023 DO THIS METHOD CALL CLEAN UP TODAY
     */
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, MixingApparatusController pEntity) {
        // Ignore Client side
        if (pLevel.isClientSide()) {
            return;
        }

        // IF IS FORMED CHANGE THE BLOCK STATES AS NEEDED
        boolean formed = pEntity.isFormed(pLevel);
        MultiblockStateHandler.refreshMultiblockState(pLevel, pState, pEntity, formed);
    }

    private static boolean hasSingleController(Level pLevel, BlockPos[] positions) {
        int controllers = 0;

        for (BlockPos position : positions) {
            BlockEntity blockEntity = pLevel.getBlockEntity(position);
            if (blockEntity instanceof MixingApparatusController) {
                controllers++;
            }
        }

        return controllers == 1;
    }

    private static void putNewInstanceInMap(BlockPos[] positions) {
        UUID uuid = UUID.randomUUID();
        MultiblocksData.MAP.put(uuid, positions);
    }

    // TODO:
    // Should create a different Menu for the controller block
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory pInventory, @NotNull Player pPlayer) {
        return null;
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.literal("Mixing Apparatus Controller");
    }

    private boolean isFormed(Level pLevel) {
        for (int i = 0; i < MultiblocksData.LIST.size(); i++) {
            boolean hasController = false;
            BlockPos[] positions = MultiblocksData.LIST.get(i);
            for (BlockPos position : positions) {
                // Contains this BlockEntity
                if (position == worldPosition) {
                    hasController = true;
                    break;
                }
            }

            // If the Array has at least a controller and the 8 blocks in total
            if (hasController && positions.length == BLOCKS_IN_MULTIBLOCK_MAX) {
                if (hasSingleController(pLevel, positions)) {
                    putNewInstanceInMap(positions);

                    return true;
                }
            }
        }

        return false;
        // return isFormed && this.isMultiblockFull();
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        isFormed = nbt.getBoolean(STRUCTURE_STATE_KEY);
        blockCount = nbt.getInt(STRUCTURE_BLOCK_COUNT_KEY);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        nbt.putBoolean(STRUCTURE_STATE_KEY, isFormed);
        nbt.putInt(STRUCTURE_BLOCK_COUNT_KEY, blockCount);

        super.saveAdditional(nbt);
    }
}