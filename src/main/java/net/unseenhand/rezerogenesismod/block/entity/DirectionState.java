package net.unseenhand.rezerogenesismod.block.entity;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum DirectionState implements StringRepresentable {
    OPENED,
    CLOSED,
    CONNECTED;

    public static DirectionState fromIntDataValue(int pIndex) {
        return DirectionState.values()[pIndex];
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String getSerializedName() {
        return this.toString();
    }
}