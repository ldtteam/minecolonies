package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface describing the different modules.
 */
public interface IBuildingModule
{
    /**
     * Deserialize the module.
     * @param compound the nbt compound.
     */
    default void deserializeNBT(CompoundNBT compound)
    {

    }

    /**
     * Serialize the module from a compound.
     * @param compound the compound.
     */
    default void serializeNBT(final CompoundNBT compound)
    {

    }

    /**
     * Serialization method to send the module data to the client side.
     * @param buf the buffer to write it to.
     */
    default void serializeToView(PacketBuffer buf)
    {

    }

    /**
     * On destruction hook of the building, calling into the modules.
     */
    default void onDestroyed()
    {

    }

    /**
     * Colony tick hook.
     * @param colony the colony the tick is invoked from.
     */
    default void onColonyTick(@NotNull IColony colony)
    {

    }

    /**
     * Upgrade complete module hook.
     * @param newLevel the new level.
     */
    default void onUpgradeComplete(int newLevel)
    {

    }

    /**
     * Set the building level hook for modules.
     * @param level the level to set it.
     */
    default void setBuildingLevel(int level)
    {

    }

    /**
     * On building move hook for modules.
     * @param oldBuilding the building that is going to be moved.
     */
    default void onBuildingMove(IBuilding oldBuilding)
    {

    }

    /**
     * Specific wakeup hook in modules.
     */
    default void onWakeUp()
    {

    }

    /**
     * On player entering hook.
     * @param player the player that entered the building.
     */
    default void onPlayerEnterBuilding(PlayerEntity player)
    {

    }

    /**
     * Specific dirty marking of modules (separate from building dirty).
     */
    void markDirty();

    /**
     * Check if one of the modules is dirty.
     * @return true if so.
     */
    boolean checkDirty();

    /**
     * Clear the dirty setting of the module.
     */
    void clearDirty();
}
