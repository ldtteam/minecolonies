package com.minecolonies.api.util;

import com.minecolonies.api.colony.capability.IColonyTagCapability;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
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

    private static final Codec<List<Tuple<Short, BlockPos>>> BUILDING_CODEC = Codec.pair(Codec.SHORT, BlockPos.CODEC).xmap(Tuple::new, Tuple::toCodecPair).listOf();
    public static final Codec<ChunkLoadStorage> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(Codec.LONG.fieldOf(TAG_POS).forGetter(s -> s.xz),
            ResourceLocation.CODEC.fieldOf(TAG_DIMENSION).forGetter(s -> s.dimension),
            ExtraCodecs.strictOptionalField(Codec.SHORT.listOf(), TAG_CLAIM_LIST, List.of()).forGetter(s -> s.owningChanges),
            ExtraCodecs.strictOptionalField(Codec.SHORT.listOf(), TAG_COLONIES_TO_REMOVE, List.of()).forGetter(s -> s.coloniesToRemove),
            ExtraCodecs.strictOptionalField(Codec.SHORT.listOf(), TAG_COLONIES_TO_ADD, List.of()).forGetter(s -> s.coloniesToAdd),
            ExtraCodecs.strictOptionalField(BUILDING_CODEC, TAG_BUILDINGS_CLAIM, List.of()).forGetter(s -> s.claimingBuilding),
            ExtraCodecs.strictOptionalField(BUILDING_CODEC, TAG_BUILDINGS_UNCLAIM, List.of()).forGetter(s -> s.unClaimingBuilding))
        .apply(builder, ChunkLoadStorage::new));

    /**
     * The colony id.
     */
    private final List<Short> owningChanges;

    /**
     * The list of colonies to be added to this loc.
     */
    private final List<Short> coloniesToRemove;

    /**
     * The list of colonies to be removed from this loc.
     */
    private final List<Short> coloniesToAdd;

    /**
     * XZ pos as long.
     */
    private final long xz;

    /**
     * The dimension of the chunk.
     */
    private final ResourceLocation dimension;

    /**
     * The building claiming this.
     */
    private final List<Tuple<Short, BlockPos>> claimingBuilding;

    /**
     * The building unclaiming this.
     */
    private final List<Tuple<Short, BlockPos>> unClaimingBuilding;

    private ChunkLoadStorage(final long xz,
        final ResourceLocation dimension,
        final List<Short> owningChanges,
        final List<Short> coloniesToRemove,
        final List<Short> coloniesToAdd,
        final List<Tuple<Short, BlockPos>> claimingBuilding,
        final List<Tuple<Short, BlockPos>> unClaimingBuilding)
    {
        this.xz = xz;
        this.dimension = dimension;
        this.owningChanges = owningChanges;
        this.coloniesToRemove = coloniesToRemove;
        this.coloniesToAdd = coloniesToAdd;
        this.claimingBuilding = claimingBuilding;
        this.unClaimingBuilding = unClaimingBuilding;
    }

    private ChunkLoadStorage(final long xz, final ResourceLocation dimension)
    {
        this.xz = xz;
        this.dimension = dimension;
        this.owningChanges = new ArrayList<>();
        this.coloniesToRemove = new ArrayList<>();
        this.coloniesToAdd = new ArrayList<>();
        this.claimingBuilding = new ArrayList<>();
        this.unClaimingBuilding = new ArrayList<>();
    }

    /**
     * Create a new chunkload storage.
     *
     * @param colonyId       the id of the colony.
     * @param xz             the chunk xz.
     * @param add            the operation type.
     * @param dimension      the dimension.
     * @param forceOwnership if the colony should own the chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final boolean add, final ResourceLocation dimension, final boolean forceOwnership)
    {
        this(xz, dimension);
        this.owningChanges.add((short) (forceOwnership && add ? colonyId : NO_COLONY_ID));

        if (add)
        {
            coloniesToAdd.add((short) colonyId);
        }
        else
        {
            coloniesToRemove.add((short) colonyId);
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
    public ChunkLoadStorage(final int colonyId, final long xz, final ResourceLocation dimension, final BlockPos building, final boolean add)
    {
        this(xz, dimension);
        if (add)
        {
            claimingBuilding.add(new Tuple<>((short) colonyId, building));
        }
        else
        {
            unClaimingBuilding.add(new Tuple<>((short) colonyId, building));
        }
    }

    /**
     * Getter for the dimension.
     *
     * @return the dimension id.
     */
    public ResourceLocation getDimension()
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
        return xz == storage.xz &&
                 dimension == storage.dimension &&
                 Objects.equals(owningChanges, storage.owningChanges) &&
                 Objects.equals(coloniesToRemove, storage.coloniesToRemove) &&
                 Objects.equals(coloniesToAdd, storage.coloniesToAdd) &&
                 Objects.equals(claimingBuilding, storage.claimingBuilding) &&
                 Objects.equals(unClaimingBuilding, storage.unClaimingBuilding);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(owningChanges, coloniesToRemove, coloniesToAdd, xz, dimension, claimingBuilding, unClaimingBuilding);
    }

    /**
     * Apply this ChunkLoadStorage to a capability.
     *
     * @param chunk the chunk to apply it to.
     * @param cap   the capability to apply it to.
     */
    public void applyToCap(final IColonyTagCapability cap, final LevelChunk chunk)
    {
        if (this.claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty())
        {
            final int amountOfOperations = Math.max(Math.max(owningChanges.size(), coloniesToAdd.size()), coloniesToRemove.size());

            for (int i = 0; i < amountOfOperations; i++)
            {
                if (i < owningChanges.size())
                {
                    final int claimID = owningChanges.get(i);
                    if (claimID > NO_COLONY_ID)
                    {
                        cap.setOwningColony(claimID, chunk);
                    }
                }

                if (i < coloniesToAdd.size() && coloniesToAdd.get(i) > NO_COLONY_ID)
                {
                    cap.addColony(coloniesToAdd.get(i), chunk);
                }

                if (i < coloniesToRemove.size() && coloniesToRemove.get(i) > NO_COLONY_ID)
                {
                    cap.removeColony(coloniesToRemove.get(i), chunk);
                }
            }
        }
        else
        {
            for (final Tuple<Short, BlockPos> tuple : unClaimingBuilding)
            {
                cap.removeBuildingClaim(tuple.getA(), tuple.getB(), chunk);
            }

            for (final Tuple<Short, BlockPos> tuple : claimingBuilding)
            {
                cap.addBuildingClaim(tuple.getA(), tuple.getB(), chunk);
            }
        }
        chunk.setUnsaved(true);
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
            owningChanges.addAll(newStorage.owningChanges);
            coloniesToAdd.addAll(newStorage.coloniesToAdd);
            coloniesToRemove.addAll(newStorage.coloniesToRemove);

            if (coloniesToAdd.size() > MAX_CHUNK_CLAIMS)
            {
                owningChanges.clear();
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
}
