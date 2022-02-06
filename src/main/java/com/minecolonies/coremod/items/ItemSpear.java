package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.SpearEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.render.SpearItemTileEntityRenderer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemSpear extends TridentItem
{
    public ItemSpear(final Properties properties)
    {
        super(properties.durability(250).tab(ModCreativeTabs.MINECOLONIES).setISTER(() -> SpearItemTileEntityRenderer::new));
        setRegistryName(new ResourceLocation(Constants.MOD_ID, "spear"));
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof PlayerEntity)
        {
            PlayerEntity playerEntity = (PlayerEntity) entityLiving;
            int usedForDuration = this.getUseDuration(stack) - timeLeft;
            if (usedForDuration >= 10)
            {
                if (!worldIn.isClientSide)
                {
                    stack.hurtAndBreak(1, playerEntity, playerEntity1 -> playerEntity1.broadcastBreakEvent(entityLiving.getUsedItemHand()));
                    SpearEntity spearEntity = new SpearEntity(worldIn, playerEntity, stack);
                    spearEntity.shootFromRotation(playerEntity, playerEntity.xRot, playerEntity.yRot, 0.0F, 2.5F, 1.0F);
                    if (playerEntity.abilities.instabuild)
                    {
                        spearEntity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                    }
                    else
                    {
                        playerEntity.inventory.removeItem(stack);
                    }

                    worldIn.addFreshEntity(spearEntity);
                    worldIn.playSound(null, spearEntity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }

                SoundEvent soundEvent = SoundEvents.TRIDENT_THROW;
                playerEntity.awardStat(Stats.ITEM_USED.get(this));
                worldIn.playSound(null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand)
    {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1)
        {
            return ActionResult.fail(itemstack);
        }
        else
        {
            playerEntity.startUsingItem(hand);
            return ActionResult.consume(itemstack);
        }
    }
}
