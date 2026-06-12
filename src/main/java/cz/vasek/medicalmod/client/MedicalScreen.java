package cz.vasek.medicalmod.client;

import cz.vasek.medicalmod.medical.BodyPart;
import cz.vasek.medicalmod.medical.ExaminationAction;
import cz.vasek.medicalmod.medical.TreatmentAction;
import cz.vasek.medicalmod.menu.MedicalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MedicalScreen extends AbstractContainerScreen<MedicalMenu> {
    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_EXAMINATION = 1;
    private static final int TAB_INJURIES = 2;
    private static final int TAB_TREATMENT = 3;

    private final List<Button> examinationButtons = new ArrayList<>();
    private final List<Button> treatmentButtons = new ArrayList<>();
    private final List<Button> bodyPartButtons = new ArrayList<>();
    private int activeTab = TAB_OVERVIEW;

    public MedicalScreen(MedicalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 360;
        imageHeight = 280;
    }

    @Override
    protected void init() {
        super.init();

        addTabButton("menu.medicalmod.tab.overview", TAB_OVERVIEW, 8);
        addTabButton("menu.medicalmod.tab.examination", TAB_EXAMINATION, 94);
        addTabButton("menu.medicalmod.tab.injuries", TAB_INJURIES, 180);
        addTabButton("menu.medicalmod.tab.treatment", TAB_TREATMENT, 266);

        BodyPart[] bodyParts = BodyPart.values();
        for (int index = 0; index < bodyParts.length; index++) {
            BodyPart bodyPart = bodyParts[index];
            int column = index % 3;
            int row = index / 3;

            Button button = Button.builder(
                    Component.translatable(bodyPart.getTranslationKey()),
                    ignored -> selectBodyPart(bodyPart)
            ).bounds(
                    leftPos + 10 + column * 115,
                    topPos + 54 + row * 24,
                    110,
                    20
            ).build();

            bodyPartButtons.add(addRenderableWidget(button));
        }

        ExaminationAction[] examinations = ExaminationAction.values();
        for (int index = 0; index < examinations.length; index++) {
            ExaminationAction action = examinations[index];
            int column = index % 3;
            int row = index / 3;

            Button button = Button.builder(
                    Component.translatable(action.getTranslationKey()),
                    ignored -> sendExamination(action)
            ).bounds(
                    leftPos + 10 + column * 115,
                    topPos + 116 + row * 30,
                    110,
                    24
            ).build();

            examinationButtons.add(addRenderableWidget(button));
        }

        TreatmentAction[] treatments = TreatmentAction.values();
        for (int index = 0; index < treatments.length; index++) {
            TreatmentAction action = treatments[index];
            int column = index % 3;
            int row = index / 3;

            Button button = Button.builder(
                    Component.translatable(action.getTranslationKey()),
                    ignored -> sendTreatment(action)
            ).bounds(
                    leftPos + 10 + column * 115,
                    topPos + 116 + row * 30,
                    110,
                    24
            ).build();

            treatmentButtons.add(addRenderableWidget(button));
        }

        updateButtonVisibility();
    }

    private void addTabButton(String translationKey, int tab, int xOffset) {
        addRenderableWidget(Button.builder(
                Component.translatable(translationKey),
                ignored -> setActiveTab(tab)
        ).bounds(leftPos + xOffset, topPos + 24, 82, 20).build());
    }

    private void setActiveTab(int tab) {
        activeTab = tab;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean examinationVisible = activeTab == TAB_EXAMINATION;
        boolean treatmentVisible = activeTab == TAB_TREATMENT;
        boolean bodySelectionVisible = examinationVisible || treatmentVisible;

        examinationButtons.forEach(button -> button.visible = examinationVisible);
        treatmentButtons.forEach(button -> button.visible = treatmentVisible);
        bodyPartButtons.forEach(button -> button.visible = bodySelectionVisible);
    }

    private void selectBodyPart(BodyPart bodyPart) {
        menu.selectBodyPartLocally(bodyPart);
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    MedicalMenu.getBodyPartButtonId(bodyPart)
            );
        }
    }

    private void sendExamination(ExaminationAction action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    action.getId()
            );
        }
    }

    private void sendTreatment(TreatmentAction action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    action.getId()
            );
        }
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = leftPos;
        int top = topPos;

        guiGraphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                0xF0181D22
        );
        guiGraphics.fill(
                left + 1,
                top + 1,
                left + imageWidth - 1,
                top + 20,
                0xFF28323C
        );
        guiGraphics.fill(left, top, left + imageWidth, top + 1, 0xFFBFC7CE);
        guiGraphics.fill(
                left,
                top + imageHeight - 1,
                left + imageWidth,
                top + imageHeight,
                0xFFBFC7CE
        );
        guiGraphics.fill(left, top, left + 1, top + imageHeight, 0xFFBFC7CE);
        guiGraphics.fill(
                left + imageWidth - 1,
                top,
                left + imageWidth,
                top + imageHeight,
                0xFFBFC7CE
        );

        if (activeTab == TAB_OVERVIEW || activeTab == TAB_INJURIES) {
            guiGraphics.fill(
                    left + 8,
                    top + 50,
                    left + imageWidth - 8,
                    top + imageHeight - 8,
                    0xCC101418
            );
        }
    }

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.drawString(
                font,
                Component.translatable(
                        "menu.medicalmod.patient",
                        menu.getTargetName()
                ),
                8,
                7,
                0xFFFFFF,
                false
        );

        switch (activeTab) {
            case TAB_EXAMINATION -> renderExamination(guiGraphics);
            case TAB_INJURIES -> renderInjuries(guiGraphics);
            case TAB_TREATMENT -> renderTreatment(guiGraphics);
            default -> renderOverview(guiGraphics);
        }
    }

    private void renderOverview(GuiGraphics guiGraphics) {
        int leftX = 16;
        int rightX = 190;
        int y = 60;
        int rowHeight = 20;

        drawSectionTitle(
                guiGraphics,
                "menu.medicalmod.section.vitals",
                leftX,
                y
        );
        y += 20;

        drawValue(
                guiGraphics,
                "medicalmod.vital.pulse",
                measured(
                        ExaminationAction.PULSE,
                        Component.literal(menu.getPulse() + " /min")
                ),
                leftX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.vital.pressure",
                measured(
                        ExaminationAction.BLOOD_PRESSURE,
                        Component.literal(
                                menu.getSystolicPressure()
                                        + "/"
                                        + menu.getDiastolicPressure()
                                        + " mmHg"
                        )
                ),
                leftX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.vital.spo2",
                measured(
                        ExaminationAction.SPO2,
                        Component.literal(menu.getOxygenSaturation() + " %")
                ),
                leftX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.vital.respiratory_rate",
                measured(
                        ExaminationAction.BREATHING,
                        Component.literal(
                                menu.getRespiratoryRate() + " /min"
                        )
                ),
                leftX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.vital.temperature",
                measured(
                        ExaminationAction.TEMPERATURE,
                        Component.literal(String.format(
                                Locale.ROOT,
                                "%.1f °C",
                                menu.getTemperatureTenths() / 10.0D
                        ))
                ),
                leftX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.vital.blood_loss",
                hasAnyBodyExamination()
                        ? bloodLossDescription()
                        : unmeasured(),
                leftX,
                y
        );

        y = 60;
        drawSectionTitle(
                guiGraphics,
                "menu.medicalmod.section.status",
                rightX,
                y
        );
        y += 20;

        drawValue(
                guiGraphics,
                "medicalmod.status.bleeding",
                hasAnyBodyExamination()
                        ? Component.literal(
                                Integer.toString(menu.getMaximumBleedingLevel())
                        )
                        : unmeasured(),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.pain",
                measured(
                        ExaminationAction.PAIN,
                        Component.literal(menu.getPain() + "/10")
                ),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.consciousness",
                measured(
                        ExaminationAction.CONSCIOUSNESS,
                        Component.translatable(
                                "medicalmod.consciousness."
                                        + menu.getConsciousnessLevel()
                        )
                ),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.airway",
                measured(
                        ExaminationAction.AIRWAY,
                        Component.translatable(
                                "medicalmod.airway."
                                        + menu.getAirwayStatus()
                        )
                ),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.pneumothorax",
                measured(
                        ExaminationAction.BREATHING,
                        yesNo(menu.hasPneumothorax())
                ),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.rhythm",
                measured(
                        ExaminationAction.PULSE,
                        Component.translatable(
                                "medicalmod.rhythm."
                                        + menu.getCardiacRhythm()
                        )
                ),
                rightX,
                y
        );
        y += rowHeight;
        drawValue(
                guiGraphics,
                "medicalmod.status.iv_access",
                yesNo(menu.hasIvAccess()),
                rightX,
                y
        );
    }

    private void renderExamination(GuiGraphics guiGraphics) {
        drawSelectedBodyPart(guiGraphics);
    }

    private void renderInjuries(GuiGraphics guiGraphics) {
        int y = 60;
        drawSectionTitle(
                guiGraphics,
                "menu.medicalmod.section.body_map",
                16,
                y
        );
        y += 24;

        for (BodyPart bodyPart : BodyPart.values()) {
            guiGraphics.drawString(
                    font,
                    Component.translatable(bodyPart.getTranslationKey()),
                    18,
                    y,
                    0xFFFFFF,
                    false
            );

            if (!menu.isBodyPartExamined(bodyPart)) {
                guiGraphics.drawString(
                        font,
                        unmeasured(),
                        125,
                        y,
                        0x9AA7B2,
                        false
                );
            } else {
                Component severity = Component.translatable(
                        "medicalmod.injury.severity."
                                + menu.getInjurySeverity(bodyPart)
                );
                Component bleeding = Component.translatable(
                        "medicalmod.bleeding.level",
                        menu.getBleedingLevel(bodyPart)
                );
                Component fracture = menu.hasFracture(bodyPart)
                        ? Component.translatable("medicalmod.fracture.yes")
                        : Component.translatable("medicalmod.fracture.no");

                guiGraphics.drawString(
                        font,
                        severity,
                        125,
                        y,
                        0xFFD37A,
                        false
                );
                guiGraphics.drawString(
                        font,
                        bleeding,
                        225,
                        y,
                        0xE57373,
                        false
                );
                guiGraphics.drawString(
                        font,
                        fracture,
                        280,
                        y,
                        0xD7E1E8,
                        false
                );
            }

            y += 26;
        }
    }

    private void renderTreatment(GuiGraphics guiGraphics) {
        drawSelectedBodyPart(guiGraphics);
    }

    private void drawSelectedBodyPart(GuiGraphics guiGraphics) {
        Component selected = Component.translatable(
                "menu.medicalmod.selected_body_part",
                Component.translatable(
                        menu.getSelectedBodyPart().getTranslationKey()
                )
        );
        guiGraphics.drawString(
                font,
                selected,
                12,
                103,
                0x6FD4FF,
                false
        );
    }

    private Component measured(
            ExaminationAction action,
            Component measuredValue
    ) {
        return menu.isExamined(action) ? measuredValue : unmeasured();
    }

    private Component unmeasured() {
        return Component.translatable("medicalmod.unmeasured");
    }

    private boolean hasAnyBodyExamination() {
        for (BodyPart bodyPart : BodyPart.values()) {
            if (menu.isBodyPartExamined(bodyPart)) {
                return true;
            }
        }
        return false;
    }

    private Component bloodLossDescription() {
        int bloodLoss = Math.max(0, 5000 - menu.getBloodVolumeMl());
        int level;

        if (bloodLoss < 500) {
            level = 0;
        } else if (bloodLoss < 1000) {
            level = 1;
        } else if (bloodLoss < 2000) {
            level = 2;
        } else {
            level = 3;
        }

        return Component.translatable("medicalmod.blood_loss." + level);
    }

    private void drawSectionTitle(
            GuiGraphics guiGraphics,
            String translationKey,
            int x,
            int y
    ) {
        guiGraphics.drawString(
                font,
                Component.translatable(translationKey),
                x,
                y,
                0x6FD4FF,
                false
        );
    }

    private void drawValue(
            GuiGraphics guiGraphics,
            String labelTranslationKey,
            Component value,
            int x,
            int y
    ) {
        Component text = Component.translatable(labelTranslationKey)
                .append(Component.literal(": "))
                .append(value);
        guiGraphics.drawString(font, text, x, y, 0xE7EDF2, false);
    }

    private Component yesNo(boolean value) {
        return Component.translatable(
                value ? "medicalmod.yes" : "medicalmod.no"
        );
    }
}
