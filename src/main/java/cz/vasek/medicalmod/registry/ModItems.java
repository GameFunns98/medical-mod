package cz.vasek.medicalmod.registry;

import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.item.BandageItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MedicalMod.MOD_ID);

    public static final RegistryObject<Item> BANDAGE = ITEMS.register(
            "bandage",
            () -> new BandageItem(new Item.Properties().stacksTo(16))
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
