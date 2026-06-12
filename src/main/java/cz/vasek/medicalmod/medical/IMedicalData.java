package cz.vasek.medicalmod.medical;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMedicalData {
    int getBleedingLevel();

    void setBleedingLevel(int level);

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
