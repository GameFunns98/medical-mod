package cz.vasek.medicalmod.medical;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMedicalData {
    int getBleedingLevel();

    void setBleedingLevel(int level);

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

    boolean hasFracture(BodyPart bodyPart);

    void setFracture(BodyPart bodyPart, boolean fractured);

    default boolean isBleeding() {
        return getBleedingLevel() > 0;
    }

    default void reduceBleeding(int amount) {
        setBleedingLevel(getBleedingLevel() - Math.max(0, amount));
    }

    default void stopBleeding() {
        setBleedingLevel(0);
    }

    void copyFrom(IMedicalData source);
}
