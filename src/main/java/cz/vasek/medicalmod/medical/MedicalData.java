package cz.vasek.medicalmod.medical;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public final class MedicalData implements IMedicalData {
    private static final String BLEEDING_LEVEL_KEY = "BleedingLevel";

    private int bleedingLevel;

    @Override
    public int getBleedingLevel() {
        return bleedingLevel;
    }

    @Override
    public void setBleedingLevel(int level) {
        bleedingLevel = Mth.clamp(level, 0, 3);
    }

    @Override
    public void copyFrom(IMedicalData source) {
        setBleedingLevel(source.getBleedingLevel());
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(BLEEDING_LEVEL_KEY, bleedingLevel);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        setBleedingLevel(tag.getInt(BLEEDING_LEVEL_KEY));
    }
}
