package com.minecolonies.api.colony.capability;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
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
     * Get a list of colonies with a static claim.
     *
     * @return a list of their ids.
     */
    @NotNull
    List<Integer> getStaticClaimColonies();

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
    void setStaticColonyClaim(final List<Integer> colonies);

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
         * The colony owning the chunk. NO_COLONY_ID If none.
         */
        private int owningColony = NO_COLONY_ID;

        /**
         * List of buildings claiming this chunk for a certain colony.
         */
        private final Map<Integer, Set<BlockPos>> claimingBuildings = new HashMap<>();

        @Override
        public void addColony(final int id, final LevelChunk chunk)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(id, chunk.getLevel().dimension());
            if (colony == null)
            {
                return;
            }

            colonies.add(id);
            if (owningColony == NO_COLONY_ID || IColonyManager.getInstance().getColonyByDimension(owningColony, chunk.getLevel().dimension()) == null)
            {
                colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
                owningColony = id;
            }
            chunk.setUnsaved(true);
        }

        @Override
        public void removeColony(final int id, final LevelChunk chunk)
        {
            colonies.remove(id);
            claimingBuildings.remove(id);
            if (owningColony == id)
            {
                if (!claimingBuildings.isEmpty())
                {
                    owningColony = claimingBuildings.keySet().iterator().next();
                }
                else if (!colonies.isEmpty())
                {
                    owningColony = colonies.iterator().next();
                }
                else
                {
                    owningColony = NO_COLONY_ID;
                }
            }

            chunk.setUnsaved(true);
        }

        @Override
        public void setStaticColonyClaim(final List<Integer> colonies)
        {
            this.colonies = new HashSet<>(colonies);
        }

        @Override
        public void reset(final LevelChunk chunk)
        {
            colonies.clear();
            owningColony = NO_COLONY_ID;
            claimingBuildings.clear();
            chunk.setUnsaved(true);
        }

        @Override
        public void addBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk)
        {
            if (chunk.getPos().equals(ChunkPos.ZERO))
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, chunk.getLevel().dimension());
                if (colony == null || BlockPosUtil.getDistance2D(colony.getCenter(), BlockPos.ZERO) > 200)
                {
                    Log.getLogger().warn("Claiming id:" + colonyId + " building at zero pos!" + pos, new Exception());
                }
            }

            if (owningColony == NO_COLONY_ID)
            {
                setOwningColony(colonyId, chunk);
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, chunk.getLevel().dimension());
                if (colony != null)
                {
                    colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
                }
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
            chunk.setUnsaved(true);
        }

        @Override
        public void removeBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk)
        {
            if (!claimingBuildings.containsKey(colonyId))
            {
                return;
            }

            chunk.setUnsaved(true);
            final Set<BlockPos> buildings = claimingBuildings.get(colonyId);
            buildings.remove(pos);

            if (buildings.isEmpty())
            {
                claimingBuildings.remove(colonyId);

                if (owningColony == colonyId && !colonies.contains(owningColony))
                {
                    if (claimingBuildings.isEmpty())
                    {
                        if (colonies.isEmpty())
                        {
                            owningColony = NO_COLONY_ID;
                        }
                        else
                        {
                            owningColony = colonies.iterator().next();
                        }
                    }
                    else
                    {
                        for (final Iterator<Map.Entry<Integer, Set<BlockPos>>> colonyIt = claimingBuildings.entrySet().iterator(); colonyIt.hasNext(); )
                        {
                            final Map.Entry<Integer, Set<BlockPos>> colonyEntry = colonyIt.next();
                            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyEntry.getKey(), chunk.getLevel().dimension());
                            if (colony == null)
                            {
                                continue;
                            }

                            for (final Iterator<BlockPos> buildingIt = colonyEntry.getValue().iterator(); buildingIt.hasNext(); )
                            {
                                final BlockPos buildingPos = buildingIt.next();
                                if (colony.getBuildingManager().getBuilding(buildingPos) != null)
                                {
                                    colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
                                    setOwningColony(colonyEntry.getKey(), chunk);
                                    return;
                                }
                                else
                                {
                                    buildingIt.remove();
                                }
                            }

                            if (colonyEntry.getValue().isEmpty())
                            {
                                colonyIt.remove();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void setOwningColony(final int id, final LevelChunk chunk)
        {
            this.owningColony = id;
            chunk.setUnsaved(true);
        }

        @Override
        public int getOwningColony()
        {
            return owningColony;
        }

        @NotNull
        @Override
        public List<Integer> getStaticClaimColonies()
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
            NBTUtils.streamCompound(compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND))
              .map(c -> c.getInt(TAG_ID)).forEach(colonies::add);

            // Fill claim buildings list
            NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_CLAIM, Tag.TAG_COMPOUND)).forEach(this::readClaims);
            if (owningColony == NO_COLONY_ID && !getStaticClaimColonies().isEmpty())
            {
                owningColony = getStaticClaimColonies().get(0);
            }
        }

        /**
         * Read the position list and add it to the map.
         *
         * @param compound the compound to read it from.
         */
        private void readClaims(final CompoundTag compound)
        {
            final int id = compound.getInt(TAG_ID);
            NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS, Tag.TAG_COMPOUND)).forEach(
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
    class Storage
    {
        public static Tag writeNBT(@NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance, @Nullable final Direction side)
        {
            final CompoundTag compound = new CompoundTag();
            compound.putInt(TAG_ID, instance.getOwningColony());
            compound.put(TAG_COLONIES, instance.getStaticClaimColonies().stream().map(Storage::write).collect(NBTUtils.toListNBT()));
            compound.put(TAG_BUILDINGS_CLAIM, instance.getAllClaimingBuildings().entrySet().stream().map(Storage::writeClaims).collect(NBTUtils.toListNBT()));


            return compound;
        }

        public static void readNBT(
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
