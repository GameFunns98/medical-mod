package cz.vasek.medicalmod.medical;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMedicalData {
    int getPain();

    void setPain(int pain);

    int getConsciousnessLevel();

    void setConsciousnessLevel(int level);

    int getPulse();

    void setPulse(int pulse);

    int getSystolicPressure();

    void setSystolicPressure(int pressure);

    int getDiastolicPressure();

    void setDiastolicPressure(int pressure);

    int getOxygenSaturation();

    void setOxygenSaturation(int saturation);

    int getRespiratoryRate();

    void setRespiratoryRate(int rate);

    int getTemperatureTenths();

    void setTemperatureTenths(int temperatureTenths);

    int getBloodVolumeMl();

    void setBloodVolumeMl(int bloodVolumeMl);

    int getAirwayStatus();

    void setAirwayStatus(int status);

    boolean hasPneumothorax();

    void setPneumothorax(boolean pneumothorax);

    int getCardiacRhythm();

    void setCardiacRhythm(int rhythm);

    boolean hasIvAccess();

    void setIvAccess(boolean ivAccess);

    int getInjurySeverity(BodyPart bodyPart);

    void setInjurySeverity(BodyPart bodyPart, int severity);

    int getBleedingLevel(BodyPart bodyPart);

    void setBleedingLevel(BodyPart bodyPart, int level);

    boolean hasFracture(BodyPart bodyPart);

    void setFracture(BodyPart bodyPart, boolean fractured);

    int getExaminationMask();

    void setExaminationMask(int mask);

    int getBodyExaminationMask();

    void setBodyExaminationMask(int mask);

    default int getBleedingLevel() {
        int maximum = 0;
        for (BodyPart bodyPart : BodyPart.values()) {
            maximum = Math.max(maximum, getBleedingLevel(bodyPart));
        }
        return maximum;
    }

    default int getTotalBleedingLevel() {
        int total = 0;
        for (BodyPart bodyPart : BodyPart.values()) {
            total += getBleedingLevel(bodyPart);
        }
        return total;
    }

    default void setBleedingLevel(int level) {
        if (level <= 0) {
            stopBleeding();
            return;
        }
        setBleedingLevel(BodyPart.TORSO, level);
    }

    default boolean isBleeding() {
        return getTotalBleedingLevel() > 0;
    }

    default void reduceBleeding(BodyPart bodyPart, int amount) {
        setBleedingLevel(bodyPart, getBleedingLevel(bodyPart) - Math.max(0, amount));
    }

    default void reduceBleeding(int amount) {
        BodyPart worstPart = null;
        int worstLevel = 0;

        for (BodyPart bodyPart : BodyPart.values()) {
            int level = getBleedingLevel(bodyPart);
            if (level > worstLevel) {
                worstLevel = level;
                worstPart = bodyPart;
            }
        }

        if (worstPart != null) {
            reduceBleeding(worstPart, amount);
        }
    }

    default void stopBleeding(BodyPart bodyPart) {
        setBleedingLevel(bodyPart, 0);
    }

    default void stopBleeding() {
        for (BodyPart bodyPart : BodyPart.values()) {
            stopBleeding(bodyPart);
        }
    }

    default boolean isExamined(ExaminationAction action) {
        return !action.isBodySpecific() && (getExaminationMask() & action.getMask()) != 0;
    }

    default void markExamined(ExaminationAction action) {
        if (!action.isBodySpecific()) {
            setExaminationMask(getExaminationMask() | action.getMask());
        }
    }

    default boolean isBodyPartExamined(BodyPart bodyPart) {
        return (getBodyExaminationMask() & (1 << bodyPart.ordinal())) != 0;
    }

    default void markBodyPartExamined(BodyPart bodyPart) {
        setBodyExaminationMask(getBodyExaminationMask() | (1 << bodyPart.ordinal()));
    }

    default void clearExaminations() {
        setExaminationMask(0);
        setBodyExaminationMask(0);
    }

    void copyFrom(IMedicalData source);
}
