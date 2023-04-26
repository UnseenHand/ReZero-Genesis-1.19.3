package net.unseenhand.rezerogenesismod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.unseenhand.rezerogenesismod.items.ModCreativeModeTabs;
import net.unseenhand.rezerogenesismod.blocks.ModBlocks;
import net.unseenhand.rezerogenesismod.items.ModItems;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ReZeroGenesisMod.MOD_ID)
public class ReZeroGenesisMod {
    public static final String MOD_ID = "rezerogenesismod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ReZeroGenesisMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        // Creative tab build
        @SubscribeEvent
        public static void buildContents(@NotNull CreativeModeTabEvent.BuildContents event) {
            // Add to 'Building Blocks' tab
            if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS) {
                event.accept(ModBlocks.LUGUNICA_PAVED_ROAD_BLOCK);
            }

            // Add to 'Ingredients' tab
            if (event.getTab() == CreativeModeTabs.INGREDIENTS) {
                event.accept(ModItems.COBBLESTONE_MIXTURE_ITEM);
            }

            // Add to 'Re:Zero Genesis' tab
            if (event.getTab() == ModCreativeModeTabs.Re_Zero_Genesis_Tab) {
                // Blocks
                event.accept(ModBlocks.LUGUNICA_PAVED_ROAD_BLOCK);

                //Items
                event.accept(ModItems.COBBLESTONE_MIXTURE_ITEM);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onBlockDestruction(@NotNull BlockEvent.BreakEvent event) {
            // Logic here
            BlockState blockState = event.getState();
            BlockPos pos = event.getPos();
            Player player = event.getPlayer();
            LevelAccessor level = event.getLevel();

            Block block = blockState.getBlock();

            LOGGER.info("Player " + player.getName().getString() +
                    " broke the \"" + block.getName().getString() +
                    "\" block."
            );

            // Check if the broken block is the 'Lugunica Paved Road' block and the tool used is appropriate
            if (block == ModBlocks.LUGUNICA_PAVED_ROAD_BLOCK.get() && player.hasCorrectToolForDrops(blockState)) {
                Item item = block.asItem();

                ItemStack stack = new ItemStack(item, 1);

                Block.popResource((Level) level, pos, stack);

                LOGGER.info("The \"" + item.getName(stack).getString() + "\" item has been dropped!");
            } else {
                LOGGER.info("The tool, block was broken with, is not appropriate! Item hasn't been dropped!");
            }
        }

        @SubscribeEvent
        public static void onBlockExplosion(@NotNull ExplosionEvent.Detonate event) {
            // Logic here
            List<BlockPos> blocksPos = event.getAffectedBlocks();
            Level level = event.getLevel();

            for (BlockPos pos : blocksPos) {
                Block block = level.getBlockState(pos).getBlock();

                // Checking if the exploded block is 'Lugunica Paved Road'
                // Which has the 75% chance of dropping itself
                if (block == ModBlocks.LUGUNICA_PAVED_ROAD_BLOCK.get() && level.random.nextFloat() < .75F) {
                    Item item = block.asItem();

                    ItemStack stack = new ItemStack(item, 1);

                    Block.popResource(level, pos, stack);
                }
            }
        }
    }
}