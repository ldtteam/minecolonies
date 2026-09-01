package com.minecolonies.api.colony.claims;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Records why a colony claims a single chunk: it was force-claimed by an admin, it's part of the colony's center claim, or
 * one or more buildings have this chunk within their claim radius.
 */
public class ClaimInfo
{
    /**
     * True if this chunk was force-claimed by an admin command, independent of any building.
     */
    private boolean forced;

    /**
     * True if this chunk is part of the colony's center claim (the area claimed immediately around the colony's center, not
     * tied to any single building). Unlike a forced claim, a center claim still respects other colonies' existing claims
     * instead of overriding them &mdash; it just isn't released just because a particular building goes away.
     */
    private boolean center;

    /**
     * Positions of buildings currently claiming this chunk.
     */
    private final Set<BlockPos> claimingBuildings = new HashSet<>();

    public boolean isForced()
    {
        return forced;
    }

    public void setForced(final boolean forced)
    {
        this.forced = forced;
    }

    public boolean isCenter()
    {
        return center;
    }

    public void setCenter(final boolean center)
    {
        this.center = center;
    }

    @NotNull
    public Set<BlockPos> getClaimingBuildings()
    {
        return claimingBuildings;
    }

    /**
     * Whether this record has no reason to be claimed anymore and can be removed from the claims map. A chunk is claimed
     * simply by being present as a key in that map &mdash; this method only exists to tell the code that changes claims when
     * to remove an entry, not to answer "is this chunk claimed". (That question is answered by map presence alone.)
     */
    public boolean isEmpty()
    {
        return !forced && !center && claimingBuildings.isEmpty();
    }

    @NotNull
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putBoolean(TAG_CLAIM_FORCED, forced);
        compound.putBoolean(TAG_CLAIM_CENTER, center);
        compound.put(TAG_CLAIM_BUILDINGS, claimingBuildings.stream().map(pos -> BlockPosUtil.write(new CompoundTag(), TAG_POS, pos)).collect(NBTUtils.toListNBT()));
        return compound;
    }

    @NotNull
    public static ClaimInfo deserializeNBT(@NotNull final CompoundTag compound)
    {
        final ClaimInfo info = new ClaimInfo();
        info.forced = compound.getBoolean(TAG_CLAIM_FORCED);
        info.center = compound.getBoolean(TAG_CLAIM_CENTER);
        NBTUtils.streamCompound(compound.getList(TAG_CLAIM_BUILDINGS, Tag.TAG_COMPOUND)).map(tag -> BlockPosUtil.read(tag, TAG_POS)).forEach(info.claimingBuildings::add);
        return info;
    }
}
