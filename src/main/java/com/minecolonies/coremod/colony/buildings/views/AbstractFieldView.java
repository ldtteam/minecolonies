package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client only class for the field which can be used to show it on the field window.
 */
public abstract class AbstractFieldView implements IFieldView
{
    /**
     * Colony owning the field.
     */
    protected IColonyView colony;

    /**
     * Citizen id of the citizen owning the field.
     */
    protected int ownerId;

    /**
     * The position of the field.
     */
    protected BlockPos position;

    /**
     * The plant currently stored in the field.
     */
    protected Item plant;

    /**
     * Constructor used in deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    protected AbstractFieldView(IColonyView colony)
    {
        this.colony = colony;
    }

    @Override
    @NotNull
    public final BlockPos getPosition()
    {
        return position;
    }

    @Override
    public final Item getPlant()
    {
        return plant;
    }

    /**
     * Set the plant for this farm field.
     *
     * @param plant the new plant.
     */
    public final void setPlant(Item plant)
    {
        this.plant = plant;
    }

    @Override
    public final IColonyView getColonyView()
    {
        return colony;
    }

    @Override
    @Nullable
    public final Integer getOwnerId()
    {
        return ownerId != 0 ? ownerId : null;
    }

    @Override
    public final void setOwner(final int ownerId)
    {
        this.ownerId = ownerId;
    }

    @Override
    public final void resetOwner()
    {
        ownerId = 0;
    }

    @Override
    public final boolean isTaken()
    {
        return ownerId != 0;
    }

    @Override
    public void deserialize(final FriendlyByteBuf fieldData)
    {
        ownerId = fieldData.readInt();
        position = fieldData.readBlockPos();
        if (fieldData.readBoolean())
        {
            plant = fieldData.readItem().getItem();
        }
    }
}
