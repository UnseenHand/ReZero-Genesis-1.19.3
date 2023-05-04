package net.unseenhand.rezerogenesismod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.unseenhand.rezerogenesismod.blockentity.EnumMultiblockPartType;
import org.jetbrains.annotations.Nullable;

public abstract class MultiblockBlock extends Block implements EntityBlock {
    public static final EnumProperty<EnumMultiblockPartType> MULTIBLOCK_PART_TYPE =
            EnumProperty.create("multiblock_block_type",
                    EnumMultiblockPartType.class,
                    EnumMultiblockPartType.NORMAL,
                    EnumMultiblockPartType.BLOCK,
                    EnumMultiblockPartType.CONTROLLER);

    public MultiblockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState().setValue(MULTIBLOCK_PART_TYPE, EnumMultiblockPartType.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateDefinition) {
        stateDefinition.add(MULTIBLOCK_PART_TYPE);
    }

    // Define use of this method
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);
}