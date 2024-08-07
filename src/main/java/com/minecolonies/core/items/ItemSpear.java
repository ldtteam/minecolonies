package com.minecolonies.core.items;

import com.minecolonies.core.entity.other.SpearEntity;
import com.minecolonies.core.client.render.SpearItemTileEntityRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ItemSpear extends TridentItem
{
    public ItemSpear(final Properties properties)
    {
        super(properties.durability(250));
    }

    @Override
    public void initializeClient(final Consumer<IClientItemExtensions> consumer)
    {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return new SpearItemTileEntityRenderer();
            }
        });
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof Player)
        {
            Player playerEntity = (Player) entityLiving;
            int usedForDuration = this.getUseDuration(stack, entityLiving) - timeLeft;
            if (usedForDuration >= 10)
            {
                if (!worldIn.isClientSide)
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
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull final Level world, final Player playerEntity, @NotNull final InteractionHand hand)
    {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1)
        {
            return InteractionResultHolder.fail(itemstack);
        }
        else
        {
            playerEntity.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }
}
