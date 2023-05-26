package com.minecolonies.coremod.colony.fields;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.colony.fields.AbstractField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.fields.plantation.registry.PlantationFieldRegistries;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Field class implementation for the plantation
 */
public class PlantationField extends AbstractField
{
    private static final String TAG_FIELD_TYPE  = "plantationFieldType";
    private static final String TAG_WORKING_POS = "workingPositions";

    /**
     * The plantation field type.
     */
    private PlantationFieldRegistries.FieldEntry plantationFieldType;

    /**
     * A list of all found tagged working positions.
     */
    private List<BlockPos> workingPositions = new ArrayList<>();

    /**
     * Constructor used in NBT deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    public PlantationField(final IColony colony, final BlockPos position)
    {
        super(colony, position);
    }

    /**
     * Constructor to create new instances
     *
     * @param colony           the colony it is created in.
     * @param position         the position it is placed in.
     * @param type             the plantation field type.
     * @param workingPositions the list of positions this field can be worked on.
     */
    public static PlantationField create(final IColony colony, final BlockPos position, final PlantationFieldRegistries.FieldEntry type, final List<BlockPos> workingPositions)
    {
        PlantationField field = (PlantationField) FieldRegistries.plantationField.get().produceField(colony, position);
        field.setPlantationFieldType(type);
        field.setWorkingPositions(workingPositions);
        return field;
    }

    @Override
    public boolean needsWork()
    {
        IPlantationModule module = plantationFieldType.getModule();
        if (module != null)
        {
            return module.needsWork(this);
        }
        return false;
    }

    @Override
    public boolean isValidPlacement()
    {
        BlockState blockState = getColony().getWorld().getBlockState(getPosition());
        // TODO: 1.20, remove `blockHutPlantation` from valid blocks
        return blockState.is(ModBlocks.blockHutPlantation) || blockState.is(ModBlocks.blockPlantationField);
    }

    /**
     * Get the plantation field type of this field.
     *
     * @return the field type.
     */
    public PlantationFieldRegistries.FieldEntry getPlantationFieldType()
    {
        return plantationFieldType;
    }

    /**
     * Set the plantation field type of this field.
     *
     * @param plantationFieldType the field type.
     */
    public void setPlantationFieldType(final PlantationFieldRegistries.FieldEntry plantationFieldType)
    {
        this.plantationFieldType = plantationFieldType;
    }

    /**
     * Get the list of working positions of this field.
     *
     * @return an unmodifiable collection of working positions.
     */
    public List<BlockPos> getWorkingPositions()
    {
        return workingPositions.stream().toList();
    }

    /**
     * Overwrite the working positions on the field instance.
     *
     * @param workingPositions the new list of working positions.
     */
    public void setWorkingPositions(final List<BlockPos> workingPositions)
    {
        this.workingPositions = workingPositions;
    }

    @Override
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = super.serializeNBT();
        compound.putString(TAG_FIELD_TYPE, plantationFieldType.getRegistryName().toString());
        BlockPosUtil.writePosListToNBT(compound, TAG_WORKING_POS, workingPositions);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag compound)
    {
        super.deserializeNBT(compound);
        switch (Enum.valueOf(PlantationFieldType.class, compound.getString(TAG_FIELD_TYPE)))
        {
            case SUGAR_CANE -> plantationFieldType = PlantationFieldRegistries.sugarCaneField.get();
            case CACTUS -> plantationFieldType = PlantationFieldRegistries.cactusField.get();
            case BAMBOO -> plantationFieldType = PlantationFieldRegistries.bambooField.get();
            case COCOA_BEANS -> plantationFieldType = PlantationFieldRegistries.cocoaBeansField.get();
            case VINES -> plantationFieldType = PlantationFieldRegistries.vinesField.get();
            case KELP -> plantationFieldType = PlantationFieldRegistries.kelpField.get();
            case SEAGRASS -> plantationFieldType = PlantationFieldRegistries.seagrassField.get();
            case SEA_PICKLES -> plantationFieldType = PlantationFieldRegistries.seaPicklesField.get();
            case GLOWBERRIES -> plantationFieldType = PlantationFieldRegistries.glowberriesField.get();
            case WEEPING_VINES -> plantationFieldType = PlantationFieldRegistries.weepingVinesField.get();
            case TWISTING_VINES -> plantationFieldType = PlantationFieldRegistries.twistingVinesField.get();
            case CRIMSON_FUNGUS -> plantationFieldType = PlantationFieldRegistries.crimsonPlantsField.get();
            case WARPED_FUNGUS -> plantationFieldType = PlantationFieldRegistries.warpedPlantsField.get();
        }
        //plantationFieldType = PlantationFieldRegistries.getPlantationFieldRegistry().getValue(new ResourceLocation(
        workingPositions = BlockPosUtil.readPosListFromNBT(compound, TAG_WORKING_POS);
    }

    @Override
    public void serialize(final @NotNull FriendlyByteBuf buf)
    {
        super.serialize(buf);
        buf.writeRegistryId(PlantationFieldRegistries.getPlantationFieldRegistry(), plantationFieldType);
        buf.writeInt(workingPositions.size());
        for (BlockPos workingPosition : workingPositions)
        {
            buf.writeBlockPos(workingPosition);
        }
    }

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        plantationFieldType = buf.readRegistryIdSafe(PlantationFieldRegistries.FieldEntry.class);
        workingPositions = new ArrayList<>();
        final int workingPositionCount = buf.readInt();
        for (int index = 0; index < workingPositionCount; index++)
        {
            workingPositions.add(buf.readBlockPos());
        }
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + plantationFieldType.hashCode();
        result = 31 * result + workingPositions.hashCode();
        return result;
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
        if (!super.equals(o))
        {
            return false;
        }

        final PlantationField that = (PlantationField) o;

        if (!plantationFieldType.equals(that.plantationFieldType))
        {
            return false;
        }
        return workingPositions.equals(that.workingPositions);
    }
}