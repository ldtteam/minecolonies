package com.minecolonies.api.colony;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IManagedAnimal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Data interface for animals managed by the Animal Manager.
 */
public interface IAnimalData extends INBTSerializable<CompoundTag>
{
    /**
     * Get the animal data ID.
     *
     * @return the animal data ID
    */
    public int getId();

    /**
     * Get the globally unique identifier associated with this animal data.
     *
     * @return the globally unique identifier associated with this animal data.
     */
    public UUID getUUID();

    /**
     * Initializes the entities values from animal data.
     */
    public void initEntityValues();

    /**
     * Get the animal entity.
     *
     * @return the animal entity.
     */
    public Optional<IManagedAnimal <? extends Animal>> getManagedAnimal();

    /**
     * Set the animal entity.
     *
     * @param entity the animal entity.
     */
    public void setManagedAnimal(final IManagedAnimal<? extends Animal> entity);

    /**
     * Clear the dirty flag for this animal data.
     */
    public void clearDirty();

    /**
     * Check if this animal data is dirty and needs syncing.
     *
     * @return true if dirty, false otherwise
     */
    public boolean isDirty();

    /**
     * Mark this animal data as dirty and in need of syncing / saving.
     */
    public void markDirty();

    /**
     * Update the animal data.
     *
     * @param tickRate the tick rate
     */
    public void update(final int tickRate);

    /**
     * Writes the animal data to a byte buf for transition.
     *
     * @param buf Buffer to write to.
     */
    void serializeViewNetworkData(@NotNull FriendlyByteBuf buf);

    /**
     * Gets the home building of the animal.
     * 
     * @return the home building, or null if the animal does not have a home building.
     */
    public IBuilding getHomeBuilding();

    /**
     * Sets the home building of the animal.
     * 
     * @param building the new home building of the animal.
     */
    public void setHomeBuilding(@NotNull IBuilding building);

    /**
     * Called when a building is removed.
     * 
     * @param building the building that was removed.
     */
    public void onRemoveBuilding(final IBuilding building);

    /**
     * Sets the last position of the animal.
     * 
     * @param lastPosition the last position of the animal.
     */
    public void setLastPosition(final BlockPos lastPosition);

    /**
     * Gets the last position of the animal.
     * 
     * @return the last position of the animal.
     */
    public BlockPos getLastPosition();

    /**
     * Returns the current combat cooldown of the horse. 
     * A higher value means the horse is currently less ready for combat.
     * 
     * @return the current combat cooldown of the horse
     */
    public float getCombatCooldown();

    /**
     * Sets the combat cooldown of the horse.
     * 
     * @param newCooldown the new combat cooldown of the horse
     */
    public void setCombatCooldown(float newCooldown);
}
