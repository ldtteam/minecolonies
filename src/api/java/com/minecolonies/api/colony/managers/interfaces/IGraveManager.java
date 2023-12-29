package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Interface for grave managers.
 */
public interface IGraveManager
{
    /**
     * Read the graves from NBT.
     *
     * @param compound the compound.
     */
    void read(@NotNull final CompoundTag compound);

    /**
     * Write the graves to NBT.
     *
     * @param compound the compound.
     */
    void write(@NotNull final CompoundTag compound);

    /**
     * Tick the graves on colony tick.
     *
     * @param colony the event.
     */
    void onColonyTick(IColony colony);

    /**
     * Reserve a grave
     *
     * @param pos the id of the grave.
     * @return is the grave successfully reserved.
     */
    boolean reserveGrave(BlockPos pos);

    /**
     * Un-Reserve a grave
     *
     * @param pos the id of the grave.
     */
    void unReserveGrave(BlockPos pos);

    /**
     * Reserve the next free grave
     *
     * @return the grave successfully reserved or null if none available
     */
    BlockPos reserveNextFreeGrave();

    /**
     * Attempt to create a TileEntityGrave at @pos containing the specific @citizenData
     *
     * On failure: drop all the citizen inventory on the ground.
     *
     * @param world        The world.
     * @param pos          The position where to spawn a grave
     * @param citizenData  The citizenData
     */
    void createCitizenGrave(final Level world, final BlockPos pos, final ICitizenData citizenData);

    /**
     * Returns a map with all graves within the colony. Key is ID (Coordinates), value is isReserved boolean.
     *
     * @return Map with ID (coordinates) as key, value is isReserved boolean.
     */
    @NotNull
    Map<BlockPos, Boolean> getGraves();

    /**
     * Add a grave from the Colony.
     *
     * @param pos    position of the TileEntityGrave to add.
     * @return the grave that was created and added.
     */
    @Nullable
    boolean addNewGrave(@NotNull final BlockPos pos);

    /**
     * Remove a TileEntityGrave from the Colony (when it is destroyed).
     *
     * @param pos    position of the TileEntityGrave to remove.
     */
    void removeGrave(@NotNull final BlockPos pos);
}
