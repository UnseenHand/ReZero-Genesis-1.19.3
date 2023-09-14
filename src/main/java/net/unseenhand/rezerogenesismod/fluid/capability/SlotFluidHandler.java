package net.unseenhand.rezerogenesismod.fluid.capability;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SlotFluidHandler extends Slot {
    private static final Container emptyInventory = new SimpleContainer(1);
    private final IFluidHandler fluidHandler;
    private final int index;

    public SlotFluidHandler(IFluidHandler fluidHandler, int index, int xPosition, int yPosition) {
        super(null, index, xPosition, yPosition);
        this.index = index;
        this.fluidHandler = fluidHandler;
    }

    public IFluidHandler getFluidHandler() {
        return this.fluidHandler;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        // Allow only items with fluid capabilities
        return itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent();
    }

    @Override
    public void set(ItemStack itemStack) {
        FluidStack fluid = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .map(handler -> handler.drain(12000, IFluidHandler.FluidAction.EXECUTE))
                .orElse(FluidStack.EMPTY);
        this.getFluidHandler().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        this.setChanged();
    }
}
