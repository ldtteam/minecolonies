package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for field instances.
 */
public interface IField
{
    /**
     * Return the field type for this field.
     *
     * @return the field registry entry.
     */
    @NotNull FieldRegistries.FieldEntry getFieldType();

    /**
     * Set the field type for this current field instance.
     *
     * @param fieldType the field type for this field.
     */
    void setFieldType(@NotNull FieldRegistries.FieldEntry fieldType);

    /**
     * Gets the position of the field.
     *
     * @return central location of the field.
     */
    @NotNull BlockPos getPosition();

    /**
     * Getter for the colony of the field.
     *
     * @return the int id.
     */
    IColony getColony();

    /**
     * Getter for the owning building of the field.
     *
     * @return the id or null.
     */
    @Nullable BlockPos getBuildingId();

    /**
     * Sets the owning building of the field.
     *
     * @param buildingId id of the building.
     */
    void setBuilding(final BlockPos buildingId);

    /**
     * Resets the ownership of the field.
     */
    void resetOwningBuilding();

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
     * Stores the NBT data of the field.
     */
    @NotNull CompoundTag serializeNBT();

    /**
     * Reconstruct the field from the given NBT data.
     *
     * @param compound the compound to read from.
     */
    void deserializeNBT(@NotNull CompoundTag compound);

    /**
     * Serialize a field to a buffer.
     *
     * @param buf the buffer to write the field data to.
     */
    void serializeToView(@NotNull FriendlyByteBuf buf);

    /**
     * Generate a matcher for this field.
     *
     * @return the field record matcher.
     */
    IFieldMatcher getMatcher();

    /**
     * Condition to check whether this field instance is currently properly placed down.
     *
     * @return true if the field is correctly placed at the current position.
     */
    boolean isValidPlacement();

    /**
     * Hashcode implementation for this field.
     */
    int hashCode();

    /**
     * Equals implementation for this field.
     */
    boolean equals(Object other);
}
