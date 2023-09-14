package net.unseenhand.rezerogenesismod.client.gui.menu;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ReZeroGenesisMod.MOD_ID);

    public static final RegistryObject<MenuType<MixingApparatusMenu>> MIXING_APPARATUS_MENU =
            register("mixing_apparatus_menu", MixingApparatusMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String name,
                                                                                          IContainerFactory<T> sup) {
        return MENUS.register(name, () -> IForgeMenuType.create(sup));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
