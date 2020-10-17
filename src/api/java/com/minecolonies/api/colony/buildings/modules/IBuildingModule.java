package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    void deserializeNBT(CompoundNBT compound);

    /**
     * Serialize the module from a compound.
     * @param compound the compound.
     */
    void serializeNBT(final CompoundNBT compound);

    /**
     * On destruction hook of the building, calling into the modules.
     */
    void onDestroyed();

    /**
     * Specific citizen removal hook for modules.
     * @param citizen the removed citizen.
     * @return true if one was removed.
     */
    boolean removeCitizen(@NotNull ICitizenData citizen);

    /**
     * Colony tick hook.
     * @param colony the colony the tick is invoked from.
     */
    void onColonyTick(@NotNull IColony colony);

    /**
     * Specific citizen assignment hook for modules.
     * @param citizen the added citizen.
     * @return true if one was added.
     */
    boolean assignCitizen(ICitizenData citizen);

    /**
     * Get the max building level a module allows.
     * @return the max level.
     */
    int getMaxBuildingLevel();

    /**
     * Get the max number of inhabitants this module allows.
     * @return the modules max number of assigned citizens.
     */
    int getMaxInhabitants();

    /**
     * Upgrade complete module hook.
     * @param newLevel the new level.
     */
    void onUpgradeComplete(int newLevel);

    /**
     * Set the building level hook for modules.
     * @param level the level to set it.
     */
    void setBuildingLevel(int level);

    /**
     * On building move hook for modules.
     * @param oldBuilding the building that is going to be moved.
     */
    void onBuildingMove(IBuilding oldBuilding);

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

    /**
     * Specific wakeup hook in modules.
     */
    void onWakeUp();

    /**
     * Attempt to register a specific block at a specific module.
     * @param blockState the state.
     * @param pos the position.
     * @param world the world.
     */
    void registerBlockPosition(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull World world);

    /**
     * Serialization method to send the module data to the client side.
     * @param buf the buffer to write it to.
     */
    void serializeToView(PacketBuffer buf);

    /**
     * On player entering hook.
     * @param player the player that entered the building.
     */
    void onPlayerEnterBuilding(PlayerEntity player);
}
