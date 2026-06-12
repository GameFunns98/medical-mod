package cz.vasek.medicalmod.medical;

public enum BodyPart {
    HEAD("body_part.medicalmod.head"),
    TORSO("body_part.medicalmod.torso"),
    LEFT_ARM("body_part.medicalmod.left_arm"),
    RIGHT_ARM("body_part.medicalmod.right_arm"),
    LEFT_LEG("body_part.medicalmod.left_leg"),
    RIGHT_LEG("body_part.medicalmod.right_leg");

    private final String translationKey;

    BodyPart(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
