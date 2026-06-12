package cz.vasek.medicalmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BandageItem extends Item {
    private static final float HEAL_AMOUNT = 4.0F;
    private static final int COOLDOWN_TICKS = 40;

    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getHealth() >= player.getMaxHealth()) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.medicalmod.bandage_not_needed"));
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            player.heal(HEAL_AMOUNT);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.sendSystemMessage(Component.translatable("message.medicalmod.bandage_used"));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
