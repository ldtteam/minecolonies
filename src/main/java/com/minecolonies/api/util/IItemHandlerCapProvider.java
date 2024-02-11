package com.minecolonies.api.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Our class for to join {@link IItemHandler} providers, so we can have type independent code.
 */
@FunctionalInterface
public interface IItemHandlerCapProvider
{
    /**
     * For EntityCap register only
     */
    @Nullable
    default IItemHandler getItemHandlerCap(final Void nothing)
    {
        return getItemHandlerCap();
    }

    /**
     * @return direction-unaware itemHandler
     */
    @Nullable
    default IItemHandler getItemHandlerCap()
    {
        return getItemHandlerCap((Direction) null);
    }

    /**
     * @return direction-aware itemHandler
     */
    @Nullable
    IItemHandler getItemHandlerCap(final Direction direction);

    public static IItemHandlerCapProvider wrap(final BlockEntity blockEntity)
    {
        return direction -> ItemHandler.BLOCK.getCapability(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction);
    }

    /**
     * @param sided if true then will use Direction aware capability, roughly should be true for machine-entities and false for mobs
     */
    public static IItemHandlerCapProvider wrap(final Entity entity, final boolean sided)
    {
        return sided ? direction -> ItemHandler.ENTITY_AUTOMATION.getCapability(entity, direction) :
            direction -> ItemHandler.ENTITY.getCapability(entity, null);
    }

    public static IItemHandlerCapProvider wrap(final ItemStack itemStack)
    {
        return direction -> ItemHandler.ITEM.getCapability(itemStack, null);
    }
}