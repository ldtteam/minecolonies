package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.IMinecoloniesAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IStorageBlockNotificationManager
{
    public static IStorageBlockNotificationManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getStorageBlockNotificationManager();
    }

    void registerNewTarget(final AbstractStorageBlock target, final Level level);

    void addListener(final BlockPos targetPos, final BlockPos listenerPos);

    void notifyInsert(final BlockPos targetPos, final ItemStack insertedStack);

    void notifyUpdate(final BlockPos targetPos);
}
