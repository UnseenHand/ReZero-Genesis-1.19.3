package net.unseenhand.rezerogenesismod.blockentity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;

import java.util.Map;

// Class should be able to refer through the Direction$
// Suppose we get the list of Directions
// And I want to specify that DirectionProperty value for each of the directions
// So I would go Direction$UP(6 possible values)

public class MultiblockDirectionStateProperty {
    private final ImmutableMap<Direction, DirectionState> values;

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
}