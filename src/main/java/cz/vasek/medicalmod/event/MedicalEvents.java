package cz.vasek.medicalmod.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.medical.IMedicalData;
import cz.vasek.medicalmod.medical.MedicalDataProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = MedicalMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MedicalEvents {
    private static final ResourceLocation MEDICAL_DATA_ID =
            new ResourceLocation(MedicalMod.MOD_ID, "medical_data");

    private MedicalEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }

        MedicalDataProvider provider = new MedicalDataProvider();
        event.addCapability(MEDICAL_DATA_ID, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        if (!event.isWasDeath()) {
            event.getOriginal().getCapability(MedicalDataProvider.CAPABILITY).ifPresent(oldData ->
                    event.getEntity().getCapability(MedicalDataProvider.CAPABILITY).ifPresent(newData ->
                            newData.copyFrom(oldData)
                    )
            );
        }

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getCapability(MedicalDataProvider.CAPABILITY).ifPresent(data -> {
            int bleedingLevel = data.getBleedingLevel();
            int interval = getDamageInterval(bleedingLevel);

            if (interval > 0 && player.tickCount % interval == 0) {
                player.hurt(player.damageSources().generic(), 1.0F);
                player.displayClientMessage(
                        Component.translatable("message.medicalmod.bleeding_damage", bleedingLevel),
                        true
                );
            }
        });
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("medical")
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context.getSource())))
                        .then(Commands.literal("bleed")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> setBleeding(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "level")
                                        ))))
                        .then(Commands.literal("clear")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> clearMedicalData(context.getSource())))
        );
    }

    private static int showStatus(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<IMedicalData> data = player.getCapability(MedicalDataProvider.CAPABILITY).resolve();

        if (data.isEmpty()) {
            source.sendFailure(Component.translatable("command.medicalmod.data_missing"));
            return 0;
        }

        source.sendSuccess(
                () -> Component.translatable(
                        "command.medicalmod.status",
                        data.get().getBleedingLevel()
                ),
                false
        );
        return 1;
    }

    private static int setBleeding(CommandSourceStack source, int level) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<IMedicalData> data = player.getCapability(MedicalDataProvider.CAPABILITY).resolve();

        if (data.isEmpty()) {
            source.sendFailure(Component.translatable("command.medicalmod.data_missing"));
            return 0;
        }

        data.get().setBleedingLevel(level);
        source.sendSuccess(
                () -> Component.translatable("command.medicalmod.bleeding_set", level),
                false
        );
        return 1;
    }

    private static int clearMedicalData(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<IMedicalData> data = player.getCapability(MedicalDataProvider.CAPABILITY).resolve();

        if (data.isEmpty()) {
            source.sendFailure(Component.translatable("command.medicalmod.data_missing"));
            return 0;
        }

        data.get().stopBleeding();
        source.sendSuccess(
                () -> Component.translatable("command.medicalmod.cleared"),
                false
        );
        return 1;
    }

    private static int getDamageInterval(int bleedingLevel) {
        return switch (bleedingLevel) {
            case 1 -> 200;
            case 2 -> 100;
            case 3 -> 40;
            default -> -1;
        };
    }
}
