package net.unseenhand.rezerogenesismod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class MultiblockBlock extends Block implements EntityBlock {
    public MultiblockBlock(Properties properties) {
        super(properties);
    }

    // Define use of this method
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);
}