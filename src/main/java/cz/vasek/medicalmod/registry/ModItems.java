package cz.vasek.medicalmod.registry;

import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.item.BandageItem;
import cz.vasek.medicalmod.item.MedicalTabletItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MedicalMod.MOD_ID);

    public static final RegistryObject<Item> MEDICAL_TABLET = ITEMS.register(
            "medical_tablet",
            () -> new MedicalTabletItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> BANDAGE = ITEMS.register(
            "bandage",
            () -> new BandageItem(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> TOURNIQUET = ITEMS.register(
            "tourniquet",
            () -> new Item(new Item.Properties().stacksTo(8))
    );

    public static final RegistryObject<Item> SPLINT = ITEMS.register(
            "splint",
            () -> new Item(new Item.Properties().stacksTo(8))
    );

    public static final RegistryObject<Item> OXYGEN_MASK = ITEMS.register(
            "oxygen_mask",
            () -> new Item(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<Item> MORPHINE = ITEMS.register(
            "morphine",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> EPINEPHRINE = ITEMS.register(
            "epinephrine",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> SALINE = ITEMS.register(
            "saline",
            () -> new Item(new Item.Properties().stacksTo(8))
    );

    public static final RegistryObject<Item> CHEST_SEAL = ITEMS.register(
            "chest_seal",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> DECOMPRESSION_NEEDLE = ITEMS.register(
            "decompression_needle",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> DEFIBRILLATOR = ITEMS.register(
            "defibrillator",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
