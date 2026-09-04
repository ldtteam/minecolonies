package com.minecolonies.core.items;
import net.minecraft.world.InteractionResult;

import com.minecolonies.core.entity.other.SpearEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;


public class ItemSpear extends TridentItem
{
    protected static final int SPEAR_BASE_DAMAGE = 3;

    public ItemSpear(final Properties properties)
    {
        super(properties.durability(250));
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof Player)
        {
            Player playerEntity = (Player) entityLiving;
            int usedForDuration = this.getUseDuration(stack, entityLiving) - timeLeft;
            if (usedForDuration >= 10)
            {
                if (!worldIn.isClientSide())
                {
                    stack.hurtAndBreak(1, playerEntity, EquipmentSlot.MAINHAND);
                    SpearEntity spearEntity = new SpearEntity(worldIn, playerEntity, stack);

                    if (playerEntity.getAbilities().instabuild)
                    {
                        spearEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }
                    else
                    {
                        playerEntity.getInventory().removeItem(stack);
                    }

                    worldIn.addFreshEntity(spearEntity);
                    worldIn.playSound(null, spearEntity, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                SoundEvent soundEvent = SoundEvents.TRIDENT_THROW.value();
                playerEntity.awardStat(Stats.ITEM_USED.get(this));
                worldIn.playSound(null, playerEntity, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return true;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull final Level world, final Player playerEntity, @NotNull final InteractionHand hand)
    {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1)
        {
            return InteractionResult.FAIL;
        }
        else
        {
            playerEntity.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    /**
     * For cavalry use, allow the spear to be used as a melee weapon, so allow the default sword actions.
     * @param stack the item stack.
     * @param itemAbility the item ability.
     * @return true if the action can be performed.
     */
    @Override
    public boolean canPerformAction(final ItemInstance stack, final ItemAbility itemAbility)
    {
        return itemAbility == ItemAbilities.SWORD_SWEEP;
    }

    /** Gets the base damage of the spear.
     *
     * @return the base damage of the spear.
     **/
    public int getDamage()
    {
        return SPEAR_BASE_DAMAGE;
    }
}
