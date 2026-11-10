package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.settings.GuardPatrolModeSetting;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Stores the ordered list of buildings selected as automatic cavalry patrol targets.
 */
public class StablePatrolTargetsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingEventsModule, ITickingModule
{
    /**
     * NBT tag containing the selected patrol targets.
     */
    private static final String TAG_PATROL_TARGETS = "patrolTargets";

    /**
     * Base number of patrol targets available before Stable levels are added.
     */
    private static final int BASE_TARGET_CAPACITY = 3;

    /**
     * Ordered selected patrol targets.
     */
    private final List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * Last building returned by {@link #getNextPatrolTarget()}
     */
    private BlockPos lastPatrolTarget;

    @Nullable
    private BlockPos activePatrolTargetPos;

    /**
     * Record the building of the active patrol target.
     * @param target
     */
    public void setActivePatrolTarget(@Nullable BlockPos target)
    {
        final BlockPos immutableTarget = target == null ? null : target.immutable();
        
        if (Objects.equals(activePatrolTargetPos, immutableTarget))
        {
            return;
        }

        activePatrolTargetPos = immutableTarget;
        markDirty();
    }

    /**
     * @return building of active patrol target
     */
    public @Nullable BlockPos getActivePatrolTarget()
    {
        return activePatrolTargetPos;
    }

    /**
     * Loads the persisted ordered patrol target list.
     *
     * @param compound the module data.
     */
    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        patrolTargets.clear();
        final ListTag targetList = compound.getList(TAG_PATROL_TARGETS, Tag.TAG_COMPOUND);

        for (int index = 0; index < targetList.size(); index++)
        {
            final BlockPos target = BlockPosUtil.readFromListNBT(targetList, index);
            if (!patrolTargets.contains(target))
            {
                patrolTargets.add(target);
            }
        }
        
        lastPatrolTarget = null;
        activePatrolTargetPos = null;
    }

    /**
     * Saves the ordered patrol target list.
     *
     * @param compound the module data destination.
     */
    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        final ListTag targetList = new ListTag();
        for (final BlockPos target : patrolTargets)
        {
            BlockPosUtil.writeToListNBT(targetList, target);
        }
        compound.put(TAG_PATROL_TARGETS, targetList);
    }

    /**
     * Synchronizes the target capacity and ordered selection to the client.
     *
     * @param buf the synchronization buffer.
     */
    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeVarInt(getTargetCapacity());
        buf.writeVarInt(patrolTargets.size());

        for (final BlockPos target : patrolTargets)
        {
            buf.writeBlockPos(target);
        }

        buf.writeBoolean(activePatrolTargetPos != null);

        if (activePatrolTargetPos != null)
        {
            buf.writeBlockPos(activePatrolTargetPos);
        }

    }

    /**
     * Gets the maximum number of selectable patrol targets at the Stable's current level.
     *
     * @return the target capacity.
     */
    public int getTargetCapacity()
    {
        return BASE_TARGET_CAPACITY + building.getBuildingLevel();
    }

    /**
     * Adds an eligible building to the end of the patrol route.
     *
     * @param target the building position to add.
     * @return {@code true} when the target was added.
     */
    public boolean addPatrolTarget(final BlockPos target)
    {
        validateTargets();
        if (patrolTargets.size() >= getTargetCapacity() || patrolTargets.contains(target) || !isEligibleTarget(target))
        {
            return false;
        }

        patrolTargets.add(target.immutable());
        markDirty();
        return true;
    }

    /**
     * Removes a building from the patrol route while preserving at least one valid selection.
     *
     * @param target the building position to remove.
     * @return {@code true} when the target was removed.
     */
    public boolean removePatrolTarget(final BlockPos target)
    {
        validateTargets();
        if (patrolTargets.size() <= 1 || !patrolTargets.remove(target))
        {
            return false;
        }

        markDirty();
        return true;
    }

    /**
     * Gets the next selected patrol building. The first target of each runtime route is random,
     * after which targets are returned in persisted list order with wraparound.
     *
     * @return the next patrol building position, or {@code null} when none is available.
     */
    @Nullable
    public BlockPos getNextPatrolTarget()
    {
        if (validateTargets())
        {
            markDirty();
        }

        if (patrolTargets.isEmpty())
        {
            lastPatrolTarget = null;
            return null;
        }

        final int lastIndex = lastPatrolTarget == null ? -1 : patrolTargets.indexOf(lastPatrolTarget);
        if (lastIndex < 0)
        {
            lastPatrolTarget = patrolTargets.get(building.getColony().getWorld().random.nextInt(patrolTargets.size()));
        }
        else
        {
            lastPatrolTarget = patrolTargets.get((lastIndex + 1) % patrolTargets.size());
        }

        return lastPatrolTarget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onColonyTick(final @NotNull IColony colony) 
    { 
        final boolean listChanged = validateTargets();

        if (listChanged)
        {
            markDirty();
        }

        if (building instanceof BuildingStable stable
              && (!GuardTaskSetting.PATROL.equals(stable.getTask())
                    || GuardPatrolModeSetting.MANUAL.equals(stable.getSetting(AbstractBuildingGuards.PATROL_MODE).getValue())
                    || stable.getAllAssignedMilitaryUnits().isEmpty()))
        {
            setActivePatrolTarget(null);
        }
    }

    /**
     * Ensure all patrol targets are valid.
     * (Handles clean-up if buildings are deconstructed/destroyed, etc.)
     */
    private boolean validateTargets()
    {
        boolean listChanged = false;

        if (building == null)
        {
            return listChanged;
        }

        final List<BlockPos> validatedTargets = new ArrayList<>();
        final Set<BlockPos> seenTargets = new HashSet<>();

        for (final BlockPos target : patrolTargets)
        {
            if (validatedTargets.size() >= getTargetCapacity())
            {
                break;
            }

            if (seenTargets.add(target) && isEligibleTarget(target))
            {
                validatedTargets.add(target.immutable());
            }
        }

        if (validatedTargets.isEmpty() && isEligibleTarget(building.getPosition()))
        {
            validatedTargets.add(building.getPosition().immutable());
        }

        if (!validatedTargets.equals(patrolTargets))
        {
            patrolTargets.clear();
            patrolTargets.addAll(validatedTargets);
            listChanged = true;
        }

        return listChanged;
    }

    /**
     * Checks whether a colony building can be selected as a cavalry patrol target.
     *
     * @param target the building position to check.
     * @return {@code true} when the target is currently eligible.
     */
    private boolean isEligibleTarget(final BlockPos target)
    {
        final IBuilding targetBuilding = building.getColony().getServerBuildingManager().getBuilding(target);
        return targetBuilding != null && BuildingStable.cavalryPatrolFilter().test(targetBuilding);
    }
}
