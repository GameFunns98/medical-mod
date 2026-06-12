package cz.vasek.medicalmod.medical;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.Arrays;

public final class MedicalData implements IMedicalData {
    private static final String LEGACY_BLEEDING_LEVEL_KEY = "BleedingLevel";
    private static final String BLEEDING_LEVELS_KEY = "BleedingLevels";
    private static final String PAIN_KEY = "Pain";
    private static final String CONSCIOUSNESS_KEY = "Consciousness";
    private static final String PULSE_KEY = "Pulse";
    private static final String SYSTOLIC_KEY = "SystolicPressure";
    private static final String DIASTOLIC_KEY = "DiastolicPressure";
    private static final String SPO2_KEY = "OxygenSaturation";
    private static final String RESPIRATORY_RATE_KEY = "RespiratoryRate";
    private static final String TEMPERATURE_KEY = "TemperatureTenths";
    private static final String BLOOD_VOLUME_KEY = "BloodVolumeMl";
    private static final String AIRWAY_STATUS_KEY = "AirwayStatus";
    private static final String PNEUMOTHORAX_KEY = "Pneumothorax";
    private static final String CARDIAC_RHYTHM_KEY = "CardiacRhythm";
    private static final String IV_ACCESS_KEY = "IvAccess";
    private static final String INJURIES_KEY = "Injuries";
    private static final String FRACTURE_MASK_KEY = "FractureMask";
    private static final String EXAMINATION_MASK_KEY = "ExaminationMask";
    private static final String BODY_EXAMINATION_MASK_KEY = "BodyExaminationMask";

    private int pain;
    private int consciousnessLevel;
    private int pulse = 80;
    private int systolicPressure = 120;
    private int diastolicPressure = 80;
    private int oxygenSaturation = 98;
    private int respiratoryRate = 16;
    private int temperatureTenths = 370;
    private int bloodVolumeMl = 5000;
    private int airwayStatus;
    private boolean pneumothorax;
    private int cardiacRhythm;
    private boolean ivAccess;
    private final int[] injuries = new int[BodyPart.values().length];
    private final int[] bleedingLevels = new int[BodyPart.values().length];
    private int fractureMask;
    private int examinationMask;
    private int bodyExaminationMask;

    @Override
    public int getPain() {
        return pain;
    }

    @Override
    public void setPain(int pain) {
        this.pain = Mth.clamp(pain, 0, 10);
    }

    @Override
    public int getConsciousnessLevel() {
        return consciousnessLevel;
    }

    @Override
    public void setConsciousnessLevel(int level) {
        consciousnessLevel = Mth.clamp(level, 0, 3);
    }

    @Override
    public int getPulse() {
        return pulse;
    }

    @Override
    public void setPulse(int pulse) {
        this.pulse = Mth.clamp(pulse, 0, 250);
    }

    @Override
    public int getSystolicPressure() {
        return systolicPressure;
    }

    @Override
    public void setSystolicPressure(int pressure) {
        systolicPressure = Mth.clamp(pressure, 0, 250);
    }

    @Override
    public int getDiastolicPressure() {
        return diastolicPressure;
    }

    @Override
    public void setDiastolicPressure(int pressure) {
        diastolicPressure = Mth.clamp(pressure, 0, 180);
    }

    @Override
    public int getOxygenSaturation() {
        return oxygenSaturation;
    }

    @Override
    public void setOxygenSaturation(int saturation) {
        oxygenSaturation = Mth.clamp(saturation, 0, 100);
    }

    @Override
    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    @Override
    public void setRespiratoryRate(int rate) {
        respiratoryRate = Mth.clamp(rate, 0, 80);
    }

    @Override
    public int getTemperatureTenths() {
        return temperatureTenths;
    }

    @Override
    public void setTemperatureTenths(int temperatureTenths) {
        this.temperatureTenths = Mth.clamp(temperatureTenths, 250, 450);
    }

    @Override
    public int getBloodVolumeMl() {
        return bloodVolumeMl;
    }

    @Override
    public void setBloodVolumeMl(int bloodVolumeMl) {
        this.bloodVolumeMl = Mth.clamp(bloodVolumeMl, 0, 6000);
    }

    @Override
    public int getAirwayStatus() {
        return airwayStatus;
    }

    @Override
    public void setAirwayStatus(int status) {
        airwayStatus = Mth.clamp(status, 0, 2);
    }

    @Override
    public boolean hasPneumothorax() {
        return pneumothorax;
    }

    @Override
    public void setPneumothorax(boolean pneumothorax) {
        this.pneumothorax = pneumothorax;
    }

    @Override
    public int getCardiacRhythm() {
        return cardiacRhythm;
    }

    @Override
    public void setCardiacRhythm(int rhythm) {
        cardiacRhythm = Mth.clamp(rhythm, 0, 4);
    }

    @Override
    public boolean hasIvAccess() {
        return ivAccess;
    }

    @Override
    public void setIvAccess(boolean ivAccess) {
        this.ivAccess = ivAccess;
    }

    @Override
    public int getInjurySeverity(BodyPart bodyPart) {
        return injuries[bodyPart.ordinal()];
    }

    @Override
    public void setInjurySeverity(BodyPart bodyPart, int severity) {
        injuries[bodyPart.ordinal()] = Mth.clamp(severity, 0, 3);
    }

    @Override
    public int getBleedingLevel(BodyPart bodyPart) {
        return bleedingLevels[bodyPart.ordinal()];
    }

    @Override
    public void setBleedingLevel(BodyPart bodyPart, int level) {
        bleedingLevels[bodyPart.ordinal()] = Mth.clamp(level, 0, 3);
    }

    @Override
    public boolean hasFracture(BodyPart bodyPart) {
        return (fractureMask & (1 << bodyPart.ordinal())) != 0;
    }

    @Override
    public void setFracture(BodyPart bodyPart, boolean fractured) {
        int bit = 1 << bodyPart.ordinal();
        fractureMask = fractured ? fractureMask | bit : fractureMask & ~bit;
    }

    @Override
    public int getExaminationMask() {
        return examinationMask;
    }

    @Override
    public void setExaminationMask(int mask) {
        examinationMask = Math.max(0, mask);
    }

    @Override
    public int getBodyExaminationMask() {
        return bodyExaminationMask;
    }

    @Override
    public void setBodyExaminationMask(int mask) {
        int validMask = (1 << BodyPart.values().length) - 1;
        bodyExaminationMask = mask & validMask;
    }

    @Override
    public void copyFrom(IMedicalData source) {
        setPain(source.getPain());
        setConsciousnessLevel(source.getConsciousnessLevel());
        setPulse(source.getPulse());
        setSystolicPressure(source.getSystolicPressure());
        setDiastolicPressure(source.getDiastolicPressure());
        setOxygenSaturation(source.getOxygenSaturation());
        setRespiratoryRate(source.getRespiratoryRate());
        setTemperatureTenths(source.getTemperatureTenths());
        setBloodVolumeMl(source.getBloodVolumeMl());
        setAirwayStatus(source.getAirwayStatus());
        setPneumothorax(source.hasPneumothorax());
        setCardiacRhythm(source.getCardiacRhythm());
        setIvAccess(source.hasIvAccess());
        setExaminationMask(source.getExaminationMask());
        setBodyExaminationMask(source.getBodyExaminationMask());

        for (BodyPart bodyPart : BodyPart.values()) {
            setInjurySeverity(bodyPart, source.getInjurySeverity(bodyPart));
            setBleedingLevel(bodyPart, source.getBleedingLevel(bodyPart));
            setFracture(bodyPart, source.hasFracture(bodyPart));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(PAIN_KEY, pain);
        tag.putInt(CONSCIOUSNESS_KEY, consciousnessLevel);
        tag.putInt(PULSE_KEY, pulse);
        tag.putInt(SYSTOLIC_KEY, systolicPressure);
        tag.putInt(DIASTOLIC_KEY, diastolicPressure);
        tag.putInt(SPO2_KEY, oxygenSaturation);
        tag.putInt(RESPIRATORY_RATE_KEY, respiratoryRate);
        tag.putInt(TEMPERATURE_KEY, temperatureTenths);
        tag.putInt(BLOOD_VOLUME_KEY, bloodVolumeMl);
        tag.putInt(AIRWAY_STATUS_KEY, airwayStatus);
        tag.putBoolean(PNEUMOTHORAX_KEY, pneumothorax);
        tag.putInt(CARDIAC_RHYTHM_KEY, cardiacRhythm);
        tag.putBoolean(IV_ACCESS_KEY, ivAccess);
        tag.putIntArray(INJURIES_KEY, injuries);
        tag.putIntArray(BLEEDING_LEVELS_KEY, bleedingLevels);
        tag.putInt(FRACTURE_MASK_KEY, fractureMask);
        tag.putInt(EXAMINATION_MASK_KEY, examinationMask);
        tag.putInt(BODY_EXAMINATION_MASK_KEY, bodyExaminationMask);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(PAIN_KEY)) setPain(tag.getInt(PAIN_KEY));
        if (tag.contains(CONSCIOUSNESS_KEY)) setConsciousnessLevel(tag.getInt(CONSCIOUSNESS_KEY));
        if (tag.contains(PULSE_KEY)) setPulse(tag.getInt(PULSE_KEY));
        if (tag.contains(SYSTOLIC_KEY)) setSystolicPressure(tag.getInt(SYSTOLIC_KEY));
        if (tag.contains(DIASTOLIC_KEY)) setDiastolicPressure(tag.getInt(DIASTOLIC_KEY));
        if (tag.contains(SPO2_KEY)) setOxygenSaturation(tag.getInt(SPO2_KEY));
        if (tag.contains(RESPIRATORY_RATE_KEY)) setRespiratoryRate(tag.getInt(RESPIRATORY_RATE_KEY));
        if (tag.contains(TEMPERATURE_KEY)) setTemperatureTenths(tag.getInt(TEMPERATURE_KEY));
        if (tag.contains(BLOOD_VOLUME_KEY)) setBloodVolumeMl(tag.getInt(BLOOD_VOLUME_KEY));
        if (tag.contains(AIRWAY_STATUS_KEY)) setAirwayStatus(tag.getInt(AIRWAY_STATUS_KEY));
        if (tag.contains(PNEUMOTHORAX_KEY)) setPneumothorax(tag.getBoolean(PNEUMOTHORAX_KEY));
        if (tag.contains(CARDIAC_RHYTHM_KEY)) setCardiacRhythm(tag.getInt(CARDIAC_RHYTHM_KEY));
        if (tag.contains(IV_ACCESS_KEY)) setIvAccess(tag.getBoolean(IV_ACCESS_KEY));
        if (tag.contains(EXAMINATION_MASK_KEY)) setExaminationMask(tag.getInt(EXAMINATION_MASK_KEY));
        if (tag.contains(BODY_EXAMINATION_MASK_KEY)) {
            setBodyExaminationMask(tag.getInt(BODY_EXAMINATION_MASK_KEY));
        }

        loadArray(tag, INJURIES_KEY, injuries, 0, 3);
        loadArray(tag, BLEEDING_LEVELS_KEY, bleedingLevels, 0, 3);

        if (!tag.contains(BLEEDING_LEVELS_KEY) && tag.contains(LEGACY_BLEEDING_LEVEL_KEY)) {
            setBleedingLevel(BodyPart.TORSO, tag.getInt(LEGACY_BLEEDING_LEVEL_KEY));
        }

        if (tag.contains(FRACTURE_MASK_KEY)) {
            fractureMask = tag.getInt(FRACTURE_MASK_KEY);
        }
    }

    private static void loadArray(
            CompoundTag tag,
            String key,
            int[] target,
            int minimum,
            int maximum
    ) {
        Arrays.fill(target, 0);
        if (!tag.contains(key)) {
            return;
        }

        int[] loaded = tag.getIntArray(key);
        int length = Math.min(loaded.length, target.length);
        for (int index = 0; index < length; index++) {
            target[index] = Mth.clamp(loaded[index], minimum, maximum);
        }
    }
}
