package net.unseenhand.rezerogenesismod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class MixingApparatus extends MultiblockBlockEntity {
    public MixingApparatus(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.MIXING_APPARATUS.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    public void tick() {

    }
}