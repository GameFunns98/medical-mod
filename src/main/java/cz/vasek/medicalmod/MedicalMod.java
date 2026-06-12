package cz.vasek.medicalmod;

import com.mojang.logging.LogUtils;
import cz.vasek.medicalmod.registry.ModItems;
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
        modEventBus.addListener(this::addCreativeTabItems);

        LOGGER.info("Medical Mod is loading.");
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BANDAGE);
        }
    }
}
