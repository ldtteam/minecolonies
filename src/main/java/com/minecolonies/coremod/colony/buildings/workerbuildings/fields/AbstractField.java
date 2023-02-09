package com.minecolonies.coremod.colony.buildings.workerbuildings.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
     * Colony owning the field.
     */
    protected IColony colony;

    /**
     * Building id of the building owning the field.
     */
    @Nullable
    protected BlockPos buildingId;

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
    public void deserializeNBT(CompoundTag compound)
    {
        if (compound.contains(TAG_OWNER))
        {
            buildingId = BlockPosUtil.read(compound, TAG_OWNER);
        }
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
        if (buildingId != null)
        {
            BlockPosUtil.write(compound, TAG_OWNER, buildingId);
        }
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
        fieldData.writeBoolean(buildingId != null);
        if (buildingId != null)
        {
            fieldData.writeBlockPos(buildingId);
        }
        fieldData.writeBlockPos(position);
        fieldData.writeBoolean(plant != null);
        if (plant != null)
        {
            fieldData.writeItem(new ItemStack(plant));
        }
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

        final AbstractField that = (AbstractField) o;

        return Objects.equals(this.getMatcher(), that.getMatcher());
    }
}
