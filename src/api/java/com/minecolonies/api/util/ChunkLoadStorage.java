package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The chunkload storage used to load chunks with colony information.
 */
public class ChunkLoadStorage
{
    /**
     * NBT tag for colonies to add.
     */
    private static final String TAG_COLONIES_TO_ADD = "coloniesToAdd";

    /**
     * NBT tag for colonies to remove.
     */
    private static final String TAG_COLONIES_TO_REMOVE = "coloniesToRemove";

    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The list of colonies to be added to this loc.
     */
    private final List<Integer> coloniesToRemove = new ArrayList<>();

    /**
     * The list of colonies to be removed from this loc.
     */
    private final List<Integer> coloniesToAdd = new ArrayList<>();

    /**
     * XZ pos as long.
     */
    private final long xz;

    /**
     * The dimension of the chunk.
     */
    private final int dimension;

    /**
     * The building claiming this.
     */
    private final List<Tuple<Integer, BlockPos>> claimingBuilding = new ArrayList<>();

    /**
     * The building unclaiming this.
     */
    private final List<Tuple<Integer, BlockPos>> unClaimingBuilding = new ArrayList<>();

    /**
     * Intitialize a ChunLoadStorage from nbt.
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final CompoundNBT compound)
    {
        this.colonyId = compound.getInt(TAG_ID);
        this.xz = compound.getLong(TAG_POS);
        this.dimension = compound.getInt(TAG_DIMENSION);

        coloniesToAdd.addAll(NBTUtils.streamCompound(compound.getList(TAG_COLONIES_TO_ADD, Constants.NBT.TAG_COMPOUND))
                .map(tempCompound -> tempCompound.getInt(TAG_COLONY_ID)).collect(Collectors.toList()));
        coloniesToRemove.addAll(NBTUtils.streamCompound(compound.getList(TAG_COLONIES_TO_REMOVE, Constants.NBT.TAG_COMPOUND))
                .map(tempCompound -> tempCompound.getInt(TAG_COLONY_ID)).collect(Collectors.toList()));
        claimingBuilding.addAll(NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_CLAIM, Constants.NBT.TAG_COMPOUND))
                                  .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
        unClaimingBuilding.addAll(NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_UNCLAIM, Constants.NBT.TAG_COMPOUND))
                                  .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
    }

    /**
     * Create a new chunkload storage.
     * @param colonyId the id of the colony.
     * @param xz the chunk xz.
     * @param add the operation type.
     * @param dimension the dimension.
     * @param owning if the colony should own the chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final boolean add, final int dimension, final boolean owning)
    {
        this.colonyId = owning ? colonyId : 0;
        this.xz = xz;
        this.dimension = dimension;
        if(add)
        {
            coloniesToAdd.add(colonyId);
        }
        else
        {
            coloniesToRemove.add(colonyId);
        }
    }

    /**
     * Create a new chunkload storage.
     * @param colonyId the id of the colony.
     * @param xz the chunk xz.
     * @param dimension the dimension.
     * @param building the building claiming this chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final int dimension, final BlockPos building)
    {
        this.xz = xz;
        this.dimension = dimension;
        this.claimingBuilding.add(new Tuple<>(colonyId, building));
    }

    /**
     * Write the ChunkLoadStorage to NBT.
     * @return the compound.
     */
    public CompoundNBT toNBT()
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_ID, colonyId);
        compound.setLong(TAG_POS, xz);
        compound.putInt(TAG_DIMENSION, dimension);

        compound.put(TAG_COLONIES_TO_ADD, coloniesToAdd.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toListNBT()));
        compound.put(TAG_COLONIES_TO_REMOVE, coloniesToRemove.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toListNBT()));
        compound.put(TAG_BUILDINGS, claimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toListNBT()));
        compound.put(TAG_BUILDINGS, unClaimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toListNBT()));

        return compound;
    }

    private static CompoundNBT getCompoundOfColonyId(final int id)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_COLONY_ID, id);
        return compound;
    }

    /**
     * Getter for the colonyId.
     * @return the id.
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Getter for the dimension.
     * @return the dimension id.
     */
    public int getDimension()
    {
        return dimension;
    }

    /**
     * Get the x long.
     * @return the long representing two integers.
     */
    public long getXz()
    {
        return xz;
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
        final ChunkLoadStorage storage = (ChunkLoadStorage) o;
        return colonyId == storage.colonyId &&
                xz == storage.xz &&
                dimension == storage.dimension &&
                Objects.equals(coloniesToRemove, storage.coloniesToRemove) &&
                Objects.equals(coloniesToAdd, storage.coloniesToAdd) &&
                Objects.equals(claimingBuilding, storage.claimingBuilding) &&
                Objects.equals(unClaimingBuilding, storage.unClaimingBuilding);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(colonyId, coloniesToRemove, coloniesToAdd, xz, dimension, claimingBuilding, unClaimingBuilding);
    }

    /**
     * Apply this ChunkLoadStorage to a capability.
     * @param cap the capability to apply it to.
     */
    public void applyToCap(final IColonyTagCapability cap)
    {
        if (this.claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty())
        {
            if (this.getColonyId() > 0)
            {
                cap.setOwningColony(this.colonyId);
            }

            for (final int tempColonyId : coloniesToAdd)
            {
                cap.addColony(tempColonyId);
            }

            for (final int tempColonyId : coloniesToRemove)
            {
                cap.removeColony(tempColonyId);
            }
        }
        else
        {
            for (final Tuple<Integer, BlockPos> tuple : unClaimingBuilding)
            {
                cap.removeBuildingClaim(tuple.getA(), tuple.getB());
            }

            for (final Tuple<Integer, BlockPos> tuple : claimingBuilding)
            {
                cap.addBuildingClaim(tuple.getA(), tuple.getB());
            }
        }
    }

    /**
     * Check if the chunkloadstorage is empty.
     * @return true if so.
     */
    public boolean isEmpty()
    {
        return coloniesToAdd.isEmpty() && coloniesToRemove.isEmpty();
    }

    /**
     * Merge the two Chunkstorages into one.
     * The newer one is considered to be the "more up to date" version.
     * @param newStorage the new version to add.
     */
    public void merge(final ChunkLoadStorage newStorage)
    {
        if (this.claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty())
        {
            for (final int tempColonyId : newStorage.coloniesToAdd)
            {
                if (this.coloniesToRemove.contains(tempColonyId))
                {
                    this.coloniesToRemove.remove(new Integer(tempColonyId));
                }
                else if (!this.coloniesToAdd.contains(tempColonyId))
                {
                    this.coloniesToAdd.add(tempColonyId);
                }
            }

            for (final int tempColonyId : newStorage.coloniesToRemove)
            {
                if (this.colonyId == tempColonyId)
                {
                    this.colonyId = 0;
                }

                if (this.coloniesToAdd.contains(tempColonyId))
                {
                    this.coloniesToAdd.remove(new Integer(tempColonyId));
                }
                else if (!this.coloniesToRemove.contains(tempColonyId))
                {
                    this.coloniesToRemove.add(tempColonyId);
                }
            }

            if (newStorage.getColonyId() > 0 || !newStorage.coloniesToRemove.isEmpty())
            {
                this.colonyId = newStorage.getColonyId();
            }
        }
        else
        {
            this.claimingBuilding.removeIf(newStorage.unClaimingBuilding::contains);
            this.unClaimingBuilding.removeIf(newStorage.claimingBuilding::contains);

            for (final Tuple<Integer, BlockPos> tuple : newStorage.unClaimingBuilding)
            {
                if (!this.unClaimingBuilding.contains(tuple))
                {
                    this.unClaimingBuilding.add(tuple);
                }
            }

            for (final Tuple<Integer, BlockPos> tuple : newStorage.claimingBuilding)
            {
                if (!this.claimingBuilding.contains(tuple))
                {
                    this.claimingBuilding.add(tuple);
                }
            }
        }
    }

    /**
     * Write the tuple to NBT.
     * @param tuple the tuple to write.
     * @return the resulting compound.
     */
    private static CompoundNBT writeTupleToNBT(final Tuple<Integer, BlockPos> tuple)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_COLONY_ID, tuple.getA());
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, tuple.getB());
        return compound;
    }

    /**
     * Read the tuple from NBT.
     * @param compound the compound to extract it from.
     * @return the tuple.
     */
    private static Tuple<Integer, BlockPos> readTupleFromNbt(final CompoundNBT compound)
    {
        return new Tuple<>(compound.getInt(TAG_COLONY_ID), BlockPosUtil.readFromNBT(compound, TAG_BUILDING));
    }
}
