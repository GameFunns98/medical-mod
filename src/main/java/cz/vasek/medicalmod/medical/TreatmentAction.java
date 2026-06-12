package cz.vasek.medicalmod.medical;

import java.util.Arrays;
import java.util.Optional;

public enum TreatmentAction {
    BANDAGE(0, "treatment.medicalmod.bandage"),
    TOURNIQUET(1, "treatment.medicalmod.tourniquet"),
    SPLINT(2, "treatment.medicalmod.splint"),
    OXYGEN(3, "treatment.medicalmod.oxygen"),
    MORPHINE(4, "treatment.medicalmod.morphine"),
    EPINEPHRINE(5, "treatment.medicalmod.epinephrine"),
    IV_FLUIDS(6, "treatment.medicalmod.iv_fluids"),
    CLEAR_AIRWAY(7, "treatment.medicalmod.clear_airway"),
    CHEST_SEAL(8, "treatment.medicalmod.chest_seal"),
    NEEDLE_DECOMPRESSION(9, "treatment.medicalmod.needle_decompression"),
    CPR(10, "treatment.medicalmod.cpr"),
    DEFIBRILLATION(11, "treatment.medicalmod.defibrillation");

    private final int id;
    private final String translationKey;

    TreatmentAction(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static Optional<TreatmentAction> byId(int id) {
        return Arrays.stream(values()).filter(action -> action.id == id).findFirst();
    }
}
