package com.minecolonies.core.items;

import com.minecolonies.api.items.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class handling the Pharao Scepter item.
 */
public class ItemPharaoScepter extends BowItem
{
    /**
     * Constructor method for the Chief Sword Item
     *
     * @param properties the properties.
     */
    public ItemPharaoScepter(final Properties properties)
    {
        super(properties.durability(384));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull final Level worldIn, Player playerIn, @NotNull final InteractionHand handIn)
    {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        InteractionResultHolder<ItemStack> ret = net.neoforged.neoforge.event.EventHooks.onArrowNock(itemstack, worldIn, playerIn, handIn, true);
        if (ret != null)
        {
            return ret;
        }

        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(@NotNull final ItemStack stack, @NotNull final Level worldIn, LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof Player)
        {
            Player playerentity = (Player) entityLiving;

            int useDuration = this.getUseDuration(stack) - timeLeft;
            useDuration = net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack, worldIn, playerentity, useDuration, true);
            if (useDuration < 0)
            {
                return;
            }

            float speed = getPowerForTime(useDuration);
            if (!((double) speed < 0.1D))
            {
                if (!worldIn.isClientSide)
                {
                    ArrowItem arrowitem = (ArrowItem) Items.ARROW;
                    AbstractArrow abstractarrowentity = arrowitem.createArrow(worldIn, new ItemStack(arrowitem, 1), playerentity);
                    abstractarrowentity = customArrow(abstractarrowentity);
                    abstractarrowentity.shootFromRotation(playerentity, playerentity.getXRot(), playerentity.getYRot(), 0.0F, speed * 3.0F, 1.0F);
                    if (speed == 1.0F)
                    {
                        abstractarrowentity.setCritArrow(true);
                    }

                    int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    if (j > 0)
                    {
                        abstractarrowentity.setBaseDamage(abstractarrowentity.getBaseDamage() + (double) j * 0.5D + 0.5D);
                    }

                    int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (k > 0)
                    {
                        abstractarrowentity.setKnockback(k);
                    }

                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0)
                    {
                        abstractarrowentity.setSecondsOnFire(100);
                    }

                    stack.hurtAndBreak(1, playerentity, new Consumer<Player>() {
                        @Override
                        public void accept(final Player player)
                        {
                            player.broadcastBreakEvent(playerentity.getUsedItemHand());
                        }
                    });

                    abstractarrowentity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                    worldIn.addFreshEntity(abstractarrowentity);
                }

                worldIn.playSound(null,
                  playerentity.getX(),
                  playerentity.getY(),
                  playerentity.getZ(),
                  SoundEvents.ARROW_SHOOT,
                  SoundSource.PLAYERS,
                  1.0F,
                  1.0F / (entityLiving.getRandom().nextFloat() * 0.4F + 1.2F) + speed * 0.5F);
                playerentity.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @NotNull
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles()
    {
        return itemStack -> true;
    }

    @NotNull
    @Override
    public AbstractArrow customArrow(@NotNull AbstractArrow arrow)
    {
        if (arrow.getOwner() == null)
        {
            return arrow;
        }

        AbstractArrow entity = ((ArrowItem) ModItems.firearrow).createArrow(arrow.level(), new ItemStack(ModItems.firearrow, 1), (LivingEntity) arrow.getOwner());
        entity.pickup = AbstractArrow.Pickup.DISALLOWED;
        entity.setSecondsOnFire(3);

        return entity;
    }
}
