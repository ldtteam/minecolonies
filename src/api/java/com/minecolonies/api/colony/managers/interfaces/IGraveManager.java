package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
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
    void read(@NotNull final CompoundNBT compound);

    /**
     * Write the graves to NBT.
     *
     * @param compound the compound.
     */
    void write(@NotNull final CompoundNBT compound);

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
     * Reserve the next free grave
     *
     * @return the grave successfully reserved or null if none available
     */
    BlockPos reserveNextFreeGrave();

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
