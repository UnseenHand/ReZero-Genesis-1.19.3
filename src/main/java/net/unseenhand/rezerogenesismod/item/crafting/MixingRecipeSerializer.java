package net.unseenhand.rezerogenesismod.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.unseenhand.rezerogenesismod.util.FluidJsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MixingRecipeSerializer implements RecipeSerializer<MixingRecipe> {
    @NotNull
    @Override
    public MixingRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
        ItemStack output =
                ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

        JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "input");
        NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);
        FluidStack fluid = FluidJsonUtil.readFluidFromJson(pSerializedRecipe.get("fluid").getAsJsonObject());

        for (int i = 0; i < ingredients.size(); i++) {
            inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
        }

        return new MixingRecipe(pRecipeId, output, inputs, fluid);
    }

    @Override
    public @Nullable MixingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);
        FluidStack fluid = buf.readFluidStack();

        inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));

        ItemStack output = buf.readItem();
        return new MixingRecipe(id, output, inputs, fluid);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, MixingRecipe recipe) {
        NonNullList<Ingredient> inputs = recipe.getIngredients();

        buf.writeInt(inputs.size());
        buf.writeFluidStack(recipe.fluidStack);

        for(Ingredient ingredient : inputs) {
            ingredient.toNetwork(buf);
        }

        buf.writeItemStack(recipe.getResultItem(), false);
    }
}
