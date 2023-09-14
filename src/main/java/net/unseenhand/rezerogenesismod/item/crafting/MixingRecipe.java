package net.unseenhand.rezerogenesismod.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.unseenhand.rezerogenesismod.item.ModItems;
import org.jetbrains.annotations.NotNull;

public class MixingRecipe implements Recipe<Container> {
    protected final NonNullList<Ingredient> inputs;
    protected final FluidStack fluidStack;
    private final ResourceLocation id;
    protected final ItemStack output;

    public MixingRecipe(ResourceLocation id,
                        ItemStack output,
                        NonNullList<Ingredient> inputs,
                        FluidStack fluidStack) {
        this.id = id;
        this.output = output;
        this.inputs = inputs;
        this.fluidStack = fluidStack;
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull Container container) {
        if (container instanceof RecipeWrapper wrapper) {
            // Gets the output slot items count and incremented by one count
            int outputSlotItemsCount = wrapper.getItem(2).getCount();
            int incrementedOutput = outputSlotItemsCount + 1;
            ItemStack toReturn = new ItemStack(ModItems.FORMED_COBBLESTONE_MIXTURE_ITEM.get(), incrementedOutput);

            // Removes 1 item from the input and increments the output slot items count
            wrapper.removeItem(1, 1);
            wrapper.setItem(2, toReturn);

            // Returns the item amount into the resulting slot
            return toReturn;
        }

        // TO-CHANGE:
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth == 1 && pHeight == 1;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @NotNull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MIX.get();
    }

    @NotNull
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MIX.get();
    }

    // Checks if the recipe in the container has the matching items
    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return inputs.get(0).test(pContainer.getItem(1));
    }
}
