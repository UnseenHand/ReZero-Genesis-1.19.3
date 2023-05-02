package net.unseenhand.rezerogenesismod.blockentity;

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public abstract class MultiblockBlockEntity extends BlockEntity implements Tickable {
    private UUID ID;
    private MultiblockDirectionStateProperty customProperty;

    public MultiblockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        customProperty = new MultiblockDirectionStateProperty(DirectionState.OPENED); // default value
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public DirectionState getCustomProperty(Direction dir) {
        return customProperty.get(dir);
    }

    public void setCustomProperty(Direction dir, DirectionState value) {
        customProperty = customProperty.with(dir, value);
    }

    public boolean hasConnections(int number) {
        int counter = 0;
        for (Direction direction : Direction.values()) {
            if (this.getCustomProperty(direction) == DirectionState.CONNECTED) {
                counter++;
                if (counter == number) {
                    return true;
                }
            }
        }
        return false;
    }
}