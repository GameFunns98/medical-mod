package cz.vasek.medicalmod.item;

import cz.vasek.medicalmod.menu.MedicalMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class MedicalTabletItem extends Item {
    public MedicalTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MedicalMenu.open(serverPlayer, serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof ServerPlayer targetPlayer)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide && player instanceof ServerPlayer examiner) {
            MedicalMenu.open(examiner, targetPlayer);
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
