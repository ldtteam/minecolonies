package com.minecolonies.api.colony;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Capability for the colony tag for chunks
 */
public interface IColonyTagCapability
{
    /**
     * Remove a colony from the list. Only relevant in non dynamic claiming.
     *
     * @param id the id to remove.
     */
    void removeColony(final int id, final Chunk chunk);

    /**
     * Add a new colony to the chunk. Only relevant in non dynamic claiming.
     *
     * @param id the id to add.
     */
    void addColony(final int id, final Chunk chunk);

    /**
     * Get a list of all close colonies.
     *
     * @return a list of their ids.
     */
    @NotNull
    List<Integer> getAllCloseColonies();

    /**
     * Set the owning colony.
     *
     * @param id the id to set.
     */
    void setOwningColony(final int id, final Chunk chunk);

    /**
     * Get the owning colony.
     *
     * @return the id of it.
     */
    int getOwningColony();

    /**
     * Reset the capability.
     */
    void reset(final Chunk chunk);

    /**
     * Add the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     */
    void addBuildingClaim(final int colonyId, final BlockPos pos, final Chunk chunk);

    /**
     * Remove the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     */
    void removeBuildingClaim(final int colonyId, final BlockPos pos, final Chunk chunk);

    /**
     * Get the claiming buildings map.
     *
     * @return the entire map.
     */
    @NotNull
    Map<Integer, Set<BlockPos>> getAllClaimingBuildings();

    void readFromNBT(NBTTagCompound compound);

    /**
     * The implementation of the colonyTagCapability.
     */
    class Impl implements IColonyTagCapability
    {
        /**
         * The set of all close colonies. Only relevant in non dynamic claiming.
         */
        private final Set<Integer> colonies = new HashSet<>();

        /**
         * The colony owning the chunk. 0 If none.
         */
        private int owningColony = 0;

        /**
         * List of buildings claiming this chunk for a certain colony.
         */
        private final Map<Integer, Set<BlockPos>> claimingBuildings = new HashMap<>();

        @Override
        public void addColony(final int id, final Chunk chunk)
        {
            colonies.add(id);
            chunk.markDirty();
        }

        @Override
        public void removeColony(final int id, final Chunk chunk)
        {
            colonies.remove(id);
            if (owningColony == id)
            {
                this.owningColony = 0;
            }
            chunk.markDirty();
        }

        @Override
        public void reset(final Chunk chunk)
        {
            colonies.clear();
            owningColony = 0;
            claimingBuildings.clear();
            chunk.markDirty();
        }

        @Override
        public void addBuildingClaim(final int colonyId, final BlockPos pos, final Chunk chunk)
        {
            if (owningColony == 0)
            {
                setOwningColony(colonyId, chunk);
            }

            if (claimingBuildings.containsKey(colonyId))
            {
                claimingBuildings.get(colonyId).add(pos);
            }
            else
            {
                final Set<BlockPos> newList = new HashSet<>();
                newList.add(pos);
                claimingBuildings.put(colonyId, newList);
            }
            chunk.markDirty();
        }

        @Override
        public void removeBuildingClaim(final int colonyId, final BlockPos pos, final Chunk chunk)
        {
            if (claimingBuildings.containsKey(colonyId))
            {
                final Set<BlockPos> buildings = claimingBuildings.get(colonyId);
                buildings.remove(pos);

                if (buildings.isEmpty())
                {
                    claimingBuildings.remove(colonyId);
                }

                if (owningColony == colonyId)
                {
                    if (claimingBuildings.isEmpty())
                    {
                        reset(chunk);
                    }
                    else if (claimingBuildings.size() == 1)
                    {
                        setOwningColony(claimingBuildings.keySet().iterator().next(), chunk);
                    }
                    else
                    {
                        setOwningColony(claimingBuildings.keySet().toArray(new Integer[0])[new Random().nextInt(claimingBuildings.size())], chunk);
                    }
                }
            }
            chunk.markDirty();
        }

        @Override
        public void setOwningColony(final int id, final Chunk chunk)
        {
            this.owningColony = id;
            chunk.markDirty();
        }

        @Override
        public int getOwningColony()
        {
            return owningColony;
        }

        @NotNull
        @Override
        public List<Integer> getAllCloseColonies()
        {
            return new ArrayList<>(colonies);
        }

        @NotNull
        @Override
        public Map<Integer, Set<BlockPos>> getAllClaimingBuildings()
        {
            return claimingBuildings;
        }

        @Override
        public void readFromNBT(final NBTTagCompound compound)
        {
            // Set owning
            owningColony = compound.getInteger(TAG_ID);

            // Fill colonies list
            NBTUtils.streamCompound(compound.getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
              .map(c -> c.getInteger(TAG_ID)).forEach(colonies::add);

            // Fill claim buildings list
            NBTUtils.streamCompound(compound.getTagList(TAG_BUILDINGS_CLAIM, Constants.NBT.TAG_COMPOUND)).forEach(this::readClaims);
        }

        /**
         * Read the position list and add it to the map.
         *
         * @param compound the compound to read it from.
         */
        private void readClaims(final NBTTagCompound compound)
        {
            final int id = compound.getInteger(TAG_ID);
            NBTUtils.streamCompound(compound.getTagList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND)).forEach(
              tag -> {
                  final BlockPos pos = BlockPosUtil.readFromNBT((tag), TAG_BUILDING);
                  if (claimingBuildings.containsKey(id))
                  {
                      claimingBuildings.get(id).add(pos);
                  }
                  else
                  {
                      final Set<BlockPos> newList = new HashSet<>();
                      newList.add(pos);
                      claimingBuildings.put(id, newList);
                  }
              });
        }
    }

    /**
     * The storage class of the capability.
     */
    class Storage implements Capability.IStorage<IColonyTagCapability>
    {
        @Override
        public NBTBase writeNBT(@NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance, @Nullable final EnumFacing side)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, instance.getOwningColony());
            compound.setTag(TAG_COLONIES, instance.getAllCloseColonies().stream().map(Storage::write).collect(NBTUtils.toNBTTagList()));
            compound.setTag(TAG_BUILDINGS_CLAIM, instance.getAllClaimingBuildings().entrySet().stream().map(Storage::writeClaims).collect(NBTUtils.toNBTTagList()));


            return compound;
        }

        @Override
        public void readNBT(
          @NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance,
          @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if (nbt instanceof NBTTagCompound && ((NBTTagCompound) nbt).hasKey(TAG_ID))
            {
                instance.readFromNBT((NBTTagCompound) nbt);
            }
        }

        /**
         * Write one colony id to nbt.
         *
         * @param id the id.
         * @return the compound of it.
         */
        private static NBTTagCompound write(final int id)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, id);
            return compound;
        }

        /**
         * Write the claims map entry to NBT.
         *
         * @param entry the entry.
         * @return the resulting compound.
         */
        private static NBTTagCompound writeClaims(@NotNull final Map.Entry<Integer, Set<BlockPos>> entry)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, entry.getKey());
            compound.setTag(TAG_BUILDINGS, entry.getValue().stream().map(pos -> BlockPosUtil.writeToNBT(new NBTTagCompound(), TAG_BUILDING, pos)).collect(NBTUtils.toNBTTagList()));
            return compound;
        }
    }
}
