package net.unseenhand.rezerogenesismod.block.entity;

import net.minecraft.util.StringRepresentable;

public enum EnumMultiblockPartType implements StringRepresentable {
    NORMAL("normal"),
    CONTROLLER("controller"),
    BLOCK("block");

    private final String name;

    EnumMultiblockPartType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}