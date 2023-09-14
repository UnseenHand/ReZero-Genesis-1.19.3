package net.unseenhand.rezerogenesismod.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;

import java.util.function.Supplier;

// Recipes are registered in pairs like (RecipeType<?>, RecipeSerializer<RecipeType<?>>)
// So probably some kind of Map would feet or something else
// Also there also need to be the Recipe implemented

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<RecipeType<MixingRecipe>> MIX =
            register("mixing", () -> getModRecipeType("mixing"));

    private static <T extends RecipeType<?>> RegistryObject<T> register(String name, Supplier<T> supplier) {
        return RECIPE_TYPES.register(name, supplier);
    }

    public static <T extends Recipe<?>> RecipeType<T> getModRecipeType(final String path) {
        return RecipeType.simple(new ResourceLocation(ReZeroGenesisMod.MOD_ID, path));
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
