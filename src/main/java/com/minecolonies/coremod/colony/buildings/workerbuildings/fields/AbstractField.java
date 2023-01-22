package com.minecolonies.coremod.colony.buildings.workerbuildings.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldStructureType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LOCATION;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OWNER;

/**
 * Abstract class for the field class instances.
 * Provides basic abstraction layer that implements the core functionality of a field,
 * can be further extended by specific implementations to add additional logic to a field type.
 */
public abstract class AbstractField implements IField
{
    private static final String TAG_PLANT = "plant";

    /**
     * Type of the field.
     */
    protected FieldStructureType type;

    /**
     * Colony owning the field.
     */
    protected IColony colony;

    /**
     * Citizen id of the citizen owning the field.
     */
    protected int ownerId;

    /**
     * The position of the field.
     */
    protected BlockPos position;

    /**
     * The plant of the field.
     */
    protected Item plant;

    /**
     * Constructor used in NBT deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    protected AbstractField(IColony colony)
    {
        this.type = getType();
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

    @Override
    public final void setPlant(Item plant)
    {
        this.plant = plant;
    }

    @Override
    public final IColony getColony()
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
    public void deserializeNBT(CompoundTag compound)
    {
        ownerId = compound.getInt(TAG_OWNER);
        position = BlockPosUtil.read(compound, TAG_LOCATION);
        if (compound.contains(TAG_PLANT))
        {
            plant = ItemStack.of(compound.getCompound(TAG_PLANT)).getItem();
        }
    }

    @Override
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_OWNER, ownerId);
        BlockPosUtil.write(compound, TAG_LOCATION, position);
        if (plant != null)
        {
            CompoundTag plantCompound = new CompoundTag();
            new ItemStack(plant).save(plantCompound);
            compound.put(TAG_PLANT, plantCompound);
        }
        return compound;
    }

    @Override
    public void serializeToView(final FriendlyByteBuf fieldData)
    {
        fieldData.writeInt(ownerId);
        fieldData.writeBlockPos(position);
        fieldData.writeBoolean(plant != null);
        if (plant != null)
        {
            fieldData.writeItem(new ItemStack(plant));
        }
    }

    @Override
    public final int hashCode()
    {
        return position.hashCode();
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractField that = (AbstractField) o;

        return position.equals(that.position);
    }
}
