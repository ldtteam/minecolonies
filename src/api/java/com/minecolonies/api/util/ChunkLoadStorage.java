package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WAYPOINT;

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
     * Intitialize a ChunLoadStorage from nbt.
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final NBTTagCompound compound)
    {
        this.colonyId = compound.getInteger(TAG_ID);
        this.xz = compound.getLong(TAG_POS);
        this.dimension = compound.getInteger(TAG_DIMENSION);

        coloniesToAdd.clear();
        coloniesToAdd.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_COLONIES_TO_ADD, Constants.NBT.TAG_COMPOUND))
                .map(tempComound -> tempComound.getInteger(TAG_COLONY_ID)).collect(Collectors.toList()));
        coloniesToRemove.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_COLONIES_TO_REMOVE, Constants.NBT.TAG_COMPOUND))
                .map(tempComound -> tempComound.getInteger(TAG_COLONY_ID)).collect(Collectors.toList()));
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
     * Write the ChunkLoadStorage to NBT.
     * @return the compound.
     */
    public NBTTagCompound toNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_ID, colonyId);
        compound.setLong(TAG_POS, xz);
        compound.setInteger(TAG_DIMENSION, dimension);

        compound.setTag(TAG_COLONIES_TO_ADD, coloniesToAdd.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toNBTTagList()));
        compound.setTag(TAG_COLONIES_TO_REMOVE, coloniesToRemove.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toNBTTagList()));
        return compound;
    }

    private static NBTTagCompound getCompoundOfColonyId(final int id)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_COLONY_ID, id);
        return compound;
    }

    /**
     * Getter for the list of colonies to add.
     * @return a copy of the list.
     */
    public List<Integer> getColoniesToAdd()
    {
        return new ArrayList<>(coloniesToAdd);
    }

    /**
     * Getter for the list of colonies to remove.
     * @return a copy of the list.
     */
    public List<Integer> getColoniesToRemove()
    {
        return new ArrayList<>(coloniesToRemove);
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
                Objects.equals(coloniesToAdd, storage.coloniesToAdd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(colonyId, coloniesToRemove, coloniesToAdd, xz, dimension);
    }

    /**
     * Apply this ChunkLoadStorage to a capability.
     * @param cap the capability to apply it to.
     */
    public void applyToCap(final IColonyTagCapability cap)
    {
        if(this.getColonyId() > 0)
        {
            cap.setOwningColony(this.colonyId);
        }
        
        for(final int tempColonyId: coloniesToAdd)
        {
            cap.addColony(tempColonyId);
        }

        for(final int tempColonyId: coloniesToRemove)
        {
            cap.removeColony(tempColonyId);
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
        for(final int tempColonyId: newStorage.coloniesToAdd)
        {
            if(this.coloniesToRemove.contains(tempColonyId))
            {
                this.coloniesToRemove.remove(new Integer(tempColonyId));
            }
            else if(!this.coloniesToAdd.contains(tempColonyId))
            {
                this.coloniesToAdd.add(tempColonyId);
            }
        }

        for(final int tempColonyId: newStorage.coloniesToRemove)
        {
            if(this.colonyId == tempColonyId)
            {
                this.colonyId = 0;
            }

            if(this.coloniesToAdd.contains(tempColonyId))
            {
                this.coloniesToAdd.remove(new Integer(tempColonyId));
            }
            else if(!this.coloniesToRemove.contains(tempColonyId))
            {
                this.coloniesToRemove.add(tempColonyId);
            }
        }

        if(newStorage.getColonyId() > 0 || !newStorage.coloniesToRemove.isEmpty())
        {
            this.colonyId = newStorage.getColonyId();
        }
    }
}
