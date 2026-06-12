package cz.vasek.medicalmod.registry;

import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.menu.MedicalMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MedicalMod.MOD_ID);

    public static final RegistryObject<MenuType<MedicalMenu>> MEDICAL_MENU = MENUS.register(
            "medical_menu",
            () -> IForgeMenuType.create(MedicalMenu::new)
    );

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
