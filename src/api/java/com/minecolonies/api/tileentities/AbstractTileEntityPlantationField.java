package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

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
     * The field type of this plantation.
     *
     * @return the field type.
     */
    public abstract Set<PlantationFieldType> getPlantationFieldTypes();

    /**
     * The working positions stored in this field.
     *
     * @param tag the tag to search for.
     * @return a list of working positions.
     */
    public abstract List<BlockPos> getWorkingPositions(String tag);
}
