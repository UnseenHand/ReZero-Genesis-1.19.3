package net.unseenhand.rezerogenesismod.block;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.unseenhand.rezerogenesismod.block.entity.EnumMultiblockPartType;
import org.jetbrains.annotations.NotNull;

public abstract class MultiblockBlock extends BaseEntityBlock {
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

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }
}