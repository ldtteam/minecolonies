package com.minecolonies.core.items;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * A custom item class for jug items.
 */
public class ItemJug extends Item
{
    /**
     * Creates a new jug item.
     *
     * @param builder the item properties to use.
     */
    public ItemJug(@NotNull final Properties builder)
    {
        super(builder);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull final ItemStack stack, @NotNull final Player player, @NotNull final LivingEntity entity, @NotNull final InteractionHand hand)
    {
        if (entity instanceof Cow && !entity.isBaby()) {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            stack.shrink(1);
            final ItemStack newStack = ModItems.milk_jug.getDefaultInstance();
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(player.getInventory()), newStack);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull final Level level, final Player player, @NotNull final InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() != HitResult.Type.MISS)
        {
            if (blockhitresult.getType() == HitResult.Type.BLOCK)
            {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (!level.mayInteract(player, blockpos))
                {
                    return InteractionResultHolder.pass(itemstack);
                }

                if (level.getFluidState(blockpos).is(FluidTags.WATER))
                {
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    itemstack.shrink(1);
                    final ItemStack newStack = ModItems.water_jug.getDefaultInstance();
                    InventoryUtils.addItemStackToItemHandler(new InvWrapper(player.getInventory()), newStack);
                    return InteractionResultHolder.success(itemstack);
                }
            }
        }
        return InteractionResultHolder.pass(itemstack);
    }
}
