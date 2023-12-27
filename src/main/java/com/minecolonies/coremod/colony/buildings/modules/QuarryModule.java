package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.JobQuarrier;
import com.minecolonies.coremod.colony.requestsystem.resolvers.StationRequestResolver;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The main data module for the quarry.
 */
public class QuarryModule extends AbstractAssignedCitizenModule implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule, ICreatesResolversModule, IAltersBuildingFootprint
{
    /**
     * If the quarry was finished.
     */
    private boolean isFinished = false;

    /**
     * The height of the quarry.
     */
    private final int height;

    /**
     * Create a new quarry module.
     * @param height the height of the quarry.
     */
    public QuarryModule(final int height)
    {
        this.height = height;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && BuildingUtils.canAutoHire(building, getHiringMode(), getJobEntry()))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.getJob() instanceof JobQuarrier && !hasAssignedCitizen(data) && ((JobQuarrier) data.getJob()).findQuarry() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData : new ArrayList<>(getAssignedCitizen()))
        {
            if (!(citizenData.getJob() instanceof JobQuarrier))
            {
                removeCitizen(citizenData);
            }
        }
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.contains(getModuleSerializationIdentifier()))
        {
            compound = compound.getCompound(getModuleSerializationIdentifier());
        }

        final int[] residentIds = compound.getIntArray(TAG_MINERS);
        for (final int citizenId : residentIds)
        {
            final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
            if (citizen != null)
            {
                assignCitizen(citizen);
            }
        }
        this.isFinished = compound.getBoolean(TAG_IS_FINISHED);
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        super.serializeNBT(compound);

        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            compound.putIntArray(TAG_MINERS, residentIds);
        }
        compound.putBoolean(TAG_IS_FINISHED, isFinished);
    }

    @Override
    public void onRemoval(final ICitizenData citizen)
    {
        resetProgress(citizen);
    }

    @Override
    public void onAssignment(final ICitizenData citizen)
    {
        resetProgress(citizen);
    }

    private void resetProgress(final ICitizenData citizen)
    {
        final @Nullable IBuilding building = citizen.getWorkBuilding();
        if (building instanceof BuildingMiner)
        {
            ((BuildingMiner) building).setProgressPos(null, null);
        }
    }

    @Override
    public int getModuleMax()
    {
        return 1;
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.quarrier.get();
    }

    /**
     * Check if the quarry was completed already.
     * @return true if so.
     */
    public boolean isFinished()
    {
        return isFinished;
    }

    /**
     * Set the quarry as finished.
     */
    public void setFinished()
    {
        isFinished = true;
        markDirty();
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();
        builder.add(new StationRequestResolver(building.getRequester().getLocation(), building.getColony().getRequestManager()
            .getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        return builder.build();
    }

    @Override
    protected String getModuleSerializationIdentifier()
    {
        return TAG_QUARRY_ASSIGNMENT;
    }

    @Override
    public net.minecraft.util.Tuple<BlockPos, BlockPos> getAdditionalCorners()
    {
        return new Tuple<>(new BlockPos(0, this.height, 0), new BlockPos(0, 0, 0));
    }
}
