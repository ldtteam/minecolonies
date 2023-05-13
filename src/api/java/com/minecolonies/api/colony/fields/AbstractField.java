package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OWNER;

/**
 * Abstract implementation for field instances.
 * Contains some basic mandatory logic for fields.
 */
public abstract class AbstractField implements IField
{
    /**
     * Colony owning the field.
     */
    private final IColony colony;

    /**
     * The position of the field.
     */
    private final BlockPos position;

    /**
     * The type of the field.
     */
    private FieldRegistries.FieldEntry fieldType = null;

    /**
     * Building id of the building owning the field.
     */
    @Nullable
    private BlockPos buildingId = null;

    /**
     * Constructor used in NBT deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    protected AbstractField(@NotNull IColony colony, @NotNull BlockPos position)
    {
        this.colony = colony;
        this.position = position;
    }

    @Override
    public final @NotNull FieldRegistries.FieldEntry getFieldType()
    {
        return fieldType;
    }

    @Override
    public final void setFieldType(final FieldRegistries.@NotNull FieldEntry fieldType)
    {
        this.fieldType = fieldType;
    }

    @Override
    @NotNull
    public final BlockPos getPosition()
    {
        return position;
    }

    @Override
    public final IColony getColony()
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
    public final void setBuilding(final BlockPos buildingId)
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
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        if (buildingId != null)
        {
            BlockPosUtil.write(compound, TAG_OWNER, buildingId);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag compound)
    {
        if (compound.contains(TAG_OWNER))
        {
            buildingId = BlockPosUtil.read(compound, TAG_OWNER);
        }
    }

    @Override
    public void serializeToView(final @NotNull FriendlyByteBuf buf)
    {
        buf.writeBoolean(buildingId != null);
        if (buildingId != null)
        {
            buf.writeBlockPos(buildingId);
        }
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
        if (obj instanceof IField field)
        {
            return getMatcher().matches(field);
        }

        return false;
    }
}
