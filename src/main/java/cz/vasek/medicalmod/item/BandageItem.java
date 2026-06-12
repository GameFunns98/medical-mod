package cz.vasek.medicalmod.item;

import cz.vasek.medicalmod.medical.IMedicalData;
import cz.vasek.medicalmod.medical.MedicalDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class BandageItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        Optional<IMedicalData> medicalData =
                player.getCapability(MedicalDataProvider.CAPABILITY).resolve();

        if (medicalData.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.medicalmod.data_missing"));
            return InteractionResultHolder.fail(stack);
        }

        IMedicalData data = medicalData.get();
        if (!data.isBleeding()) {
            player.sendSystemMessage(Component.translatable("message.medicalmod.bandage_not_needed"));
            return InteractionResultHolder.fail(stack);
        }

        data.reduceBleeding(1);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (data.isBleeding()) {
            player.sendSystemMessage(Component.translatable(
                    "message.medicalmod.bandage_reduced",
                    data.getBleedingLevel()
            ));
        } else {
            player.sendSystemMessage(Component.translatable("message.medicalmod.bandage_stopped"));
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
