package com.minecolonies.api.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for any field view instance.
 */
public interface IFieldView
{
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
     * Getter for the colony of the field.
     *
     * @return the int id.
     */
    IColonyView getColonyView();

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
     * @param fieldData the bugger to read the field from.
     */
    void deserialize(FriendlyByteBuf fieldData);

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
    FieldRecord getMatcher();

    /**
     * Whether this field matches the provided field record matcher.
     *
     * @param matcher the field record matcher.
     * @return true if so.
     */
    boolean matches(FieldRecord matcher);

    /**
     * Get the type for this field view.
     *
     * @return the field type.
     */
    @NotNull
    FieldType getType();
}
