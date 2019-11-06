package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
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
     * NBT tag for claims to add
     */
    public static final String TAG_CLAIM_LIST = "claimsToAdd";

    /**
     * NBT tag for colonies to add.
     */
    private static final String TAG_COLONIES_TO_ADD = "coloniesToAdd";

    /**
     * NBT tag for colonies to remove.
     */
    private static final String TAG_COLONIES_TO_REMOVE = "coloniesToRemove";

    /**
     * The max amount of claim caches we stack
     */
    private static final int MAX_CHUNK_CLAIMS = 20;

    /**
     * The colony id.
     */
    private final List<Short> colonyId = new ArrayList<>();

    /**
     * The list of colonies to be added to this loc.
     */
    private final List<Short> coloniesToRemove = new ArrayList<>();

    /**
     * The list of colonies to be removed from this loc.
     */
    private final List<Short> coloniesToAdd = new ArrayList<>();

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
    private final List<Tuple<Short, BlockPos>> claimingBuilding = new ArrayList<>();

    /**
     * The building unclaiming this.
     */
    private final List<Tuple<Short, BlockPos>> unClaimingBuilding = new ArrayList<>();

    /**
     * Intitialize a ChunLoadStorage from nbt.
     *
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_ID))
        {
            this.colonyId.add((short)compound.getInteger(TAG_ID));
        }

        this.xz = compound.getLong(TAG_POS);
        this.dimension = compound.getInteger(TAG_DIMENSION);

        colonyId.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_CLAIM_LIST, Constants.NBT.TAG_COMPOUND))
                          .map(tempCompound -> tempCompound.getShort(TAG_COLONY_ID)).collect(Collectors.toList()));
        coloniesToAdd.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_COLONIES_TO_ADD, Constants.NBT.TAG_COMPOUND))
                               .map(tempCompound -> tempCompound.getShort(TAG_COLONY_ID)).collect(Collectors.toList()));
        coloniesToRemove.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_COLONIES_TO_REMOVE, Constants.NBT.TAG_COMPOUND))
                                  .map(tempCompound -> tempCompound.getShort(TAG_COLONY_ID)).collect(Collectors.toList()));

        claimingBuilding.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_BUILDINGS_CLAIM, Constants.NBT.TAG_COMPOUND))
                                  .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
        unClaimingBuilding.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_BUILDINGS_UNCLAIM, Constants.NBT.TAG_COMPOUND))
                                    .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
    }

    /**
     * Create a new chunkload storage.
     *
     * @param colonyId  the id of the colony.
     * @param xz        the chunk xz.
     * @param add       the operation type.
     * @param dimension the dimension.
     * @param owning    if the colony should own the chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final boolean add, final int dimension, final boolean owning)
    {
        this.colonyId.add((short)(owning && add ? colonyId : 0));
        this.xz = xz;
        this.dimension = dimension;

        if (add)
        {
            coloniesToAdd.add((short)colonyId);
            coloniesToRemove.add((short)0);
        }
        else
        {
            coloniesToAdd.add((short)0);
            coloniesToRemove.add((short)colonyId);
        }
    }

    /**
     * Create a new chunkload storage.
     *
     * @param colonyId  the id of the colony.
     * @param xz        the chunk xz.
     * @param dimension the dimension.
     * @param building  the building claiming this chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final int dimension, final BlockPos building)
    {
        this.xz = xz;
        this.dimension = dimension;
        this.claimingBuilding.add(new Tuple<>((short)colonyId, building));
    }

    /**
     * Write the ChunkLoadStorage to NBT.
     *
     * @return the compound.
     */
    public NBTTagCompound toNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong(TAG_POS, xz);
        compound.setInteger(TAG_DIMENSION, dimension);

        compound.setTag(TAG_CLAIM_LIST, colonyId.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toNBTTagList()));
        compound.setTag(TAG_COLONIES_TO_ADD, coloniesToAdd.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toNBTTagList()));
        compound.setTag(TAG_COLONIES_TO_REMOVE, coloniesToRemove.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toNBTTagList()));
        compound.setTag(TAG_BUILDINGS, claimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toNBTTagList()));
        compound.setTag(TAG_BUILDINGS, unClaimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toNBTTagList()));

        return compound;
    }

    private static NBTTagCompound getCompoundOfColonyId(final int id)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_COLONY_ID, id);
        return compound;
    }

    /**
     * Getter for the dimension.
     *
     * @return the dimension id.
     */
    public int getDimension()
    {
        return dimension;
    }

    /**
     * Get the x long.
     *
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
        return   xz == storage.xz &&
                 dimension == storage.dimension &&
                 Objects.equals(colonyId, storage.colonyId) &&
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
     *
     * @param cap the capability to apply it to.
     */
    public void applyToCap(final IColonyTagCapability cap, final Chunk chunk)
    {
        if (this.claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty())
        {
            final int amountOfOperations = Math.max(Math.max(colonyId.size(),coloniesToAdd.size()),coloniesToRemove.size());

            for (int i = 0; i < amountOfOperations; i++)
            {
                if (i < colonyId.size())
                {
                    final int claimID = colonyId.get(i);
                    if (claimID > 0)
                    {
                        cap.setOwningColony(claimID, chunk);
                    }
                }

                if (i < coloniesToAdd.size() && coloniesToAdd.get(i) > 0)
                {
                    cap.addColony(coloniesToAdd.get(i), chunk);
                }

                if (i < coloniesToRemove.size() && coloniesToRemove.get(i) > 0)
                {
                    cap.removeColony(coloniesToRemove.get(i), chunk);
                }
            }
        }
        else
        {
            for (final Tuple<Short, BlockPos> tuple : unClaimingBuilding)
            {
                cap.removeBuildingClaim(tuple.getFirst(), tuple.getSecond(), chunk);
            }

            for (final Tuple<Short, BlockPos> tuple : claimingBuilding)
            {
                cap.addBuildingClaim(tuple.getFirst(), tuple.getSecond(), chunk);
            }
        }
        chunk.markDirty();
    }

    /**
     * Check if the chunkloadstorage is empty.
     *
     * @return true if so.
     */
    public boolean isEmpty()
    {
        return coloniesToAdd.isEmpty() && coloniesToRemove.isEmpty();
    }

    /**
     * Merge the two Chunkstorages into one. The newer one is considered to be the "more up to date" version.
     *
     * @param newStorage the new version to add.
     */
    public void merge(final ChunkLoadStorage newStorage)
    {
        if (this.claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty())
        {
            colonyId.addAll(newStorage.colonyId);
            coloniesToAdd.addAll(newStorage.coloniesToAdd);
            coloniesToRemove.addAll(newStorage.coloniesToRemove);

            if (coloniesToAdd.size() > MAX_CHUNK_CLAIMS)
            {
                colonyId.clear();
                coloniesToAdd.clear();
                coloniesToRemove.clear();
            }

        }
        else
        {
            this.claimingBuilding.removeIf(newStorage.unClaimingBuilding::contains);
            this.unClaimingBuilding.removeIf(newStorage.claimingBuilding::contains);

            for (final Tuple<Short, BlockPos> tuple : newStorage.unClaimingBuilding)
            {
                if (!this.unClaimingBuilding.contains(tuple))
                {
                    this.unClaimingBuilding.add(tuple);
                }
            }

            for (final Tuple<Short, BlockPos> tuple : newStorage.claimingBuilding)
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
     *
     * @param tuple the tuple to write.
     * @return the resulting compound.
     */
    private static NBTTagCompound writeTupleToNBT(final Tuple<Short, BlockPos> tuple)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setShort(TAG_COLONY_ID, tuple.getFirst());
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, tuple.getSecond());
        return compound;
    }

    /**
     * Read the tuple from NBT.
     *
     * @param compound the compound to extract it from.
     * @return the tuple.
     */
    private static Tuple<Short, BlockPos> readTupleFromNbt(final NBTTagCompound compound)
    {
        return new Tuple<>(compound.getShort(TAG_COLONY_ID), BlockPosUtil.readFromNBT(compound, TAG_BUILDING));
    }
}
