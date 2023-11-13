package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The worker module for citizen where they are assigned to if they work at it.
 */
public class WorkerBuildingModule extends AbstractAssignedCitizenModule
  implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    /**
     * Module specific skills.
     */
    private final Skill primary;
    private final Skill secondary;

    /**
     * Job creator function.
     */
    private final JobEntry jobEntry;

    /**
     * Check if this worker by default can work in the rain.
     */
    private final boolean canWorkingDuringRain;

    /**
     * Max size in terms of assignees.
     */
    private final Function<IBuilding, Integer> sizeLimit;

    public WorkerBuildingModule(
      final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        this.jobEntry = entry;
        this.primary = primary;
        this.secondary = secondary;
        this.canWorkingDuringRain = canWorkingDuringRain;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (assignedCitizen.contains(citizen) || isFull() || citizen == null)
        {
            return false;
        }

        IJob job = citizen.getJob();
        if (job == null)
        {
            job = createJob(citizen);
        }

        if (!job.assignTo(this))
        {
            return false;
        }

        return super.assignCitizen(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_WORKER))
        {
            final ListTag workersTagList = compound.getList(TAG_WORKER, Tag.TAG_COMPOUND);
            for (int i = 0; i < workersTagList.size(); ++i)
            {
                final ICitizenData data = building.getColony().getCitizenManager().getCivilian(workersTagList.getCompound(i).getInt(TAG_WORKER_ID));
                if (data != null && data.getJob() != null && data.getJob().getJobRegistryEntry() == jobEntry)
                {
                    assignCitizen(data);
                }
            }
        }
        else if (compound.contains(jobEntry.getKey().toString()))
        {
            final CompoundTag jobCompound = compound.getCompound(jobEntry.getKey().toString());
            final int[] residentIds = jobCompound.getIntArray(TAG_WORKING_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
                if (citizen != null)
                {
                    assignCitizen(citizen);
                }
            }
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && ((building.getBuildingLevel() > 0 && building.isBuilt()) || building instanceof BuildingBuilder)
              && (this.getHiringMode() == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.getHiringMode() == HiringMode.AUTO))
        {
            final ICitizenData joblessCitizen = colony.getCitizenManager().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                assignCitizen(joblessCitizen);
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        super.serializeNBT(compound);
        final CompoundTag jobCompound = compound.contains(getModuleSerializationIdentifier()) ? compound.getCompound(getModuleSerializationIdentifier()) : new CompoundTag();
        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            jobCompound.putIntArray(TAG_WORKING_RESIDENTS, residentIds);
        }
        compound.put(getModuleSerializationIdentifier(), jobCompound);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeRegistryId(IMinecoloniesAPI.getInstance().getJobRegistry(), jobEntry);
        buf.writeInt(getPrimarySkill().ordinal());
        buf.writeInt(getSecondarySkill().ordinal());
    }

    @Override
    void onAssignment(final ICitizenData citizen)
    {
        for (final AbstractCraftingBuildingModule module : building.getModules(AbstractCraftingBuildingModule.class))
        {
            module.updateWorkerAvailableForRecipes();
        }
        citizen.getJob().onLevelUp();
        building.getColony()
          .getProgressManager()
          .progressEmploy((int) building.getColony().getCitizenManager().getCitizens().stream().filter(citizenData -> citizenData.getJob() != null).count());
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        if (citizen.getJob() != null)
        {
            citizen.getJob().onRemoval();
        }

        building.cancelAllRequestsOfCitizen(citizen);
        citizen.setVisibleStatus(null);
    }

    @Override
    public int getModuleMax()
    {
        return sizeLimit.apply(this.building);
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        for (final Optional<AbstractEntityCitizen> entityCitizen : Objects.requireNonNull(getAssignedEntities()))
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    /**
     * Get the Job DisplayName
     */
    public String getJobDisplayName()
    {
        return Component.translatable(jobEntry.getTranslationKey()).getString();
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return jobEntry.produceJob(citizen);
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return building.getBuildingLevel() >= building.getMaxBuildingLevel() || canWorkingDuringRain;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return primary;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return secondary;
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();
        builder.add(new BuildingRequestResolver(building.getRequester().getLocation(), building.getColony().getRequestManager()
            .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
          new PrivateWorkerCraftingRequestResolver(building.getRequester().getLocation(), building.getColony().getRequestManager()
            .getFactoryController().getNewInstance(TypeConstants.ITOKEN), jobEntry),
          new PrivateWorkerCraftingProductionResolver(building.getRequester().getLocation(), building.getColony().getRequestManager()
            .getFactoryController().getNewInstance(TypeConstants.ITOKEN), jobEntry));
        return builder.build();
    }

    @Override
    public JobEntry getJobEntry()
    {
        return jobEntry;
    }

    @Override
    protected String getModuleSerializationIdentifier()
    {
        return jobEntry.getKey().toString();
    }
}
