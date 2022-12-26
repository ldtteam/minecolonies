package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The abstract implementation for plantation field tile entities.
 */
public abstract class AbstractTileEntityPlantationField extends BlockEntity
{
    /**
     * Default method.
     *
     * @param entityType the entity type.
     * @param pos        the positions this tile entity is at.
     * @param state      the state the entity is in.
     */
    protected AbstractTileEntityPlantationField(final BlockEntityType<? extends AbstractTileEntityPlantationField> entityType, final BlockPos pos, final BlockState state)
    {
        super(entityType, pos, state);
    }

    /**
     * Is this tile entity a valid field, meaning it has a field type set as well as at least one working position.
     *
     * @return true if so.
     */
    public final boolean isValidPlantationField()
    {
        return getPlantationFieldType() != null && !getWorkingPositions().isEmpty();
    }

    /**
     * The field type of this plantation.
     *
     * @return the field type.
     */
    public abstract PlantationFieldType getPlantationFieldType();

    /**
     * The working positions stored in this field.
     *
     * @return a list of working positions.
     */
    public abstract List<BlockPos> getWorkingPositions();
}
