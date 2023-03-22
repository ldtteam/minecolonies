package com.minecolonies.coremod.colony.buildings.workerbuildings.fields;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.views.AbstractFieldView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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
    private PlantationFieldType plantationFieldType;

    /**
     * A list of all found tagged working positions.
     */
    private List<BlockPos> workingPositions = new ArrayList<>();

    /**
     * Constructor used in NBT deserialization.
     *
     * @param colony the colony this field belongs to.
     */
    public PlantationField(final IColony colony)
    {
        super(colony);
    }

    /**
     * Constructor to create new instances
     *
     * @param colony           the colony it is created in.
     * @param position         the position it is placed in.
     * @param type             the plantation field type.
     * @param plant            the plant the plantation manages.
     * @param workingPositions the list of positions this field can be worked on.
     */
    public PlantationField(final IColony colony, final BlockPos position, final PlantationFieldType type, final Item plant, final List<BlockPos> workingPositions)
    {
        super(colony);
        this.position = position;
        this.plantationFieldType = type;
        this.plant = plant;
        this.workingPositions = workingPositions;
    }

    @Override
    public FieldType getType()
    {
        return FieldType.PLANTATION_FIELDS;
    }

    @Override
    public boolean needsWork()
    {
        PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationFieldType);
        if (module != null)
        {
            return module.needsWork(this);
        }
        return false;
    }

    /**
     * Get the plantation field type of this field.
     *
     * @return the field type.
     */
    public PlantationFieldType getPlantationFieldType()
    {
        return plantationFieldType;
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
    public void deserializeNBT(CompoundTag compound)
    {
        super.deserializeNBT(compound);
        plantationFieldType = Enum.valueOf(PlantationFieldType.class, compound.getString(TAG_FIELD_TYPE));
        workingPositions = BlockPosUtil.readPosListFromNBT(compound, TAG_WORKING_POS);
    }

    @Override
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = super.serializeNBT();
        compound.putString(TAG_FIELD_TYPE, plantationFieldType.name());
        BlockPosUtil.writePosListToNBT(compound, TAG_WORKING_POS, workingPositions);
        return compound;
    }

    @Override
    public void serializeToView(final FriendlyByteBuf fieldData)
    {
        super.serializeToView(fieldData);
        fieldData.writeEnum(plantationFieldType);
        fieldData.writeInt(workingPositions.size());
        for (BlockPos workingPosition : workingPositions)
        {
            fieldData.writeBlockPos(workingPosition);
        }
    }

    @Override
    public boolean isValidPlacement()
    {
        BlockState blockState = colony.getWorld().getBlockState(position);
        // TODO: 1.20, remove `blockHutPlantation` from valid blocks
        return blockState.is(ModBlocks.blockHutPlantation) || blockState.is(ModBlocks.blockPlantationField);
    }

    /**
     * View class for the {@link PlantationField}.
     */
    public static class View extends AbstractFieldView
    {
        /**
         * The plantation field type.
         */
        private PlantationFieldType plantationFieldType;

        /**
         * A list of all found tagged working positions.
         */
        private List<BlockPos> workingPositions = new ArrayList<>();

        /**
         * Default constructor.
         */
        protected View(final IColonyView colony)
        {
            super(colony);
        }

        @Override
        public void deserialize(final FriendlyByteBuf fieldData)
        {
            super.deserialize(fieldData);
            plantationFieldType = fieldData.readEnum(PlantationFieldType.class);
            workingPositions = new ArrayList<>();
            final int size = fieldData.readInt();
            for (int i = 0; i < size; i++)
            {
                workingPositions.add(fieldData.readBlockPos());
            }
        }

        @Override
        public @NotNull FieldType getType()
        {
            return FieldType.PLANTATION_FIELDS;
        }

        /**
         * Get the plantation field type of this field.
         *
         * @return the field type.
         */
        public PlantationFieldType getPlantationFieldType()
        {
            return plantationFieldType;
        }

        /**
         * Get the list of working positions of this field.
         *
         * @return an unmodifiable collection of working positions.
         */
        public Collection<BlockPos> getWorkingPositions()
        {
            return workingPositions.stream().toList();
        }
    }
}