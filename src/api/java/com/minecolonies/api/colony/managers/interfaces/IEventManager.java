package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IEventManager
{
    void addEvent(IColonyEvent raidEvent);

    int getAndTakeNextEventID();

    void registerEntity(@NotNull Entity entity, int eventID);

    void unregisterEntity(@NotNull Entity entity, int eventID);

    void onEntityDeath(EntityLiving entity, int raidID);

    IColonyEvent getEventByID(int ID);

    void onColonyTick(@NotNull IColony colony);

    Map<Integer, IColonyEvent> getEvents();

    void readFromNBT(@NotNull NBTTagCompound compound);

    void onTileEntityBreak(int eventID, TileEntity te);

    void writeToNBT(@NotNull NBTTagCompound compound);

    IEventStructureManager getStructureManager();

    void onNightFall();
}
