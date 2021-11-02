package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The living module for citizen to call their home.
 */
public class WorkerBuildingModule extends AbstractAssignedCitizenModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    /**
     * Module specific skills.
     */
    private final Skill primary;
    private final Skill secondary;

    /**
     * Job identifier.
     */
    private final String                      jobID;

    /**
     * Job creator function.
     */
    private final Function<ICitizenData, IJob<?>> jobCreator;

    /**
     * Check if this worker by default can work in the rain.
     */
    private final boolean                                 canWorkingDuringRain;

    /**
     * Max size in terms of assignees.
     */
    private final Function<IBuilding, Integer> sizeLimit;

    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode  hiringMode = HiringMode.DEFAULT;

    /**
     * The display name of the job - post localization
     */
    private String jobDisplayName = "";

    public WorkerBuildingModule(final Function<ICitizenData, IJob<?>> jobCreator,
      final String jobName,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        this.jobCreator = jobCreator;
        this.jobID = jobName;
        this.primary = primary;
        this.secondary = secondary;
        this.canWorkingDuringRain = canWorkingDuringRain;
        this.sizeLimit = sizeLimit;
    }

    //todo, where we call this, we want to make two things sure:
    // a) Always call a specific one
    // b) If there is a living module, call this together for assign and remove.
    //todo, Always when a citizen moves out of a housing building into a working building, notify player.
    /*LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
                      "com.minecolonies.coremod.gui.workerhuts.archertraineeassignbed",
                      citizen.getName(),
                      LanguageHandler.format("block.minecolonies." + building.getBuildingType().getBuildingBlock().getHutName() + ".name"),
                                                                                                                                  BlockPosUtil.getString(building.getID()));*/
    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (citizen.getWorkBuilding() != null)
        {
            citizen.getWorkBuilding().getFirstOptionalModuleOccurance(WorkerBuildingModule.class).ifPresent(m -> m.removeCitizen(citizen));
        }

        if (!super.assignCitizen(citizen))
        {
            Log.getLogger().warn("Unable to assign citizen:" + citizen.getName() + " to building:" + building.getSchematicName() + " jobname:" + getJobDisplayName());
            return false;
        }
        return true;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        if (compound.getAllKeys().contains(TAG_WORKER))
        {
            final ListNBT workersTagList = compound.getList(TAG_WORKER, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < workersTagList.size(); ++i)
            {
                final ICitizenData data = building.getColony().getCitizenManager().getCivilian(workersTagList.getCompound(i).getInt(TAG_WORKER_ID));
                if (data != null)
                {
                    assignCitizen(data);
                }
            }
        }
        else if (compound.contains(TAG_WORKING_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_WORKING_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
                if (citizen != null)
                {
                    assignCitizen(citizen);
                }
            }
        }
        this.hiringMode = HiringMode.values()[compound.getInt(TAG_HIRING_MODE)];
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && ((building.getBuildingLevel() > 0 && building.isBuilt()) || building instanceof BuildingBuilder)
              && (this.hiringMode == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.hiringMode == HiringMode.AUTO))
        {
            final ICitizenData joblessCitizen = colony.getCitizenManager().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                assignCitizen(joblessCitizen);
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        if (!assignedCitizen.isEmpty())
        {
            @NotNull final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            compound.putIntArray(TAG_WORKING_RESIDENTS, residentIds);
        }
        compound.putInt(TAG_HIRING_MODE, this.hiringMode.ordinal());
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(hiringMode.ordinal());
        buf.writeUtf(this.getJobID());
        buf.writeInt(getModuleMax());
        buf.writeInt(getPrimarySkill().ordinal());
        buf.writeInt(getSecondarySkill().ordinal());
        buf.writeUtf(getJobDisplayName());
    }

    @Override
    void onAssignment(final ICitizenData citizen)
    {
        citizen.setWorkBuilding(building);
        for (final AbstractCraftingBuildingModule module : building.getModules(AbstractCraftingBuildingModule.class))
        {
            module.updateWorkerAvailableForRecipes();
        }
        citizen.getJob().onLevelUp();
        building.getColony().getProgressManager().progressEmploy((int) building.getColony().getCitizenManager().getCitizens().stream().filter(citizenData -> citizenData.getJob() != null).count());
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        citizen.setWorkBuilding(null);
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
        if (jobDisplayName.isEmpty())
        {
            jobDisplayName = createJob(null).getName();
        }
        return jobDisplayName;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return jobCreator.apply(citizen);
    }

    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        this.markDirty();
    }

    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    @NotNull
    @Override
    public String getJobID()
    {
        return jobID;
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
                                                                                   .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
          new PrivateWorkerCraftingProductionResolver(building.getRequester().getLocation(), building.getColony().getRequestManager()
                                                                                      .getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        return builder.build();
    }
}
