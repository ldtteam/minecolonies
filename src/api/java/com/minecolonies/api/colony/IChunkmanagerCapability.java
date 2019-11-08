package com.minecolonies.api.colony;

import com.minecolonies.api.util.ChunkLoadStorage;
import com.minecolonies.api.util.NBTUtils;
import gnu.trove.map.hash.THashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 *
 * Capability for the colony tag for chunks
 */
public interface IChunkmanagerCapability
{
    /**
     * Get the chunkStorage at a certain location.
     * @param chunkX the x chunk location.
     * @param chunkZ the z chunk location.
     * @return the storage or null.
     */
    @Nullable
    ChunkLoadStorage getChunkStorage(int chunkX, int chunkZ);

    /**
     * Add a new chunkStorage.
     * @param chunkX chunkX the x chunk location.
     * @param chunkZ chunkX the z chunk location.
     * @param storage the new to add or update.
     * @return true if override else false.
     */
    boolean addChunkStorage(int chunkX, int chunkZ, ChunkLoadStorage storage);

    /**
     * Get all chunk storages for serialization.
     * @return the storages.
     */
    Map<ChunkPos, ChunkLoadStorage> getAllChunkStorages();

    /**
     * The implementation of the colonyTagCapability.
     */
    class Impl implements IChunkmanagerCapability
    {
        /**
         * Map of chunkPos to chunkLoadStorage.
         */
        private final Map<ChunkPos, ChunkLoadStorage> chunkStorages = new THashMap<>();

        @Nullable
        @Override
        public ChunkLoadStorage getChunkStorage(final int chunkX, final int chunkZ)
        {
            return chunkStorages.remove(new ChunkPos(chunkX, chunkZ));
        }

        @Override
        public boolean addChunkStorage(final int chunkX, final int chunkZ, final ChunkLoadStorage storage)
        {
            final ChunkLoadStorage existingStorage = chunkStorages.get(new ChunkPos(chunkX, chunkZ));
            if (existingStorage == null)
            {
                chunkStorages.put(new ChunkPos(chunkX, chunkZ), storage);
                return false;
            }
            else
            {
                existingStorage.merge(storage);
                return true;
            }
        }


        @Override
        public Map<ChunkPos, ChunkLoadStorage> getAllChunkStorages()
        {
            return chunkStorages;
        }
    }

    /**
     * The storage class of the capability.
     */
    class Storage implements Capability.IStorage<IChunkmanagerCapability>
    {
        @Override
        public NBTBase writeNBT(@NotNull final Capability<IChunkmanagerCapability> capability, @NotNull final IChunkmanagerCapability instance, @Nullable final EnumFacing side)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(TAG_ALL_CHUNK_STORAGES, instance.getAllChunkStorages().entrySet().stream().map(entry -> write(entry.getKey(), entry.getValue())).collect(NBTUtils.toNBTTagList()));
            return compound;
        }

        @Override
        public void readNBT(@NotNull final Capability<IChunkmanagerCapability> capability, @NotNull final IChunkmanagerCapability instance,
          @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if(nbt instanceof NBTTagCompound && ((NBTTagCompound) nbt).hasKey(TAG_ALL_CHUNK_STORAGES))
            {
                NBTUtils.streamCompound(((NBTTagCompound) nbt).getTagList(TAG_ALL_CHUNK_STORAGES, Constants.NBT.TAG_COMPOUND))
                  .map(Storage::read).forEach(key -> instance.addChunkStorage(key.getFirst().x, key.getFirst().z, key.getSecond()));
            }
        }

        /**
         * Write a single ChunkPos, ChunkLoadStorage pair to nbt.
         * @param key the key.
         * @param value the value
         * @return the resulting compound.
         */
        private static NBTTagCompound write(final ChunkPos key, final ChunkLoadStorage value)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(TAG_CHUNK_STORAGE, value.toNBT());
            compound.setInteger(TAG_X, key.x);
            compound.setInteger(TAG_Z, key.z);
            return compound;
        }

        /**
         * Read a key value pair for the chunkloadstorages.
         * @param compound the compound to read it from.
         * @return a tuple for both.
         */
        private static Tuple<ChunkPos, ChunkLoadStorage> read(final NBTTagCompound compound)
        {
            final ChunkLoadStorage storage = new ChunkLoadStorage(compound.getCompoundTag(TAG_CHUNK_STORAGE));
            final int x = compound.getInteger(TAG_X);
            final int z = compound.getInteger(TAG_Z);
            return new Tuple<>(new ChunkPos(x,z), storage);
        }
    }
}
