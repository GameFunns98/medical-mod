package cz.vasek.medicalmod;

import com.mojang.logging.LogUtils;
import cz.vasek.medicalmod.registry.ModItems;
import cz.vasek.medicalmod.registry.ModMenus;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MedicalMod.MOD_ID)
public final class MedicalMod {
    public static final String MOD_ID = "medicalmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MedicalMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        modEventBus.addListener(this::addCreativeTabItems);

        LOGGER.info("Medical Mod is loading.");
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.MEDICAL_TABLET);
            event.accept(ModItems.DEFIBRILLATOR);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BANDAGE);
            event.accept(ModItems.TOURNIQUET);
            event.accept(ModItems.SPLINT);
            event.accept(ModItems.OXYGEN_MASK);
            event.accept(ModItems.MORPHINE);
            event.accept(ModItems.EPINEPHRINE);
            event.accept(ModItems.SALINE);
            event.accept(ModItems.CHEST_SEAL);
            event.accept(ModItems.DECOMPRESSION_NEEDLE);
        }
    }
}
