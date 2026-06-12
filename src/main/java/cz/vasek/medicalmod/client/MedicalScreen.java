package cz.vasek.medicalmod.client;

import cz.vasek.medicalmod.medical.BodyPart;
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
    private static final int TAB_INJURIES = 1;
    private static final int TAB_TREATMENT = 2;

    private final List<Button> treatmentButtons = new ArrayList<>();
    private int activeTab = TAB_OVERVIEW;

    public MedicalScreen(MedicalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 320;
        imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(
                Component.translatable("menu.medicalmod.tab.overview"),
                button -> setActiveTab(TAB_OVERVIEW)
        ).bounds(leftPos + 8, topPos + 24, 98, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("menu.medicalmod.tab.injuries"),
                button -> setActiveTab(TAB_INJURIES)
        ).bounds(leftPos + 111, topPos + 24, 98, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("menu.medicalmod.tab.treatment"),
                button -> setActiveTab(TAB_TREATMENT)
        ).bounds(leftPos + 214, topPos + 24, 98, 20).build());

        TreatmentAction[] actions = TreatmentAction.values();
        for (int index = 0; index < actions.length; index++) {
            TreatmentAction action = actions[index];
            int column = index % 3;
            int row = index / 3;

            Button button = Button.builder(
                    Component.translatable(action.getTranslationKey()),
                    ignored -> sendTreatment(action)
            ).bounds(
                    leftPos + 10 + column * 102,
                    topPos + 62 + row * 31,
                    96,
                    24
            ).build();

            treatmentButtons.add(addRenderableWidget(button));
        }

        updateTreatmentButtonVisibility();
    }

    private void setActiveTab(int tab) {
        activeTab = tab;
        updateTreatmentButtonVisibility();
    }

    private void updateTreatmentButtonVisibility() {
        boolean visible = activeTab == TAB_TREATMENT;
        treatmentButtons.forEach(button -> button.visible = visible);
    }

    private void sendTreatment(TreatmentAction action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action.getId());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xF0181D22);
        guiGraphics.fill(left + 1, top + 1, left + imageWidth - 1, top + 20, 0xFF28323C);
        guiGraphics.fill(left, top, left + imageWidth, top + 1, 0xFFBFC7CE);
        guiGraphics.fill(left, top + imageHeight - 1, left + imageWidth, top + imageHeight, 0xFFBFC7CE);
        guiGraphics.fill(left, top, left + 1, top + imageHeight, 0xFFBFC7CE);
        guiGraphics.fill(left + imageWidth - 1, top, left + imageWidth, top + imageHeight, 0xFFBFC7CE);

        if (activeTab != TAB_TREATMENT) {
            guiGraphics.fill(left + 8, top + 50, left + imageWidth - 8, top + imageHeight - 8, 0xCC101418);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(
                font,
                Component.translatable("menu.medicalmod.patient", menu.getTargetName()),
                8,
                7,
                0xFFFFFF,
                false
        );

        switch (activeTab) {
            case TAB_INJURIES -> renderInjuries(guiGraphics);
            case TAB_TREATMENT -> renderTreatmentInfo(guiGraphics);
            default -> renderOverview(guiGraphics);
        }
    }

    private void renderOverview(GuiGraphics guiGraphics) {
        int leftX = 16;
        int rightX = 168;
        int y = 58;
        int rowHeight = 18;

        drawSectionTitle(guiGraphics, "menu.medicalmod.section.vitals", leftX, y);
        y += 18;
        drawValue(guiGraphics, "medicalmod.vital.pulse", Component.literal(menu.getPulse() + " /min"), leftX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.vital.pressure", Component.literal(
                menu.getSystolicPressure() + "/" + menu.getDiastolicPressure() + " mmHg"
        ), leftX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.vital.spo2", Component.literal(menu.getOxygenSaturation() + " %"), leftX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.vital.respiratory_rate", Component.literal(
                menu.getRespiratoryRate() + " /min"
        ), leftX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.vital.temperature", Component.literal(
                String.format(Locale.ROOT, "%.1f °C", menu.getTemperatureTenths() / 10.0D)
        ), leftX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.vital.blood_volume", Component.literal(
                menu.getBloodVolumeMl() + " ml"
        ), leftX, y);

        y = 58;
        drawSectionTitle(guiGraphics, "menu.medicalmod.section.status", rightX, y);
        y += 18;
        drawValue(guiGraphics, "medicalmod.status.bleeding", Component.literal(
                Integer.toString(menu.getBleedingLevel())
        ), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.pain", Component.literal(menu.getPain() + "/10"), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.consciousness", Component.translatable(
                "medicalmod.consciousness." + menu.getConsciousnessLevel()
        ), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.airway", Component.translatable(
                "medicalmod.airway." + menu.getAirwayStatus()
        ), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.pneumothorax", yesNo(menu.hasPneumothorax()), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.rhythm", Component.translatable(
                "medicalmod.rhythm." + menu.getCardiacRhythm()
        ), rightX, y);
        y += rowHeight;
        drawValue(guiGraphics, "medicalmod.status.iv_access", yesNo(menu.hasIvAccess()), rightX, y);
    }

    private void renderInjuries(GuiGraphics guiGraphics) {
        int y = 60;
        drawSectionTitle(guiGraphics, "menu.medicalmod.section.body_map", 16, y);
        y += 22;

        for (BodyPart bodyPart : BodyPart.values()) {
            Component severity = Component.translatable(
                    "medicalmod.injury.severity." + menu.getInjurySeverity(bodyPart)
            );
            Component fracture = menu.hasFracture(bodyPart)
                    ? Component.translatable("medicalmod.fracture.yes")
                    : Component.translatable("medicalmod.fracture.no");

            guiGraphics.drawString(
                    font,
                    Component.translatable(bodyPart.getTranslationKey()),
                    18,
                    y,
                    0xFFFFFF,
                    false
            );
            guiGraphics.drawString(font, severity, 128, y, 0xFFD37A, false);
            guiGraphics.drawString(font, fracture, 222, y, 0xD7E1E8, false);
            y += 21;
        }
    }

    private void renderTreatmentInfo(GuiGraphics guiGraphics) {
        guiGraphics.drawString(
                font,
                Component.translatable("menu.medicalmod.treatment.info"),
                12,
                50,
                0xC9D3DC,
                false
        );
    }

    private void drawSectionTitle(GuiGraphics guiGraphics, String translationKey, int x, int y) {
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
        return Component.translatable(value ? "medicalmod.yes" : "medicalmod.no");
    }
}
