package net.unseenhand.rezerogenesismod.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.unseenhand.rezerogenesismod.item.crafting.MixingRecipe;
import net.unseenhand.rezerogenesismod.item.crafting.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

public class MixingApparatus extends MultiblockBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MixingApparatus(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.MIXING_APPARATUS.get(), blockPos, blockState);
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(2).getMaxStackSize() > inventory.getItem(2).getCount();
    }

    public static boolean canInsertIntoOutputSlot(SimpleContainer inventory, MixingRecipe recipe) {
        return canInsertAmountIntoOutputSlot(inventory) &&
                canInsertItemIntoOutputSlot(inventory, recipe.getResultItem());
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack itemStack) {
        return inventory.getItem(2).getItem() == itemStack.getItem() || inventory.getItem(2).isEmpty();
    }

    private static void craftItem(Level level, MixingApparatus pEntity) {
        Optional<MixingRecipe> recipe = tryGetRecipe(level, pEntity);

        if (hasRecipe(level, pEntity) && recipe.isPresent()) {
            pEntity.fluidHandler.drain(recipe.get().getFluidStack().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            ItemStack assembledStack = recipe.get().assemble(new RecipeWrapper(pEntity.itemHandler));

            if (!assembledStack.isEmpty()) {
                pEntity.resetProgress();
            } else {
                LOGGER.error("Unexpected stack size assembled. Progress was not reset.", new RuntimeException());
            }
        } else {
            LOGGER.error("Recipe not found. May be that the custom one is not set properly.", new RuntimeException());
        }
    }

    private static void fillTankWithFluid(MixingApparatus pEntity, FluidStack stack, ItemStack container) {
        pEntity.fluidHandler.fill(stack, IFluidHandler.FluidAction.EXECUTE);

        pEntity.itemHandler.extractItem(3, 1, false);
        pEntity.itemHandler.insertItem(3, container, false);
    }

    private static boolean hasCorrectFluidInTank(MixingApparatus pEntity, MixingRecipe recipe) {
        return recipe.getFluidStack().equals(pEntity.fluidHandler.getFluid());
    }

    private static boolean hasEnoughFluid(MixingApparatus pEntity) {
        return pEntity.fluidHandler.getFluidAmount() >= 200;
    }

    private static boolean hasFluidItemInSourceSlot(MixingApparatus pEntity) {
        return pEntity.itemHandler.getStackInSlot(3).getCount() > 0;
    }

    private static boolean hasRecipe(Level pLevel, MixingApparatus pEntity) {
        SimpleContainer inventory = new SimpleContainer(pEntity.itemHandler.getSlots());
        for (int i = 0; i < pEntity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, pEntity.itemHandler.getStackInSlot(i));
        }

        Optional<MixingRecipe> recipe = tryGetRecipe(pLevel, pEntity);

        return recipe.isPresent() &&
                hasCorrectFluidInTank(pEntity, recipe.get()) &&
                canInsertIntoOutputSlot(inventory, recipe.get());
    }

    private static Optional<MixingRecipe> tryGetRecipe(Level pLevel, MixingApparatus pEntity) {
        return pLevel.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.MIX.get(), new RecipeWrapper(pEntity.itemHandler), pLevel);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MixingApparatus pEntity) {
        if (level.isClientSide()) {
            return;
        }

        // Get the recipe for the mixer
        if (hasRecipe(level, pEntity) && hasEnoughFluid(pEntity)) {
            pEntity.progress++;
            setChanged(level, pos, state);

            if (pEntity.progress >= pEntity.maxProgress) {
                craftItem(level, pEntity);
            }
        } else {
            pEntity.resetProgress();
            setChanged(level, pos, state);
        }

        if (hasFluidItemInSourceSlot(pEntity)) {
            transferItemFluidToFluidTank(pEntity);
        }
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.literal("Mixing Apparatus");
    }

    private static void transferItemFluidToFluidTank(MixingApparatus pEntity) {
        pEntity.itemHandler.getStackInSlot(3).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int drainAmount = Math.min(pEntity.fluidHandler.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            if (pEntity.fluidHandler.isFluidValid(stack)) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(pEntity, stack, handler.getContainer());
            }

        });
    }
}