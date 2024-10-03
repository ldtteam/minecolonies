package com.minecolonies.core.tileentities.storageblocks;

import java.util.HashMap;
import java.util.Map;

import com.ldtteam.blockui.mod.Log;
import com.minecolonies.api.colony.event.StorageBlockStackInsertEvent;
import com.minecolonies.api.tileentities.storageblocks.IStorageBlockNotificationManager;
import com.minecolonies.api.tileentities.storageblocks.InsertNotifier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

public class StorageBlockNotificationManager implements IStorageBlockNotificationManager
{
    private Map<ResourceKey<Level>, Map<BlockPos, InsertNotifier>> notifiers = new HashMap<>();

    @Override
    public void addListener(final ResourceKey<Level> dimension, final BlockPos targetPos, final BlockPos listenerPos)
    {
        if (!notifiers.containsKey(dimension))
        {
            notifiers.put(dimension, new HashMap<BlockPos, InsertNotifier>());
        }

        if (!notifiers.get(dimension).containsKey(targetPos))
        {
            notifiers.get(dimension).put(targetPos, new InsertNotifier());
        }

        notifiers.get(dimension).get(targetPos).addInsertListener(listenerPos);
    }

    @Override
    public void onInsert(StorageBlockStackInsertEvent event)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Level level = server.levels.get(event.getDimension());
        if (level.isClientSide)
        {
            return;
        }

        if (!notifiers.containsKey(event.getDimension()))
        {
            return;
        }

        if (!notifiers.get(event.getDimension()).containsKey(event.getPosition()))
        {
            return;
        }

        if (event.getInsertedStack().isEmpty())
        {
            return;
        }

        notifiers.get(event.getDimension()).get(event.getPosition()).notifyInsert(event.getDimension(), event.getPosition(), event.getInsertedStack());
    }
}
