package cz.vasek.medicalmod.menu;

import cz.vasek.medicalmod.medical.BodyPart;
import cz.vasek.medicalmod.medical.IMedicalData;
import cz.vasek.medicalmod.medical.MedicalDataProvider;
import cz.vasek.medicalmod.medical.TreatmentAction;
import cz.vasek.medicalmod.registry.ModItems;
import cz.vasek.medicalmod.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeServerPlayer;

import java.util.Optional;

public final class MedicalMenu extends AbstractContainerMenu {
    private static final int BLEEDING_INDEX = 0;
    private static final int PAIN_INDEX = 1;
    private static final int CONSCIOUSNESS_INDEX = 2;
    private static final int PULSE_INDEX = 3;
    private static final int SYSTOLIC_INDEX = 4;
    private static final int DIASTOLIC_INDEX = 5;
    private static final int SPO2_INDEX = 6;
    private static final int RESPIRATORY_RATE_INDEX = 7;
    private static final int TEMPERATURE_INDEX = 8;
    private static final int BLOOD_VOLUME_INDEX = 9;
    private static final int AIRWAY_INDEX = 10;
    private static final int PNEUMOTHORAX_INDEX = 11;
    private static final int RHYTHM_INDEX = 12;
    private static final int IV_ACCESS_INDEX = 13;
    private static final int FRACTURE_MASK_INDEX = 14;
    private static final int INJURY_START_INDEX = 15;
    private static final int DATA_COUNT = INJURY_START_INDEX + BodyPart.values().length;

    private final Player targetPlayer;
    private final ContainerData medicalData;

    public MedicalMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                findTarget(inventory.player, buffer.readVarInt()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public MedicalMenu(int containerId, Inventory inventory, Player targetPlayer) {
        this(containerId, inventory, targetPlayer, createServerData(targetPlayer));
    }

    private MedicalMenu(
            int containerId,
            Inventory inventory,
            Player targetPlayer,
            ContainerData medicalData
    ) {
        super(ModMenus.MEDICAL_MENU.get(), containerId);
        this.targetPlayer = targetPlayer;
        this.medicalData = medicalData;

        checkContainerDataCount(medicalData, DATA_COUNT);
        addDataSlots(medicalData);
    }

    public static void open(ServerPlayer examiner, ServerPlayer target) {
        ((IForgeServerPlayer) examiner).openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, player) -> new MedicalMenu(containerId, inventory, target),
                        Component.translatable("menu.medicalmod.patient", target.getDisplayName())
                ),
                buffer -> buffer.writeVarInt(target.getId())
        );
    }

    private static Player findTarget(Player viewer, int entityId) {
        Entity entity = viewer.level().getEntity(entityId);
        return entity instanceof Player player ? player : viewer;
    }

    private static ContainerData createServerData(Player targetPlayer) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                Optional<IMedicalData> optional = targetPlayer
                        .getCapability(MedicalDataProvider.CAPABILITY)
                        .resolve();

                if (optional.isEmpty()) {
                    return 0;
                }

                IMedicalData data = optional.get();
                if (index >= INJURY_START_INDEX && index < DATA_COUNT) {
                    return data.getInjurySeverity(BodyPart.values()[index - INJURY_START_INDEX]);
                }

                return switch (index) {
                    case BLEEDING_INDEX -> data.getBleedingLevel();
                    case PAIN_INDEX -> data.getPain();
                    case CONSCIOUSNESS_INDEX -> data.getConsciousnessLevel();
                    case PULSE_INDEX -> data.getPulse();
                    case SYSTOLIC_INDEX -> data.getSystolicPressure();
                    case DIASTOLIC_INDEX -> data.getDiastolicPressure();
                    case SPO2_INDEX -> data.getOxygenSaturation();
                    case RESPIRATORY_RATE_INDEX -> data.getRespiratoryRate();
                    case TEMPERATURE_INDEX -> data.getTemperatureTenths();
                    case BLOOD_VOLUME_INDEX -> data.getBloodVolumeMl();
                    case AIRWAY_INDEX -> data.getAirwayStatus();
                    case PNEUMOTHORAX_INDEX -> data.hasPneumothorax() ? 1 : 0;
                    case RHYTHM_INDEX -> data.getCardiacRhythm();
                    case IV_ACCESS_INDEX -> data.hasIvAccess() ? 1 : 0;
                    case FRACTURE_MASK_INDEX -> getFractureMask(data);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // State changes are accepted only through validated server-side actions.
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private static int getFractureMask(IMedicalData data) {
        int mask = 0;
        for (BodyPart bodyPart : BodyPart.values()) {
            if (data.hasFracture(bodyPart)) {
                mask |= 1 << bodyPart.ordinal();
            }
        }
        return mask;
    }

    @Override
    public boolean clickMenuButton(Player examiner, int buttonId) {
        if (examiner.level().isClientSide || examiner.distanceToSqr(targetPlayer) > 64.0D) {
            return false;
        }

        Optional<TreatmentAction> action = TreatmentAction.byId(buttonId);
        Optional<IMedicalData> targetData = targetPlayer
                .getCapability(MedicalDataProvider.CAPABILITY)
                .resolve();

        if (action.isEmpty() || targetData.isEmpty()) {
            return false;
        }

        boolean success = applyTreatment(examiner, targetData.get(), action.get());
        if (success) {
            examiner.sendSystemMessage(Component.translatable(
                    "message.medicalmod.treatment_success",
                    Component.translatable(action.get().getTranslationKey()),
                    targetPlayer.getDisplayName()
            ));
        } else {
            examiner.sendSystemMessage(Component.translatable(
                    "message.medicalmod.treatment_failed",
                    Component.translatable(action.get().getTranslationKey())
            ));
        }
        return true;
    }

    private boolean applyTreatment(Player examiner, IMedicalData data, TreatmentAction action) {
        return switch (action) {
            case BANDAGE -> {
                if (!data.isBleeding() || !consumeItem(examiner, ModItems.BANDAGE.get())) {
                    yield false;
                }
                data.reduceBleeding(1);
                data.setPain(data.getPain() - 1);
                yield true;
            }
            case TOURNIQUET -> {
                if (!data.isBleeding() || !consumeItem(examiner, ModItems.TOURNIQUET.get())) {
                    yield false;
                }
                data.stopBleeding();
                data.setPain(data.getPain() + 1);
                yield true;
            }
            case SPLINT -> {
                BodyPart fracturedPart = firstFracturedPart(data);
                if (fracturedPart == null || !consumeItem(examiner, ModItems.SPLINT.get())) {
                    yield false;
                }
                data.setFracture(fracturedPart, false);
                data.setPain(data.getPain() - 2);
                yield true;
            }
            case OXYGEN -> {
                if (!consumeItem(examiner, ModItems.OXYGEN_MASK.get())) {
                    yield false;
                }
                data.setOxygenSaturation(data.getOxygenSaturation() + 12);
                data.setRespiratoryRate(Math.max(12, data.getRespiratoryRate() - 2));
                yield true;
            }
            case MORPHINE -> {
                if (!consumeItem(examiner, ModItems.MORPHINE.get())) {
                    yield false;
                }
                data.setPain(data.getPain() - 4);
                data.setRespiratoryRate(data.getRespiratoryRate() - 2);
                yield true;
            }
            case EPINEPHRINE -> {
                if (!consumeItem(examiner, ModItems.EPINEPHRINE.get())) {
                    yield false;
                }
                data.setPulse(data.getPulse() + 20);
                data.setSystolicPressure(data.getSystolicPressure() + 15);
                data.setConsciousnessLevel(data.getConsciousnessLevel() - 1);
                yield true;
            }
            case IV_FLUIDS -> {
                if (!consumeItem(examiner, ModItems.SALINE.get())) {
                    yield false;
                }
                data.setIvAccess(true);
                data.setBloodVolumeMl(data.getBloodVolumeMl() + 500);
                data.setSystolicPressure(data.getSystolicPressure() + 10);
                data.setDiastolicPressure(data.getDiastolicPressure() + 5);
                yield true;
            }
            case CLEAR_AIRWAY -> {
                if (data.getAirwayStatus() == 0) {
                    yield false;
                }
                data.setAirwayStatus(0);
                data.setOxygenSaturation(data.getOxygenSaturation() + 5);
                yield true;
            }
            case CHEST_SEAL -> {
                if (!consumeItem(examiner, ModItems.CHEST_SEAL.get())) {
                    yield false;
                }
                data.reduceBleeding(1);
                data.setInjurySeverity(
                        BodyPart.TORSO,
                        Math.max(0, data.getInjurySeverity(BodyPart.TORSO) - 1)
                );
                yield true;
            }
            case NEEDLE_DECOMPRESSION -> {
                if (!data.hasPneumothorax()
                        || !consumeItem(examiner, ModItems.DECOMPRESSION_NEEDLE.get())) {
                    yield false;
                }
                data.setPneumothorax(false);
                data.setOxygenSaturation(data.getOxygenSaturation() + 10);
                data.setRespiratoryRate(Math.max(12, data.getRespiratoryRate() - 4));
                yield true;
            }
            case CPR -> {
                if (data.getPulse() > 0 && data.getCardiacRhythm() != 4) {
                    yield false;
                }
                data.setPulse(35);
                data.setCardiacRhythm(1);
                data.setSystolicPressure(60);
                yield true;
            }
            case DEFIBRILLATION -> {
                if (data.getCardiacRhythm() != 3
                        || !hasItem(examiner, ModItems.DEFIBRILLATOR.get())) {
                    yield false;
                }
                data.setCardiacRhythm(0);
                data.setPulse(80);
                data.setSystolicPressure(110);
                data.setDiastolicPressure(70);
                yield true;
            }
        };
    }

    private static BodyPart firstFracturedPart(IMedicalData data) {
        for (BodyPart bodyPart : BodyPart.values()) {
            if (data.hasFracture(bodyPart)) {
                return bodyPart;
            }
        }
        return null;
    }

    private static boolean consumeItem(Player player, Item item) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean hasItem(Player player, Item item) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return targetPlayer.isAlive() && player.distanceToSqr(targetPlayer) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public Component getTargetName() {
        return targetPlayer.getDisplayName();
    }

    public int getBleedingLevel() {
        return medicalData.get(BLEEDING_INDEX);
    }

    public int getPain() {
        return medicalData.get(PAIN_INDEX);
    }

    public int getConsciousnessLevel() {
        return medicalData.get(CONSCIOUSNESS_INDEX);
    }

    public int getPulse() {
        return medicalData.get(PULSE_INDEX);
    }

    public int getSystolicPressure() {
        return medicalData.get(SYSTOLIC_INDEX);
    }

    public int getDiastolicPressure() {
        return medicalData.get(DIASTOLIC_INDEX);
    }

    public int getOxygenSaturation() {
        return medicalData.get(SPO2_INDEX);
    }

    public int getRespiratoryRate() {
        return medicalData.get(RESPIRATORY_RATE_INDEX);
    }

    public int getTemperatureTenths() {
        return medicalData.get(TEMPERATURE_INDEX);
    }

    public int getBloodVolumeMl() {
        return medicalData.get(BLOOD_VOLUME_INDEX);
    }

    public int getAirwayStatus() {
        return medicalData.get(AIRWAY_INDEX);
    }

    public boolean hasPneumothorax() {
        return medicalData.get(PNEUMOTHORAX_INDEX) != 0;
    }

    public int getCardiacRhythm() {
        return medicalData.get(RHYTHM_INDEX);
    }

    public boolean hasIvAccess() {
        return medicalData.get(IV_ACCESS_INDEX) != 0;
    }

    public boolean hasFracture(BodyPart bodyPart) {
        return (medicalData.get(FRACTURE_MASK_INDEX) & (1 << bodyPart.ordinal())) != 0;
    }

    public int getInjurySeverity(BodyPart bodyPart) {
        return medicalData.get(INJURY_START_INDEX + bodyPart.ordinal());
    }
}
