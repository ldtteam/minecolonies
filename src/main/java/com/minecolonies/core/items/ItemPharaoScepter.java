package com.minecolonies.core.items;

import com.minecolonies.api.items.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

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
            int useDuration = this.getUseDuration(stack, entityLiving) - timeLeft;
            useDuration = net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack, worldIn, playerentity, useDuration, true);
            if (useDuration < 0)
            {
                return;
            }

            ItemStack itemstack = playerentity.getProjectile(stack);
            if (!itemstack.isEmpty())
            {
                float speed = getPowerForTime(useDuration);
                if (!((double) speed < 0.1))
                {
                    List<ItemStack> list = draw(stack, itemstack, entityLiving);
                    if (worldIn instanceof ServerLevel serverlevel && !list.isEmpty())
                    {
                        this.shoot(serverlevel, entityLiving, entityLiving.getUsedItemHand(), stack, list, speed * 3.0F, 1.0F, speed == 1.0F, null);
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
    }

    @NotNull
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles()
    {
        return itemStack -> true;
    }

    @Override
    public AbstractArrow customArrow(final AbstractArrow arrow, final ItemStack projectileStack, final ItemStack weaponStack)
    {
        if (arrow.getOwner() == null)
        {
            return arrow;
        }

        AbstractArrow entity = ModItems.firearrow.get().createArrow(arrow.level(), ModItems.firearrow.toStack(), (LivingEntity) arrow.getOwner(), weaponStack);
        entity.pickup = AbstractArrow.Pickup.DISALLOWED;
        entity.setRemainingFireTicks(3 * TICKS_PER_SECOND);

        return entity;
    }
}
