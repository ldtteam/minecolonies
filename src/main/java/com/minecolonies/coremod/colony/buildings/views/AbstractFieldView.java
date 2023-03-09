package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
     * Building id of the building owning the field.
     */
    protected BlockPos buildingId;

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
    public final BlockPos getBuildingId()
    {
        return buildingId;
    }

    @Override
    public void setBuilding(final BlockPos buildingId)
    {
        this.buildingId = buildingId;
    }

    @Override
    public final void resetOwningBuilding()
    {
        buildingId = null;
    }

    @Override
    public final boolean isTaken()
    {
        return buildingId != null;
    }

    @Override
    public void deserialize(final FriendlyByteBuf fieldData)
    {
        if (fieldData.readBoolean())
        {
            buildingId = fieldData.readBlockPos();
        }
        position = fieldData.readBlockPos();
        if (fieldData.readBoolean())
        {
            plant = fieldData.readItem().getItem();
        }
    }

    @Override
    public int getDistance(final IBuildingView building)
    {
        return (int) Math.sqrt(BlockPosUtil.getDistanceSquared(position, building.getPosition()));
    }

    @Override
    public final FieldRecord getMatcher()
    {
        return new FieldRecord(position, plant);
    }

    @Override
    public final boolean matches(final FieldRecord matcher)
    {
        return position.equals(matcher.position()) && Objects.equals(plant, matcher.plant());
    }

    @Override
    public int hashCode()
    {
        return this.getMatcher().hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractFieldView that = (AbstractFieldView) o;

        return Objects.equals(this.getMatcher(), that.getMatcher());
    }
}