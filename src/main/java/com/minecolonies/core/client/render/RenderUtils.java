package com.minecolonies.core.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class RenderUtils
{
    /**
     * Arm pose helper, take from PlayerRenderer#getArmPose
     *
     * @param entity
     * @param hand
     * @return
     */
    public static HumanoidModel.ArmPose getArmPose(Mob entity, InteractionHand hand)
    {
        if (entity.isLeftHanded())
        {
            hand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }

        ItemStack itemstack = entity.getItemInHand(hand);
        if (itemstack.isEmpty())
        {
            return HumanoidModel.ArmPose.EMPTY;
        }
        else
        {
            if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0)
            {
                ItemUseAnimation useanim = itemstack.getUseAnimation();
                if (useanim == ItemUseAnimation.BLOCK)
                {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == ItemUseAnimation.BOW)
                {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == ItemUseAnimation.SPEAR)
                {
                    return HumanoidModel.ArmPose.SPEAR;
                }

                if (useanim == ItemUseAnimation.CROSSBOW && hand == entity.getUsedItemHand())
                {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == ItemUseAnimation.SPYGLASS)
                {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (useanim == ItemUseAnimation.TOOT_HORN)
                {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (useanim == ItemUseAnimation.BRUSH)
                {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            }
            else if (!entity.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack))
            {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            HumanoidModel.ArmPose forgeArmPose = IClientItemExtensions.of(itemstack).getArmPose(entity, hand, itemstack);
            if (forgeArmPose != null)
            {
                return forgeArmPose;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }
}
