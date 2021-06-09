package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.items.ModItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import net.minecraft.item.Item.Properties;

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
        super(properties.tab(ModCreativeTabs.MINECOLONIES).durability(384));
        setRegistryName("pharaoscepter");
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> use(@NotNull final World worldIn, PlayerEntity playerIn, @NotNull final Hand handIn)
    {
        ItemStack itemstack = playerIn.getLastHandItem(handIn);

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, true);
        if (ret != null)
        {
            return ret;
        }

        playerIn.startUsingItem(handIn);
        return ActionResult.consume(itemstack);
    }

    @Override
    public void releaseUsing(@NotNull final ItemStack stack, @NotNull final World worldIn, LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof PlayerEntity)
        {
            PlayerEntity playerentity = (PlayerEntity) entityLiving;

            int useDuration = this.getUseDuration(stack) - timeLeft;
            useDuration = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, playerentity, useDuration, true);
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
                    AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, new ItemStack(arrowitem, 1), playerentity);
                    abstractarrowentity = customArrow(abstractarrowentity);
                    abstractarrowentity.shoot(playerentity.xRot, playerentity.yRot, 0.0F, speed * 3.0F, 1.0F);
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

                    stack.hurtAndBreak(1, playerentity, (p_220009_1_) -> {
                        p_220009_1_.broadcastBreakEvent(playerentity.getUsedItemHand());
                    });
                    abstractarrowentity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;

                    worldIn.addFreshEntity(abstractarrowentity);
                }

                worldIn.playSound(null,
                  playerentity.getX(),
                  playerentity.getY(),
                  playerentity.getZ(),
                  SoundEvents.ARROW_SHOOT,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F / (random.nextFloat() * 0.4F + 1.2F) + speed * 0.5F);
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
    public AbstractArrowEntity customArrow(@NotNull AbstractArrowEntity arrow)
    {
        AbstractArrowEntity entity = ((ArrowItem) ModItems.firearrow).createArrow(arrow.level, new ItemStack(ModItems.firearrow, 1), (LivingEntity) arrow.getOwner());
        entity.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
        entity.setSecondsOnFire(3);

        return entity;
    }
}
