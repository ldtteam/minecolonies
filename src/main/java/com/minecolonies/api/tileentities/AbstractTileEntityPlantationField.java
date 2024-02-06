package com.minecolonies.api.tileentities;

import com.ldtteam.structurize.api.IRotatableBlockEntity;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * The abstract implementation for plantation field tile entities.
 */
public abstract class AbstractTileEntityPlantationField extends BlockEntity implements IBlueprintDataProviderBE, IRotatableBlockEntity
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
    public abstract Set<FieldRegistries.FieldEntry> getPlantationFieldTypes();

    /**
     * The working positions stored in this field.
     *
     * @param tag the tag to search for.
     * @return a list of working positions.
     */
    public abstract List<BlockPos> getWorkingPositions(String tag);

    /**
     * The colony this field is located in.
     *
     * @return the colony instance.
     */
    public abstract IColony getCurrentColony();

    /**
     * Get the dimension this plantation field is placed in.
     *
     * @return the dimension key.
     */
    @Nullable
    public abstract ResourceKey<Level> getDimension();

    @Nullable
    @Override
    public abstract ClientboundBlockEntityDataPacket getUpdatePacket();

    /**
     * Get the rotation of the controller.
     * @return the placed rotation.
     */
    public abstract Rotation getRotation();

    /**
     * Get the mirroring setting of the controller.
     * @return true if mirrored.
     */
    public abstract boolean getMirror();
}