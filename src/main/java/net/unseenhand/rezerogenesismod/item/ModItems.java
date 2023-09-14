package net.unseenhand.rezerogenesismod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<Item> COBBLESTONE_MIXTURE = ITEMS.register(
            "cobblestone_mixture",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FORMED_COBBLESTONE_MIXTURE_ITEM = ITEMS.register(
            "formed_cobblestone_mixture",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}