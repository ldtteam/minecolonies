package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for any field instance.
 */
public interface IField
{
    /**
     * Get the type of the field.
     *
     * @return the type of the field.
     */
    FieldStructureType getType();

    /**
     * Gets the position of the field.
     *
     * @return central location of the field.
     */
    @NotNull BlockPos getPosition();

    /**
     * Gets the plant which is being cultivated on this field.
     *
     * @return the plant for this field.
     */
    @Nullable Item getPlant();

    /**
     * Set the plant for this farm field.
     *
     * @param plant the new plant.
     */
    void setPlant(Item plant);

    /**
     * Getter for the colony of the field.
     *
     * @return the int id.
     */
    IColony getColony();

    /**
     * Getter for the ownerId of the field.
     *
     * @return the int id or null.
     */
    @Nullable Integer getOwnerId();

    /**
     * Sets the owner of the field.
     * This method stores both the citizen and colony ID.
     *
     * @param ownerId id of the citizen.
     */
    void setOwner(final int ownerId);

    /**
     * Resets the ownership of the field.
     */
    void resetOwner();

    /**
     * Has the field been taken.
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    boolean isTaken();

    /**
     * Checks if the field needs work.
     *
     * @return true if so.
     */
    boolean needsWork();

    /**
     * Reconstruct the field from the given NBT data.
     *
     * @param compound the compound to read from.
     */
    void deserializeNBT(CompoundTag compound);

    /**
     * Stores the NBT data of the field.
     */
    @NotNull CompoundTag serializeNBT();

    /**
     * Serialize a field to a buffer.
     *
     * @param fieldData the buffer to write the field data to.
     */
    void serializeToView(FriendlyByteBuf fieldData);
}
