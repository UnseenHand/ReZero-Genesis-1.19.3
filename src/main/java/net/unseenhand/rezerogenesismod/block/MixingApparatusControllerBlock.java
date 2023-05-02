package net.unseenhand.rezerogenesismod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.unseenhand.rezerogenesismod.blockentity.MixingApparatusController;
import org.jetbrains.annotations.Nullable;

public class MixingApparatusControllerBlock extends MultiblockBlock {
    public MixingApparatusControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixingApparatusController(pos, state);
    }
}