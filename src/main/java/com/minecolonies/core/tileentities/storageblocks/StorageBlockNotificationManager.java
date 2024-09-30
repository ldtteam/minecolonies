package com.minecolonies.core.tileentities.storageblocks;

import java.util.HashMap;
import java.util.Map;

import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlock;
import com.minecolonies.api.tileentities.storageblocks.IStorageBlockNotificationManager;
import com.minecolonies.api.tileentities.storageblocks.InsertNotifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;

public class StorageBlockNotificationManager implements IStorageBlockNotificationManager
{
    
    public static void handleNeighborUpdate(BlockEvent.NeighborNotifyEvent event)
    {
        BlockEntity entity = event.getLevel().getBlockEntity(event.getPos());
        if (entity == null)
        {
            return;
        }
        
        IStorageBlockNotificationManager.getInstance().notifyUpdate(event.getPos());
    }

    private Map<BlockPos, InsertNotifier> insertNotifiers = new HashMap<>();
    private Map<BlockPos, InsertNotifier> updateNotifiers = new HashMap<>();

    @Override
    public void addListener(BlockPos targetPos, BlockPos listenerPos)
    {
        if (insertNotifiers.containsKey(targetPos))
        {
            insertNotifiers.get(targetPos).addInsertListener(listenerPos);
        }

        if (updateNotifiers.containsKey(targetPos))
        {
            updateNotifiers.get(targetPos).addInsertListener(listenerPos);
        }
    }


    @Override
    public void registerNewTarget(final AbstractStorageBlock target, final Level level)
    {
        BlockPos targetPos = target.getPosition();
        if (target.supportsItemInsertNotification())
        {
            insertNotifiers.put(targetPos, new InsertNotifier(level));
        }
        else
        {
            updateNotifiers.put(targetPos, new InsertNotifier(level));
        }
    }


    @Override
    public void notifyInsert(BlockPos targetPos, ItemStack insertedStack)
    {
        if (insertNotifiers.containsKey(targetPos))
        {
            insertNotifiers.get(targetPos).notifyInsert(targetPos, insertedStack);
        }
    }


    @Override
    public void notifyUpdate(BlockPos targetPos)
    {
        if (updateNotifiers.containsKey(targetPos))
        {
            updateNotifiers.get(targetPos).notifyUpdate(targetPos);
        }
    }
    
}
