package net.unseenhand.rezerogenesismod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;
import net.unseenhand.rezerogenesismod.block.ModBlocks;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = ReZeroGenesisMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTabs {
    public static CreativeModeTab Re_Zero_Genesis_Tab;

    @SubscribeEvent
    public static void registerCreativeModTabs(@NotNull CreativeModeTabEvent.Register event) {
        Re_Zero_Genesis_Tab = event.registerCreativeModeTab(
                new ResourceLocation(ReZeroGenesisMod.MOD_ID, "re_zero_genesis_tab"),
                builder -> builder.icon(() -> new ItemStack(ModBlocks.LUGUNICA_PAVED_ROAD.get().asItem()))
                        .title(Component.translatable("creativemodetab.re_zero_genesis_tab"))
        );
    }
}