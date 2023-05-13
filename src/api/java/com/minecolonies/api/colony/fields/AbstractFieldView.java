package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract implementation of the field it's view instance.
 */
public abstract class AbstractFieldView implements IFieldView
{
    /**
     * Colony owning the field.
     */
    private final @NotNull IColonyView colony;

    /**
     * The position of the field.
     */
    private final @NotNull BlockPos position;

    /**
     * The type of the field.
     */
    private FieldRegistries.FieldEntry fieldType = null;

    /**
     * Building id of the building owning the field.
     */
    private BlockPos buildingId;

    /**
     * Constructor used in deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    protected AbstractFieldView(@NotNull IColonyView colony, @NotNull BlockPos position)
    {
        this.colony = colony;
        this.position = position;
    }

    @Override
    public @NotNull FieldRegistries.FieldEntry getFieldType()
    {
        return fieldType;
    }

    @Override
    public void setFieldType(final FieldRegistries.@NotNull FieldEntry fieldType)
    {
        this.fieldType = fieldType;
    }

    @Override
    public final @NotNull IColonyView getColonyView()
    {
        return colony;
    }

    @Override
    @NotNull
    public final BlockPos getPosition()
    {
        return position;
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
    public void deserialize(final FriendlyByteBuf buf)
    {
        if (buf.readBoolean())
        {
            buildingId = buf.readBlockPos();
        }
    }

    @Override
    public int getDistance(final IBuildingView building)
    {
        return (int) Math.sqrt(BlockPosUtil.getDistanceSquared(position, building.getPosition()));
    }

    @Override
    public abstract @NotNull IFieldMatcher getMatcher();

    @Override
    public final int hashCode()
    {
        return getMatcher().hashCode();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj instanceof IFieldView field)
        {
            return getMatcher().matchesView(field);
        }

        return false;
    }
}
