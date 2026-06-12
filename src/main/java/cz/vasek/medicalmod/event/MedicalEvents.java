package cz.vasek.medicalmod.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.medical.BodyPart;
import cz.vasek.medicalmod.medical.IMedicalData;
import cz.vasek.medicalmod.medical.MedicalDataProvider;
import cz.vasek.medicalmod.menu.MedicalMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MedicalMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MedicalEvents {
    private static final ResourceLocation MEDICAL_DATA_ID =
            new ResourceLocation(MedicalMod.MOD_ID, "medical_data");
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

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
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide
                || player.isCreative()
                || player.isSpectator()
                || INTERNAL_DAMAGE.contains(player.getUUID())) {
            return;
        }

        float damage = event.getAmount();
        if (damage < 1.0F) {
            return;
        }

        player.getCapability(MedicalDataProvider.CAPABILITY).ifPresent(data -> {
            BodyPart bodyPart = selectBodyPart(player, event);
            int severity = Mth.clamp(Mth.ceil(damage / 3.0F), 1, 3);

            data.setInjurySeverity(
                    bodyPart,
                    Math.max(data.getInjurySeverity(bodyPart), severity)
            );
            data.setPain(data.getPain() + severity);

            if (damage >= 2.0F) {
                data.setBleedingLevel(Math.max(
                        data.getBleedingLevel(),
                        Mth.clamp(Mth.ceil(damage / 5.0F), 1, 3)
                ));
            }

            boolean hardFall = event.getSource().is(DamageTypes.FALL) && damage >= 4.0F;
            if (hardFall || damage >= 8.0F) {
                data.setFracture(bodyPart, true);
            }

            if (bodyPart == BodyPart.TORSO && damage >= 7.0F) {
                data.setPneumothorax(true);
            }
        });
    }

    private static BodyPart selectBodyPart(Player player, LivingDamageEvent event) {
        if (event.getSource().is(DamageTypes.FALL)) {
            return player.getRandom().nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }

        BodyPart[] bodyParts = BodyPart.values();
        return bodyParts[player.getRandom().nextInt(bodyParts.length)];
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
                INTERNAL_DAMAGE.add(player.getUUID());
                try {
                    player.hurt(player.damageSources().generic(), 1.0F);
                } finally {
                    INTERNAL_DAMAGE.remove(player.getUUID());
                }

                player.displayClientMessage(
                        Component.translatable("message.medicalmod.bleeding_damage", bleedingLevel),
                        true
                );
            }

            if (player.tickCount % 20 == 0) {
                updatePhysiology(data);
            }
        });
    }

    private static void updatePhysiology(IMedicalData data) {
        int bleedingLevel = data.getBleedingLevel();
        if (bleedingLevel > 0) {
            data.setBloodVolumeMl(data.getBloodVolumeMl() - bleedingLevel * 8);
        }

        int bloodDeficit = Math.max(0, 5000 - data.getBloodVolumeMl());
        int calculatedPulse = 80 + data.getPain() * 2 + bloodDeficit / 55;
        int calculatedSystolic = 120 - bloodDeficit / 45;
        int calculatedDiastolic = 80 - bloodDeficit / 75;

        if (data.getCardiacRhythm() == 0 || data.getCardiacRhythm() == 1 || data.getCardiacRhythm() == 2) {
            data.setPulse(calculatedPulse);
            data.setSystolicPressure(calculatedSystolic);
            data.setDiastolicPressure(calculatedDiastolic);
            data.setCardiacRhythm(calculatedPulse < 55 ? 1 : calculatedPulse > 110 ? 2 : 0);
        }

        if (data.hasPneumothorax()) {
            data.setOxygenSaturation(data.getOxygenSaturation() - 1);
            data.setRespiratoryRate(data.getRespiratoryRate() + 1);
        }

        if (data.getAirwayStatus() > 0) {
            data.setOxygenSaturation(data.getOxygenSaturation() - data.getAirwayStatus());
        }

        if (data.getBloodVolumeMl() <= 1000 || data.getOxygenSaturation() <= 55) {
            data.setPulse(0);
            data.setCardiacRhythm(4);
            data.setSystolicPressure(0);
            data.setDiastolicPressure(0);
            data.setConsciousnessLevel(3);
        } else if (data.getBloodVolumeMl() < 2200 || data.getOxygenSaturation() < 75) {
            data.setConsciousnessLevel(3);
        } else if (data.getBloodVolumeMl() < 3200 || data.getOxygenSaturation() < 86) {
            data.setConsciousnessLevel(2);
        } else if (data.getSystolicPressure() < 90) {
            data.setConsciousnessLevel(1);
        } else {
            data.setConsciousnessLevel(0);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("medical")
                        .then(Commands.literal("gui")
                                .executes(context -> openGui(context.getSource())))
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

    private static int openGui(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MedicalMenu.open(player, player);
        return 1;
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
                        data.get().getBleedingLevel(),
                        data.get().getPulse(),
                        data.get().getSystolicPressure(),
                        data.get().getDiastolicPressure(),
                        data.get().getOxygenSaturation()
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
        Optional<IMedicalData> optional = player.getCapability(MedicalDataProvider.CAPABILITY).resolve();

        if (optional.isEmpty()) {
            source.sendFailure(Component.translatable("command.medicalmod.data_missing"));
            return 0;
        }

        resetMedicalData(optional.get());
        source.sendSuccess(
                () -> Component.translatable("command.medicalmod.cleared"),
                false
        );
        return 1;
    }

    private static void resetMedicalData(IMedicalData data) {
        data.setBleedingLevel(0);
        data.setPain(0);
        data.setConsciousnessLevel(0);
        data.setPulse(80);
        data.setSystolicPressure(120);
        data.setDiastolicPressure(80);
        data.setOxygenSaturation(98);
        data.setRespiratoryRate(16);
        data.setTemperatureTenths(370);
        data.setBloodVolumeMl(5000);
        data.setAirwayStatus(0);
        data.setPneumothorax(false);
        data.setCardiacRhythm(0);
        data.setIvAccess(false);

        for (BodyPart bodyPart : BodyPart.values()) {
            data.setInjurySeverity(bodyPart, 0);
            data.setFracture(bodyPart, false);
        }
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
