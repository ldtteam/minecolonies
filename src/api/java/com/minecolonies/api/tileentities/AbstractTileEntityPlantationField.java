package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Check condition whether the field UI can be opened or not.
     *
     * @param player the player attempting to open the menu.
     * @return whether the player is authorized to open this menu.
     */
    public abstract boolean canOpenMenu(@NotNull Player player);

    /**
     * Gets the schematic name, required to be saved
     *
     * @return schematic name
     */
    public abstract String getSchematicName();

    /**
     * Get the dimension this plantation field is placed in.
     *
     * @return the dimension key.
     */
    @Nullable
    public abstract ResourceKey<Level> getDimension();
}
