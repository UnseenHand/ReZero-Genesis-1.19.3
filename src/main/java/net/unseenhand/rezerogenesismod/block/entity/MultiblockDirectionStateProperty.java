package net.unseenhand.rezerogenesismod.block.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

// Class should be able to refer through the Direction$
// Suppose we get the list of Directions
// And I want to specify that DirectionProperty value for each of the directions
// So I would go Direction$UP(6 possible values)

public class MultiblockDirectionStateProperty implements INBTSerializable<CompoundTag> {
    public static final String DIRECTION_DATA_KEY = "DirectionData";
    public static final String DIRECTION_KEY = "direction";
    public static final String STATE_KEY = "state";
    public static final String MAP_DATA_KEY = "MapData";
    private ImmutableMap<Direction, DirectionState> values;

    public MultiblockDirectionStateProperty(DirectionState defaultValue) {
        ImmutableMap.Builder<Direction, DirectionState> builder = ImmutableMap.builder();
        for (Direction dir : Direction.values()) {
            builder.put(dir, defaultValue);
        }
        values = builder.buildOrThrow();
    }

    public MultiblockDirectionStateProperty(Map<Direction, DirectionState> map) {
        ImmutableMap.Builder<Direction, DirectionState> builder = ImmutableMap.builder();
        builder.putAll(map); // I don't know if it is necessary, but I will attach to it for now
        values = builder.buildOrThrow();
    }

    public DirectionState get(Direction dir) {
        return values.get(dir);
    }

    public ImmutableMap<Direction, DirectionState> getValues() {
        return values;
    }

    public MultiblockDirectionStateProperty with(Direction dir, DirectionState value) {
        ImmutableMap.Builder<Direction, DirectionState> builder = ImmutableMap.builder();
        for (Map.Entry<Direction, DirectionState> entry : values.entrySet()) {
            if (!entry.getKey().equals(dir)) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        builder.put(dir, value);
        return new MultiblockDirectionStateProperty(builder.buildOrThrow());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag data = new CompoundTag();

        ImmutableMap<Direction, DirectionState> map = this.getValues();
        ListTag mapTag = new ListTag();

        for (Map.Entry<Direction, DirectionState> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            Direction direction = entry.getKey();
            DirectionState state = entry.getValue();

            entryTag.putInt(DIRECTION_KEY, direction.ordinal());
            entryTag.putInt(STATE_KEY, state.ordinal());

            mapTag.add(entryTag);
        }

        data.put(MAP_DATA_KEY, mapTag);
        return data;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag list = nbt.getList(MAP_DATA_KEY, Tag.TAG_COMPOUND);

        Map<Direction, DirectionState> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            Direction direction = Direction.from3DDataValue(entry.getInt(DIRECTION_KEY));
            DirectionState state = DirectionState.fromIntDataValue(entry.getInt(STATE_KEY));

            map.put(direction, state);
        }

        values = ImmutableMap.<Direction, DirectionState>builder().putAll(map).buildOrThrow();
    }
}