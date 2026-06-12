package cz.vasek.medicalmod.medical;

import java.util.Arrays;
import java.util.Optional;

public enum ExaminationAction {
    CONSCIOUSNESS(20, "examination.medicalmod.consciousness", 0, 40, false),
    PAIN(21, "examination.medicalmod.pain", 1, 40, false),
    PULSE(22, "examination.medicalmod.pulse", 2, 60, false),
    BLOOD_PRESSURE(23, "examination.medicalmod.blood_pressure", 3, 100, false),
    SPO2(24, "examination.medicalmod.spo2", 4, 80, false),
    TEMPERATURE(25, "examination.medicalmod.temperature", 5, 100, false),
    AIRWAY(26, "examination.medicalmod.airway", 6, 60, false),
    BREATHING(27, "examination.medicalmod.breathing", 7, 80, false),
    BODY_PART(28, "examination.medicalmod.body_part", -1, 80, true);

    private final int id;
    private final String translationKey;
    private final int maskIndex;
    private final int durationTicks;
    private final boolean bodySpecific;

    ExaminationAction(
            int id,
            String translationKey,
            int maskIndex,
            int durationTicks,
            boolean bodySpecific
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.maskIndex = maskIndex;
        this.durationTicks = durationTicks;
        this.bodySpecific = bodySpecific;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getMask() {
        return maskIndex < 0 ? 0 : 1 << maskIndex;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public boolean isBodySpecific() {
        return bodySpecific;
    }

    public static Optional<ExaminationAction> byId(int id) {
        return Arrays.stream(values()).filter(action -> action.id == id).findFirst();
    }
}
