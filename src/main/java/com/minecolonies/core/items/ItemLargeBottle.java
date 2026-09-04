package com.minecolonies.core.items;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * A custom item class for jug items.
 */
public class ItemLargeBottle extends Item
{
    /**
     * Creates a new jug item.
     *
     * @param builder the item properties to use.
     */
    public ItemLargeBottle(@NotNull final Properties builder)
    {
        super(builder);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(
        @NotNull final ItemStack stack,
        @NotNull final Player player,
        @NotNull final LivingEntity entity,
        @NotNull final InteractionHand hand)
    {
        if (this != ModItems.large_empty_bottle)
        {
            return super.interactLivingEntity(stack, player, entity, hand);
        }

        if (player.getCooldowns().isOnCooldown(stack))
        {
            return super.interactLivingEntity(stack, player, entity, hand);
        }

        if (entity instanceof Cow && !entity.isBaby())
        {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            if (!InventoryUtils.addItemStackToItemHandler(new PlayerMainInvWrapper(player.getInventory()), ModItems.large_milk_bottle.getDefaultInstance()))
            {
                player.drop(ModItems.large_milk_bottle.getDefaultInstance(), false);
            }
            stack.shrink(1);
            player.getCooldowns().addCooldown(stack, TICKS_SECOND * 10);
            return InteractionResult.SUCCESS;
        }
        else if (entity instanceof final Goat goat && !entity.isBaby())
        {
            player.playSound((goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK), 1.0F, 1.0F);
            if (!InventoryUtils.addItemStackToItemHandler(new PlayerMainInvWrapper(player.getInventory()), ModItems.large_milk_bottle.getDefaultInstance()))
            {
                player.drop(ModItems.large_milk_bottle.getDefaultInstance(), false);
            }
            stack.shrink(1);
            player.getCooldowns().addCooldown(stack, TICKS_SECOND * 10);
            return InteractionResult.SUCCESS;
        }

        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull final Level level, final Player player, @NotNull final InteractionHand hand)
    {
        final ItemStack itemstack = player.getItemInHand(hand);
        if (this != ModItems.large_empty_bottle)
        {
            return InteractionResult.PASS;
        }

        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() != HitResult.Type.MISS)
        {
            if (blockhitresult.getType() == HitResult.Type.BLOCK)
            {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (!level.mayInteract(player, blockpos))
                {
                    return InteractionResult.PASS;
                }

                if (level.getFluidState(blockpos).is(FluidTags.WATER))
                {
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    if (!InventoryUtils.addItemStackToItemHandler(new PlayerMainInvWrapper(player.getInventory()), ModItems.large_water_bottle.getDefaultInstance()))
                    {
                        player.drop(ModItems.large_water_bottle.getDefaultInstance(), false);
                    }
                    itemstack.shrink(1);
                    player.getCooldowns().addCooldown(itemstack, TICKS_SECOND);
                    return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
                }
            }
        }
        return InteractionResult.PASS;
    }
}
