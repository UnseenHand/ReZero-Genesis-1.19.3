package net.unseenhand.rezerogenesismod.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;
import net.unseenhand.rezerogenesismod.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<MixingApparatus>> MIXING_APPARATUS = BLOCK_ENTITIES.register(
            "mixing_apparatus",
            () -> BlockEntityType.Builder.of(MixingApparatus::new, ModBlocks.MIXING_APPARATUS.get())
                    .build(null));

    // Rethink if this is right thing to assign the entity to
    public static final RegistryObject<BlockEntityType<MixingApparatusController>> MIXING_APPARATUS_CONTROLLER = BLOCK_ENTITIES.register(
            "mixing_apparatus_controller",
            () -> BlockEntityType.Builder.of(MixingApparatusController::new, ModBlocks.MIXING_APPARATUS_CONTROLLER.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}