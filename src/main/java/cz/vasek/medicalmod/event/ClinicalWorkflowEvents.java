package cz.vasek.medicalmod.event;

import cz.vasek.medicalmod.MedicalMod;
import cz.vasek.medicalmod.medical.BodyPart;
import cz.vasek.medicalmod.medical.IMedicalData;
import cz.vasek.medicalmod.medical.MedicalDataProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MedicalMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClinicalWorkflowEvents {
    private static final Map<UUID, DamageSnapshot> DAMAGE_SNAPSHOTS = new HashMap<>();

    private ClinicalWorkflowEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void captureStateBeforeDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide
                || player.isCreative()
                || player.isSpectator()) {
            return;
        }

        player.getCapability(MedicalDataProvider.CAPABILITY).ifPresent(data -> {
            int[] injuries = new int[BodyPart.values().length];
            int fractureMask = 0;

            for (BodyPart bodyPart : BodyPart.values()) {
                injuries[bodyPart.ordinal()] = data.getInjurySeverity(bodyPart);
                if (data.hasFracture(bodyPart)) {
                    fractureMask |= 1 << bodyPart.ordinal();
                }
            }

            DAMAGE_SNAPSHOTS.put(
                    player.getUUID(),
                    new DamageSnapshot(
                            injuries,
                            fractureMask,
                            data.getBleedingLevel(BodyPart.TORSO)
                    )
            );
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void correctBodySpecificDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().isClientSide) {
            return;
        }

        DamageSnapshot snapshot = DAMAGE_SNAPSHOTS.remove(player.getUUID());
        if (snapshot == null || event.isCanceled() || event.getAmount() < 1.0F) {
            return;
        }

        player.getCapability(MedicalDataProvider.CAPABILITY).ifPresent(data -> {
            BodyPart injuredPart = findChangedBodyPart(data, snapshot);

            if (injuredPart == null && event.getAmount() < 2.0F) {
                return;
            }

            if (injuredPart == null) {
                injuredPart = fallbackBodyPart(player, event);
            }

            if (event.getAmount() >= 2.0F) {
                int bleedingLevel = Mth.clamp(
                        Mth.ceil(event.getAmount() / 5.0F),
                        1,
                        3
                );

                data.setBleedingLevel(
                        BodyPart.TORSO,
                        snapshot.torsoBleedingLevel()
                );
                data.setBleedingLevel(
                        injuredPart,
                        Math.max(
                                data.getBleedingLevel(injuredPart),
                                bleedingLevel
                        )
                );
            }

            data.setBodyExaminationMask(
                    data.getBodyExaminationMask()
                            & ~(1 << injuredPart.ordinal())
            );
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyCombinedBloodLoss(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide
                || event.player.tickCount % 20 != 0) {
            return;
        }

        Player player = event.player;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getCapability(MedicalDataProvider.CAPABILITY).ifPresent(data -> {
            int additionalBleeding =
                    data.getTotalBleedingLevel() - data.getBleedingLevel();

            if (additionalBleeding > 0) {
                data.setBloodVolumeMl(
                        data.getBloodVolumeMl() - additionalBleeding * 8
                );
            }
        });
    }

    private static BodyPart findChangedBodyPart(
            IMedicalData data,
            DamageSnapshot snapshot
    ) {
        BodyPart bestMatch = null;
        int largestChange = 0;

        for (BodyPart bodyPart : BodyPart.values()) {
            int injuryChange = data.getInjurySeverity(bodyPart)
                    - snapshot.injuries()[bodyPart.ordinal()];
            boolean newFracture = data.hasFracture(bodyPart)
                    && (snapshot.fractureMask()
                    & (1 << bodyPart.ordinal())) == 0;

            int score = injuryChange * 2 + (newFracture ? 1 : 0);
            if (score > largestChange) {
                largestChange = score;
                bestMatch = bodyPart;
            }
        }

        return bestMatch;
    }

    private static BodyPart fallbackBodyPart(
            Player player,
            LivingDamageEvent event
    ) {
        if (event.getSource().is(DamageTypes.FALL)) {
            return player.getRandom().nextBoolean()
                    ? BodyPart.LEFT_LEG
                    : BodyPart.RIGHT_LEG;
        }
        return BodyPart.TORSO;
    }

    private record DamageSnapshot(
            int[] injuries,
            int fractureMask,
            int torsoBleedingLevel
    ) {
    }
}
