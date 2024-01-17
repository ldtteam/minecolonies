package com.minecolonies.core.colony.fields;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
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
    private static final String TAG_WORKING_POS = "workingPositions";

    /**
     * A list of all found tagged working positions.
     */
    private List<BlockPos> workingPositions = new ArrayList<>();

    /**
     * Constructor used in NBT deserialization.
     *
     * @param fieldType the type of field.
     * @param position  the position of the field.
     */
    public PlantationField(final @NotNull FieldRegistries.FieldEntry fieldType, final @NotNull BlockPos position)
    {
        super(fieldType, position);
    }

    /**
     * Constructor to create new instances
     *
     * @param fieldEntry the type of field we want to produce.
     * @param position   the position it is placed in.
     */
    public static PlantationField create(final FieldRegistries.FieldEntry fieldEntry, final BlockPos position)
    {
        return (PlantationField) fieldEntry.produceField(position);
    }

    @Override
    public boolean isValidPlacement(final IColony colony)
    {
        BlockState blockState = colony.getWorld().getBlockState(getPosition());
        // TODO: future, remove `blockHutPlantation` from valid blocks
        return blockState.is(ModBlocks.blockHutPlantation) || blockState.is(ModBlocks.blockPlantationField);
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

    /**
     * Get the plantation module on this field.
     *
     * @return the plantation module instance.
     */
    public IPlantationModule getModule()
    {
        return getFirstModuleOccurance(IPlantationModule.class);
    }

    @Override
    public @NotNull CompoundTag serializeNBT()
    {
        CompoundTag compound = super.serializeNBT();
        BlockPosUtil.writePosListToNBT(compound, TAG_WORKING_POS, workingPositions);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag compound)
    {
        super.deserializeNBT(compound);
        workingPositions = BlockPosUtil.readPosListFromNBT(compound, TAG_WORKING_POS);
    }

    @Override
    public void serialize(final @NotNull FriendlyByteBuf buf)
    {
        super.serialize(buf);
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
        workingPositions = new ArrayList<>();
        final int workingPositionCount = buf.readInt();
        for (int index = 0; index < workingPositionCount; index++)
        {
            workingPositions.add(buf.readBlockPos());
        }
    }
}