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
        super(properties.group(ModCreativeTabs.MINECOLONIES).maxDamage(384));
        setRegistryName("pharaoscepter");
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@NotNull final World worldIn, PlayerEntity playerIn, @NotNull final Hand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, true);
        if (ret != null)
            return ret;

        playerIn.setActiveHand(handIn);
        return ActionResult.func_226249_b_(itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(@NotNull final ItemStack stack, @NotNull final World worldIn, LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof PlayerEntity)
        {
            PlayerEntity playerentity = (PlayerEntity) entityLiving;

            int useDuration = this.getUseDuration(stack) - timeLeft;
            useDuration = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, playerentity, useDuration, true);
            if (useDuration < 0) return;

            float speed = getArrowVelocity(useDuration);
            if (!((double) speed < 0.1D))
            {
                if (!worldIn.isRemote)
                {
                    ArrowItem arrowitem = (ArrowItem) Items.ARROW;
                    AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, new ItemStack(arrowitem, 1), playerentity);
                    abstractarrowentity = customeArrow(abstractarrowentity);
                    abstractarrowentity.shoot(playerentity, playerentity.rotationPitch, playerentity.rotationYaw, 0.0F, speed * 3.0F, 1.0F);
                    if (speed == 1.0F)
                    {
                        abstractarrowentity.setIsCritical(true);
                    }

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                    if (j > 0)
                    {
                        abstractarrowentity.setDamage(abstractarrowentity.getDamage() + (double) j * 0.5D + 0.5D);
                    }

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                    if (k > 0)
                    {
                        abstractarrowentity.setKnockbackStrength(k);
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
                    {
                        abstractarrowentity.setFire(100);
                    }

                    stack.damageItem(1, playerentity, (p_220009_1_) -> {
                        p_220009_1_.sendBreakAnimation(playerentity.getActiveHand());
                    });
                    abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;

                    worldIn.addEntity(abstractarrowentity);
                }

                worldIn.playSound(null,
                  playerentity.getPosX(),
                  playerentity.getPosY(),
                  playerentity.getPosZ(),
                  SoundEvents.ENTITY_ARROW_SHOOT,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F / (random.nextFloat() * 0.4F + 1.2F) + speed * 0.5F);
                playerentity.addStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @NotNull
    @Override
    public Predicate<ItemStack> getInventoryAmmoPredicate()
    {
        return itemStack -> true;
    }

    @NotNull
    @Override
    public AbstractArrowEntity customeArrow(@NotNull AbstractArrowEntity arrow)
    {
        AbstractArrowEntity entity = ((ArrowItem) ModItems.firearrow).createArrow(arrow.world, new ItemStack(ModItems.firearrow, 1), (LivingEntity) arrow.getShooter());
        entity.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
        entity.setFire(3);

        return entity;
    }
}
