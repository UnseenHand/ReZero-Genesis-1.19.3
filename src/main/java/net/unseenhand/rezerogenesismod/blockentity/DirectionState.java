package net.unseenhand.rezerogenesismod.blockentity;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum DirectionState implements StringRepresentable {
    OPENED,
    CLOSED,
    CONNECTED;

    @Contract(pure = true)
    @NotNull
    @Override
    public String getSerializedName() {
        return this.toString();
    }
}
