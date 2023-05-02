package net.unseenhand.rezerogenesismod.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;
import net.unseenhand.rezerogenesismod.items.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<Block> LUGUNICA_PAVED_ROAD = registerBlock(
            "lugunica_paved_road",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(4.0F)
                    .friction(.5F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<MixingApparatusBlock> MIXING_APPARATUS = registerBlock(
            "mixing_apparatus",
            () -> new MixingApparatusBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<MixingApparatusControllerBlock> MIXING_APPARATUS_CONTROLLER = registerBlock(
            "mixing_apparatus_controller",
            () -> new MixingApparatusControllerBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}