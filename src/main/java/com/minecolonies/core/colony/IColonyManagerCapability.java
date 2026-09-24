package com.minecolonies.core.colony;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.claims.ClaimInfo;
import com.minecolonies.api.colony.claims.ClaimReason;
import com.minecolonies.api.colony.claims.UnclaimReason;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.util.BackUpHelper;
import com.minecolonies.core.util.ChunkDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Capability for the colony tag for chunks
 */
public interface IColonyManagerCapability
{
    /**
     * Create a colony and return it.
     *
     * @param w   the world the colony is in.
     * @param pos the position of the colony.
     * @return the created colony.
     */
    IColony createColony(@NotNull final Level w, @NotNull final BlockPos pos);

    /**
     * Delete a colony with a certain id.
     *
     * @param id the id of the colony.
     */
    void deleteColony(final int id);

    /**
     * Get a colony with a certain id.
     *
     * @param id the id of the colony.
     * @return the colony or null.
     */
    @Nullable
    IColony getColony(final int id);

    /**
     * Get a list of all colonies.
     *
     * @return a complete list.
     */
    List<IColony> getColonies();

    /**
     * add a new colony to the capability.
     *
     * @param colony the colony to add.
     */
    void addColony(IColony colony);

    /**
     * Rebuilds a colony's claims from scratch: grants it a center claim (if it doesn't already have one) and re-claims a
     * chunk for every one of its current buildings. For a colony whose claim data is missing or wrong for any reason
     * &mdash; a pre-refactor save that predates claim data existing at all, or a colony restored from a backup &mdash; this
     * puts it back in the same state a freshly created colony would be in.
     *
     * @param colony the colony to rebuild claims for. Its world ({@link IColony#getWorld()}) must already be set.
     */
    void reclaimChunks(@NotNull final IColony colony);

    /**
     * Get the top most id of all colonies.
     *
     * @return the top most id.
     */
    int getTopID();

    /**
     * Get the colony that currently owns the given chunk, if any.
     *
     * @param chunkPos the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @return the owning colony, or null if unclaimed.
     */
    @Nullable
    IColony getOwningColony(final long chunkPos);

    /**
     * Get all chunks currently claimed by the given colony.
     *
     * @param colonyId the colony id.
     * @return the set of claimed chunk positions, as {@code ChunkPos.asLong(x, z)}. Empty if the colony has no claims.
     */
    @NotNull
    Set<Long> getClaimedChunks(final int colonyId);

    /**
     * Attempts to claim a chunk, for the given reason. Asks every other colony in this capability whether they already
     * claim the chunk (unless the reason says not to); if none object, the claim is granted.
     *
     * @param chunkPos    the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @param requesterId the id of the colony requesting the claim.
     * @param reason      why the chunk is being claimed, which decides what gets recorded and whether other colonies get a
     *                    say first.
     * @return true if the claim was granted.
     */
    boolean tryClaimChunk(final long chunkPos, final int requesterId, @NotNull final ClaimReason reason);

    /**
     * Releases a colony's claim on a chunk, for the given reason. No need to check with other colonies first. If the colony
     * ends up with no reason left to claim the chunk, the record is discarded entirely.
     *
     * @param chunkPos the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @param ownerId  the id of the colony releasing the claim.
     * @param reason   why the claim is being released, which decides what gets cleared.
     */
    void unclaimChunk(final long chunkPos, final int ownerId, @NotNull final UnclaimReason reason);

    /**
     * Get the raw claim record for a chunk, for a specific colony. Read-only debug/reporting access, does not affect ownership.
     *
     * @param chunkPos the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @param colonyId the id of the colony to check.
     * @return the claim record, or null if that colony has no claim on the chunk.
     */
    @Nullable
    ClaimInfo getClaimInfo(final long chunkPos, final int colonyId);

    /**
     * The implementation of the colonyTagCapability.
     */
    class Impl implements IColonyManagerCapability
    {
        /**
         * The list of all colonies.
         */
        @NotNull
        private final ColonyList<IColony> colonies = new ColonyList<>();

        /**
         * Chunks claimed by colonies in this dimension. Keyed by colony id, then by chunk position (as {@code ChunkPos.asLong(x, z)}).
         * A colony claims a chunk just by having an entry here for it. The fields on {@link ClaimInfo} don't decide whether a
         * chunk is claimed, they only record why, so we know if the claim can be dropped once one of those reasons goes away.
         * This field is package-visible, so {@link Storage} can read and write it directly.
         */
        @NotNull
        final Map<Integer, Map<Long, ClaimInfo>> claims = new HashMap<>();

        /**
         * Cache of a chunk -> owning colony, so we don't have to scan every colony on every lookup. If a chunk isn't in the cache
         * yet, we look it up the slow way and store the result. Every method below that changes a claim clears the cache entry
         * for that chunk afterward, so the cache can never go stale in a way that matters.
         * <p>
         * The value is wrapped in {@code Optional} only because Guava's cache doesn't allow storing null (an unclaimed chunk
         * would otherwise have nothing to store). This is purely an internal detail of the cache and never shows up outside
         * this class.
         */
        private final LoadingCache<Long, Optional<IColony>> owningColonyCache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(this::findOwningColony));

        @Override
        public IColony createColony(@NotNull final Level w, @NotNull final BlockPos pos)
        {
            final IColony colony = colonies.create(w, pos);
            claimCenter(colony);
            return colony;
        }

        /**
         * Grants a colony its center claim: the fixed area around its center, claimed immediately, not tied to any
         * building. Only ever called from inside this class ({@link #createColony} and {@link #reclaimChunks}), since a
         * center claim must never be re-grantable once a colony already has one.
         * <p>
         * Still checks for an existing owner on each chunk, the same as any other non-forced claim, in case the colony ended up
         * placed somewhere that overlaps another colony's claims despite the distance check that's supposed to prevent that.
         *
         * @param colony the colony to grant the center claim to.
         */
        private void claimCenter(@NotNull final IColony colony)
        {
            final int range = MineColonies.getConfig().getServer().initialColonySize.get();
            for (final long chunkPos : ChunkDataHelper.getChunksInRange(colony.getCenter(), range))
            {
                final IColony currentOwner = getOwningColony(chunkPos);
                if (currentOwner != null && currentOwner.getID() != colony.getID())
                {
                    continue;
                }

                claims.computeIfAbsent(colony.getID(), k -> new HashMap<>()).computeIfAbsent(chunkPos, k -> new ClaimInfo()).setCenter(true);
                owningColonyCache.invalidate(chunkPos);
            }
        }

        @Override
        public void reclaimChunks(@NotNull final IColony colony)
        {
            claimCenter(colony);
            for (final IBuilding building : colony.getServerBuildingManager().getBuildings().values())
            {
                ChunkDataHelper.claimBuildingChunks(colony,
                    true,
                    building.getPosition(),
                    building.getClaimRadius(building.getBuildingLevel()),
                    building.getCorners());
            }
        }

        @Override
        public void deleteColony(final int id)
        {
            colonies.remove(id);
            final Map<Long, ClaimInfo> removedClaims = claims.remove(id);
            if (removedClaims != null)
            {
                owningColonyCache.invalidateAll(removedClaims.keySet());
            }
        }

        @Override
        public IColony getColony(final int id)
        {
            return colonies.get(id);
        }

        @Override
        public List<IColony> getColonies()
        {
            return colonies.getCopyAsList();
        }

        @Override
        public void addColony(final IColony colony)
        {
            colonies.add(colony);
        }

        @Override
        public int getTopID()
        {
            return colonies.getTopID();
        }

        /**
         * It looks up the owning colony by checking every colony's claims one by one. Only called by the cache when it doesn't
         * already have an answer for this chunk.
         */
        @NotNull
        private Optional<IColony> findOwningColony(final long chunkPos)
        {
            for (final Map.Entry<Integer, Map<Long, ClaimInfo>> entry : claims.entrySet())
            {
                if (entry.getValue().containsKey(chunkPos))
                {
                    return Optional.ofNullable(getColony(entry.getKey()));
                }
            }
            return Optional.empty();
        }

        @Nullable
        @Override
        public IColony getOwningColony(final long chunkPos)
        {
            return owningColonyCache.getUnchecked(chunkPos).orElse(null);
        }

        @NotNull
        @Override
        public Set<Long> getClaimedChunks(final int colonyId)
        {
            final Map<Long, ClaimInfo> colonyClaims = claims.get(colonyId);
            return colonyClaims == null ? Collections.emptySet() : new HashSet<>(colonyClaims.keySet());
        }

        @Override
        public boolean tryClaimChunk(final long chunkPos, final int requesterId, @NotNull final ClaimReason reason)
        {
            if (reason.checksExistingOwner())
            {
                final IColony currentOwner = getOwningColony(chunkPos);
                if (currentOwner != null && currentOwner.getID() != requesterId)
                {
                    return false;
                }
            }
            else
            {
                // Skips the normal claim check, so we have to check every colony instead of just the cached owner, to be
                // sure no other colony is left thinking it still owns this chunk.
                for (final Map.Entry<Integer, Map<Long, ClaimInfo>> entry : claims.entrySet())
                {
                    if (entry.getKey() != requesterId && entry.getValue().remove(chunkPos) != null)
                    {
                        // This colony just lost the chunk, but it wasn't the one calling this method, so we have to tell it
                        // directly to make sure its next sync to players still reflects the loss.
                        markDirtyIfPresent(entry.getKey());
                    }
                }
            }

            reason.applyTo(claims.computeIfAbsent(requesterId, k -> new HashMap<>()).computeIfAbsent(chunkPos, k -> new ClaimInfo()));
            owningColonyCache.invalidate(chunkPos);
            markDirtyIfPresent(requesterId);
            return true;
        }

        @Override
        public void unclaimChunk(final long chunkPos, final int ownerId, @NotNull final UnclaimReason reason)
        {
            final Map<Long, ClaimInfo> ownerClaims = claims.get(ownerId);
            if (ownerClaims == null)
            {
                return;
            }

            final ClaimInfo info = ownerClaims.get(chunkPos);
            if (info == null)
            {
                return;
            }

            reason.removeFrom(info);
            if (info.isEmpty())
            {
                ownerClaims.remove(chunkPos);
                owningColonyCache.invalidate(chunkPos);
            }
            markDirtyIfPresent(ownerId);
        }

        @Nullable
        @Override
        public ClaimInfo getClaimInfo(final long chunkPos, final int colonyId)
        {
            final Map<Long, ClaimInfo> ownerClaims = claims.get(colonyId);
            return ownerClaims == null ? null : ownerClaims.get(chunkPos);
        }

        /**
         * Tells a colony its claims changed, so the next sync sends players an updated list. Does nothing if the colony
         * doesn't exist (for example, it was already deleted).
         *
         * @param colonyId the id of the colony to mark dirty.
         */
        private void markDirtyIfPresent(final int colonyId)
        {
            final IColony colony = getColony(colonyId);
            if (colony != null)
            {
                colony.markClaimsDirty();
            }
        }
    }

    /**
     * The storage class of the capability.
     */
    class Storage
    {

        public static Tag writeNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance, final boolean overworld)
        {
            final CompoundTag compound = new CompoundTag();

            final ListTag colonies = new ListTag();
            for (final IColony colony : instance.getColonies())
            {
                try
                {
                    colonies.add(colony.getColonyTag());
                }
                catch (Exception e)
                {
                    Log.getLogger()
                        .error("Colony: " + colony.getName() + " id:" + colony.getID() + " owner:" + colony.getPermissions().getOwnerName() + " could not be saved! Error:", e);
                }
            }

            compound.put(TAG_COLONIES, colonies);

            final ListTag claimColonies = new ListTag();
            for (final Map.Entry<Integer, Map<Long, ClaimInfo>> colonyEntry : ((Impl) instance).claims.entrySet())
            {
                final CompoundTag colonyClaimsCompound = new CompoundTag();
                colonyClaimsCompound.putInt(TAG_ID, colonyEntry.getKey());

                final ListTag chunkClaims = new ListTag();
                for (final Map.Entry<Long, ClaimInfo> chunkEntry : colonyEntry.getValue().entrySet())
                {
                    final CompoundTag claimCompound = chunkEntry.getValue().serializeNBT();
                    claimCompound.putLong(TAG_CLAIM_CHUNK, chunkEntry.getKey());
                    chunkClaims.add(claimCompound);
                }
                colonyClaimsCompound.put(TAG_CLAIMS, chunkClaims);
                claimColonies.add(colonyClaimsCompound);
            }
            compound.put(TAG_CLAIM_COLONIES, claimColonies);

            if (overworld)
            {
                final CompoundTag managerCompound = new CompoundTag();
                IColonyManager.getInstance().write(managerCompound);
                compound.put(TAG_COLONY_MANAGER, managerCompound);
            }
            return compound;
        }

        public static void readNBT(
            @NotNull final Capability<IColonyManagerCapability> capability,
            @NotNull final IColonyManagerCapability instance,
            final boolean overworld,
            @NotNull final Tag nbt)
        {
            // Notify that we did load the cap for this world
            IColonyManager.getInstance().setCapLoaded();
            if (nbt instanceof CompoundTag)
            {
                final CompoundTag compound = (CompoundTag) nbt;

                if (!compound.contains(TAG_COLONIES))
                {
                    BackUpHelper.loadManagerBackup();
                    return;
                }

                if (overworld && !compound.contains(TAG_COLONY_MANAGER))
                {
                    BackUpHelper.loadManagerBackup();
                }

                // Load all colonies from Nbt
                Multimap<BlockPos, IColony> tempColonies = ArrayListMultimap.create();
                for (final Tag tag : compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND))
                {
                    final IColony colony = Colony.loadColony((CompoundTag) tag, null);
                    if (colony != null)
                    {
                        tempColonies.put(colony.getCenter(), colony);
                        instance.addColony(colony);
                    }
                }

                readClaims(instance, compound);

                // Check colonies for duplicates causing issues.
                for (final BlockPos pos : tempColonies.keySet())
                {
                    // Check if any position has more than one colony
                    if (tempColonies.get(pos).size() > 1)
                    {
                        Log.getLogger().warn("Detected duplicate colonies which are at the same position:");
                        for (final IColony colony : tempColonies.get(pos))
                        {
                            Log.getLogger()
                                .warn("ID: " + colony.getID() + " name:" + colony.getName() + " citizens:" + colony.getCitizenManager().getCitizens().size() + " building count:"
                                    + colony.getServerBuildingManager().getBuildings().size());
                        }
                        Log.getLogger().warn("Check and remove all except one of the duplicated colonies above!");
                    }
                }

                if (compound.contains(TAG_COLONY_MANAGER) && overworld)
                {
                    IColonyManager.getInstance().read(compound.getCompound(TAG_COLONY_MANAGER));
                }
            }
            else
            {
                BackUpHelper.loadManagerBackup();
            }
        }

        /**
         * Reads saved claim data into {@code instance}. Colonies with no claim data of their own (a pre-refactor save) are
         * left with none here &mdash; the level isn't fully constructed yet at this point and colonies don't have a world
         * assigned this early, so their claims can't be rebuilt yet. {@link ColonyManager#onWorldLoad} rebuilds claims for
         * any colony it finds with none, which covers this case once a world is actually available.
         *
         * @param instance the capability being loaded.
         * @param compound the top-level compound being read.
         */
        private static void readClaims(@NotNull final IColonyManagerCapability instance, @NotNull final CompoundTag compound)
        {
            final Map<Integer, Map<Long, ClaimInfo>> claims = ((Impl) instance).claims;

            // Tracks every chunk already granted to a colony while loading, so a chunk saved as claimed by more than one
            // colony (only possible from corrupted/pre-refactor data) is granted to just the first colony that claims it,
            // instead of silently letting multiple colonies disagree about who owns it.
            final Set<Long> claimedChunks = new HashSet<>();

            if (compound.contains(TAG_CLAIM_COLONIES))
            {
                for (final Tag tag : compound.getList(TAG_CLAIM_COLONIES, Tag.TAG_COMPOUND))
                {
                    final CompoundTag colonyClaimsCompound = (CompoundTag) tag;
                    final int colonyId = colonyClaimsCompound.getInt(TAG_ID);

                    final Map<Long, ClaimInfo> colonyClaims = claims.computeIfAbsent(colonyId, k -> new HashMap<>());
                    for (final Tag claimTag : colonyClaimsCompound.getList(TAG_CLAIMS, Tag.TAG_COMPOUND))
                    {
                        final CompoundTag claimCompound = (CompoundTag) claimTag;
                        final long chunkPos = claimCompound.getLong(TAG_CLAIM_CHUNK);

                        if (!claimedChunks.add(chunkPos))
                        {
                            Log.getLogger()
                                .warn("Chunk at pos " + new ChunkPos(chunkPos) + " is claimed by more than one colony in the save data, "
                                    + "ignoring the duplicate claim for colony " + colonyId + ". This may cause colony borders to look different than before.");
                            continue;
                        }

                        colonyClaims.put(chunkPos, ClaimInfo.deserializeNBT(claimCompound));
                    }
                }
            }
        }
    }
}
