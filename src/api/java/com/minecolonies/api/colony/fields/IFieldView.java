package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for field view instances.
 */
public interface IFieldView
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
     * Getter for the colony of the field.
     *
     * @return the int id.
     */
    @NotNull IColonyView getColonyView();

    /**
     * Gets the position of the field.
     *
     * @return central location of the field.
     */
    @NotNull BlockPos getPosition();

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
     * Deserialize a field from a buffer.
     *
     * @param buf the bugger to read the field from.
     */
    void deserialize(FriendlyByteBuf buf);

    /**
     * Get the distance to the building.
     *
     * @param building the building to check the distance to.
     * @return the distance as a full number.
     */
    int getDistance(IBuildingView building);

    /**
     * Generate a matcher for this field.
     *
     * @return the field record matcher.
     */
    IFieldMatcher getMatcher();

    /**
     * Hashcode implementation for this field.
     */
    int hashCode();

    /**
     * Equals implementation for this field.
     */
    boolean equals(Object other);
}
