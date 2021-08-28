package com.minecolonies.api.colony;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
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
     * @param chunk the chunk to remove it from.
     * @param id    the id to remove.
     */
    void removeColony(final int id, final LevelChunk chunk);

    /**
     * Add a new colony to the chunk. Only relevant in non dynamic claiming.
     *
     * @param chunk the chunk to add it to.
     * @param id    the id to add.
     */
    void addColony(final int id, final LevelChunk chunk);

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
     * @param chunk the chunk to set it for.
     * @param id    the id to set.
     */
    void setOwningColony(final int id, final LevelChunk chunk);

    /**
     * Get the owning colony.
     *
     * @return the id of it.
     */
    int getOwningColony();

    /**
     * Reset the capability.
     *
     * @param chunk the chunk to reset.
     */
    void reset(final LevelChunk chunk);

    /**
     * Add the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     * @param chunk    the chunk to add the claim for.
     */
    void addBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk);

    /**
     * Remove the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     * @param chunk    the chunk to remove it from.
     */
    void removeBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk);

    /**
     * Sets all close colonies.
     *
     * @param colonies the set of colonies.
     */
    void setCloseColonies(final List<Integer> colonies);

    /**
     * Get the claiming buildings map.
     *
     * @return the entire map.
     */
    @NotNull
    Map<Integer, Set<BlockPos>> getAllClaimingBuildings();

    void readFromNBT(CompoundTag compound);

    /**
     * The implementation of the colonyTagCapability.
     */
    class Impl implements IColonyTagCapability
    {
        /**
         * The set of all close colonies. Only relevant in non dynamic claiming.
         */
        private Set<Integer> colonies = new HashSet<>();

        /**
         * The colony owning the chunk. 0 If none.
         */
        private int owningColony = 0;

        /**
         * List of buildings claiming this chunk for a certain colony.
         */
        private final Map<Integer, Set<BlockPos>> claimingBuildings = new HashMap<>();

        @Override
        public void addColony(final int id, final LevelChunk chunk)
        {
            colonies.add(id);
            chunk.markUnsaved();
        }

        @Override
        public void removeColony(final int id, final LevelChunk chunk)
        {
            colonies.remove(id);
            if (owningColony == id)
            {
                this.owningColony = 0;
            }
            chunk.markUnsaved();
        }

        @Override
        public void setCloseColonies(final List<Integer> colonies)
        {
            this.colonies = new HashSet<>(colonies);
        }

        @Override
        public void reset(final LevelChunk chunk)
        {
            colonies.clear();
            owningColony = 0;
            claimingBuildings.clear();
            chunk.markUnsaved();
        }

        @Override
        public void addBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk)
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
            chunk.markUnsaved();
        }

        @Override
        public void removeBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk)
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
            chunk.markUnsaved();
        }

        @Override
        public void setOwningColony(final int id, final LevelChunk chunk)
        {
            this.owningColony = id;
            chunk.markUnsaved();
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
        public void readFromNBT(final CompoundTag compound)
        {
            // Set owning
            owningColony = compound.getInt(TAG_ID);

            // Fill colonies list
            NBTUtils.streamCompound(compound.getList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
              .map(c -> c.getInt(TAG_ID)).forEach(colonies::add);

            // Fill claim buildings list
            NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_CLAIM, Constants.NBT.TAG_COMPOUND)).forEach(this::readClaims);
        }

        /**
         * Read the position list and add it to the map.
         *
         * @param compound the compound to read it from.
         */
        private void readClaims(final CompoundTag compound)
        {
            final int id = compound.getInt(TAG_ID);
            NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND)).forEach(
              tag -> {
                  final BlockPos pos = BlockPosUtil.read((tag), TAG_BUILDING);
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
        public Tag writeNBT(@NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance, @Nullable final Direction side)
        {
            final CompoundTag compound = new CompoundTag();
            compound.putInt(TAG_ID, instance.getOwningColony());
            compound.put(TAG_COLONIES, instance.getAllCloseColonies().stream().map(Storage::write).collect(NBTUtils.toListNBT()));
            compound.put(TAG_BUILDINGS_CLAIM, instance.getAllClaimingBuildings().entrySet().stream().map(Storage::writeClaims).collect(NBTUtils.toListNBT()));


            return compound;
        }

        @Override
        public void readNBT(
          @NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance,
          @Nullable final Direction side, @NotNull final Tag nbt)
        {
            if (nbt instanceof CompoundTag && ((CompoundTag) nbt).contains(TAG_ID))
            {
                instance.readFromNBT((CompoundTag) nbt);
            }
        }

        /**
         * Write one colony id to nbt.
         *
         * @param id the id.
         * @return the compound of it.
         */
        private static CompoundTag write(final int id)
        {
            final CompoundTag compound = new CompoundTag();
            compound.putInt(TAG_ID, id);
            return compound;
        }

        /**
         * Write the claims map entry to NBT.
         *
         * @param entry the entry.
         * @return the resulting compound.
         */
        private static CompoundTag writeClaims(@NotNull final Map.Entry<Integer, Set<BlockPos>> entry)
        {
            final CompoundTag compound = new CompoundTag();
            compound.putInt(TAG_ID, entry.getKey());
            compound.put(TAG_BUILDINGS, entry.getValue().stream().map(pos -> BlockPosUtil.write(new CompoundTag(), TAG_BUILDING, pos)).collect(NBTUtils.toListNBT()));
            return compound;
        }
    }
}
