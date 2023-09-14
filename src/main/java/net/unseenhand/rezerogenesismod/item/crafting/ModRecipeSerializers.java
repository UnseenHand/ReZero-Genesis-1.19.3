package net.unseenhand.rezerogenesismod.item.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<MixingRecipe>> MIX =
            register("mixing", MixingRecipeSerializer::new);

    private static <T extends RecipeSerializer<?>> RegistryObject<T> register(String name, Supplier<T> supplier) {
        return RECIPE_SERIALIZERS.register(name, supplier);
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
