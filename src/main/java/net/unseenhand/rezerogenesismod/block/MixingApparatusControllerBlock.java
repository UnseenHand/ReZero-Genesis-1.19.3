package net.unseenhand.rezerogenesismod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatusController;
import net.unseenhand.rezerogenesismod.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class MixingApparatusControllerBlock extends MultiblockBlock {
    public MixingApparatusControllerBlock(Properties properties) {
        super(properties);
    }

    @ParametersAreNonnullByDefault
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MixingApparatusController(pPos, pState);
    }

    @ParametersAreNonnullByDefault
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel,
                                                                  BlockState pState,
                                                                  BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType,
                ModBlockEntities.MIXING_APPARATUS_CONTROLLER.get(),
                MixingApparatusController::tick);
    }
}