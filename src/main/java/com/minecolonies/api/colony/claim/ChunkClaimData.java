package com.minecolonies.api.colony.claim;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The implementation of the colonyTagCapability.
 */
public class ChunkClaimData implements IChunkClaimData, INBTSerializable<CompoundTag>
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
    private final Map<Integer, Set<BlockPos>> claimingBuildings;

    public ChunkClaimData()
    {
        this(new HashSet<>(), NO_COLONY_ID, new HashMap<>());
    }

    private ChunkClaimData(final Set<Integer> colonies, final int owningColony, final Map<Integer, Set<BlockPos>> claimingBuildings)
    {
        this.colonies = colonies;
        this.owningColony = owningColony;
        this.claimingBuildings = claimingBuildings;
    }

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
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_ID, owningColony);

        final ListTag colonyClaimTag = new ListTag();
        for (final int colonyId : colonies)
        {
            colonyClaimTag.add(IntTag.valueOf(colonyId));
        }
        compound.put(TAG_COLONIES, colonyClaimTag);

        for (final Map.Entry<Integer, Set<BlockPos>> entry : claimingBuildings.entrySet())
        {
            final CompoundTag perColonyEntry = new CompoundTag();
            perColonyEntry.putInt(TAG_ID, entry.getKey());

            final ListTag buildingListTag = new ListTag();
            for (final BlockPos pos : entry.getValue())
            {
                BlockPosUtil.writeToListNBT(buildingListTag, pos);
            }
            perColonyEntry.put(TAG_BUILDINGS_CLAIM, buildingListTag);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        // Set owning
        owningColony = compound.getInt(TAG_ID);

        final ListTag colonyClaim = compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < colonyClaim.size(); i++)
        {
            colonies.add(colonyClaim.getInt(i));
        }

        final ListTag buildingClaim = compound.getList(TAG_BUILDINGS_CLAIM, Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingClaim.size(); i++)
        {
            final CompoundTag perColonyCompound = buildingClaim.getCompound(i);
            final int id = perColonyCompound.getInt(TAG_ID);
            final Set<BlockPos> buildings = claimingBuildings.computeIfAbsent(id, HashSet::new);

            final ListTag buildingList = perColonyCompound.getList(TAG_BUILDINGS, Tag.TAG_COMPOUND);
            for (int j = 0; j < buildingList.size(); j++)
            {
                buildings.add(BlockPosUtil.readFromListNBT(buildingList, j));
            }
        }

        if (owningColony == NO_COLONY_ID && !getStaticClaimColonies().isEmpty())
        {
            owningColony = getStaticClaimColonies().get(0);
        }
    }
}