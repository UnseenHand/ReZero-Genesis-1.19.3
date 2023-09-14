package net.unseenhand.rezerogenesismod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.unseenhand.rezerogenesismod.block.entity.MixingApparatus;
import net.unseenhand.rezerogenesismod.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class MixingApparatusBlock extends MultiblockBlock {
    public MixingApparatusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixingApparatus(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MIXING_APPARATUS.get(), MixingApparatus::tick);
    }
}