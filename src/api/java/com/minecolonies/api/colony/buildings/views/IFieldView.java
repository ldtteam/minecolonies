package com.minecolonies.api.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
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
     * Deserialize a field from a buffer.
     *
     * @param fieldData the bugger to read the field from.
     */
    void deserialize(FriendlyByteBuf fieldData);
}
