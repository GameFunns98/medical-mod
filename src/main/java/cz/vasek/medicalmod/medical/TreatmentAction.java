package cz.vasek.medicalmod.medical;

import java.util.Arrays;
import java.util.Optional;

public enum TreatmentAction {
    BANDAGE(0, "treatment.medicalmod.bandage", 100),
    TOURNIQUET(1, "treatment.medicalmod.tourniquet", 80),
    SPLINT(2, "treatment.medicalmod.splint", 160),
    OXYGEN(3, "treatment.medicalmod.oxygen", 80),
    MORPHINE(4, "treatment.medicalmod.morphine", 80),
    EPINEPHRINE(5, "treatment.medicalmod.epinephrine", 80),
    IV_FLUIDS(6, "treatment.medicalmod.iv_fluids", 200),
    CLEAR_AIRWAY(7, "treatment.medicalmod.clear_airway", 80),
    CHEST_SEAL(8, "treatment.medicalmod.chest_seal", 120),
    NEEDLE_DECOMPRESSION(9, "treatment.medicalmod.needle_decompression", 120),
    CPR(10, "treatment.medicalmod.cpr", 100),
    DEFIBRILLATION(11, "treatment.medicalmod.defibrillation", 120);

    private final int id;
    private final String translationKey;
    private final int durationTicks;

    TreatmentAction(int id, String translationKey, int durationTicks) {
        this.id = id;
        this.translationKey = translationKey;
        this.durationTicks = durationTicks;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public static Optional<TreatmentAction> byId(int id) {
        return Arrays.stream(values()).filter(action -> action.id == id).findFirst();
    }
}
