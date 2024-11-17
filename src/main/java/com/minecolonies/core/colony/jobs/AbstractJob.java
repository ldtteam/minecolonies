package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.ai.ITickingStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.interactionhandling.RequestBasedInteraction;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_JOB_TYPE;
import static com.minecolonies.api.util.constant.Suppression.CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION;

/**
 * Basic job information.
 * <p>
 * Suppressing Sonar Rule squid:S2390 This rule does "Classes should not access static members of their own subclasses during initialization" But in this case the rule does not
 * apply because We are only mapping classes and that is reasonable
 */
@SuppressWarnings(CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION)
public abstract class AbstractJob<AI extends AbstractAISkeleton<J> & ITickingStateAI, J extends AbstractJob<AI, J>> implements IJob<AI>
{
    private static final String TAG_ASYNC_REQUESTS = "asyncRequests";
    private static final String TAG_ACTIONS_DONE   = "actionsDone";
    private static final String TAG_WORK_POS = "workPos";

    /**
     * Job associated to the abstract job.
     */
    private JobEntry entry;

    /**
     * A counter to dump the inventory after x actions.
     */
    private int actionsDone = 0;

    /**
     * Citizen connected with the job.
     */
    private final ICitizenData citizen;

    /**
     * The name tag of the job.
     */
    private String nameTag = "";

    /**
     * A set of tokens that point to requests for which we do not wait.
     */
    private final Set<IToken<?>> asyncRequests = new HashSet<>();

    /**
     * Check if the worker has searched for food today.
     */
    private boolean searchedForFoodToday;

    /**
     * Position of the work building
     */
    protected BlockPos workBuildingPos = null;

    /**
     * The work building
     */
    protected IBuilding workBuilding = null;

    /**
     * The work module we're assigned to
     */
    protected IAssignsJob workModule = null;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJob(final ICitizenData entity)
    {
        this.citizen = entity;
    }

    @Override
    public boolean pickupSuccess(@NotNull ItemStack pickedUpStack)
    {
        return true;
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.CITIZEN_ID;
    }

    @Override
    public IColony getColony()
    {
        return citizen.getColony();
    }

    @Override
    public void setRegistryEntry(final JobEntry jobEntry)
    {
        this.entry = jobEntry;
    }

    @Override
    public BlockPos getBuildingPos()
    {
        return workBuildingPos;
    }

    @Override
    public IBuilding getWorkBuilding()
    {
        return workBuilding;
    }

    @Override
    public IAssignsJob getWorkModule()
    {
        return workModule;
    }

    @Override
    public boolean assignTo(final IAssignsJob module)
    {
        if (module == null || !module.getJobEntry().equals(getJobRegistryEntry()))
        {
            return false;
        }

        if (workBuilding != null && workBuilding != module.getBuilding())
        {
            for (final IAssignsJob oldJobModule : workBuilding.getModulesByType(IAssignsJob.class))
            {
                if (oldJobModule.hasAssignedCitizen(citizen))
                {
                    oldJobModule.removeCitizen(citizen);
                }
            }
        }

        workBuilding = module.getBuilding();
        workBuildingPos = workBuilding.getID();
        workModule = module;

        citizen.setJob(this);
        return true;
    }

    @Override
    final public JobEntry getJobRegistryEntry()
    {
        return this.entry;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();

        compound.putString(TAG_JOB_TYPE, getJobRegistryEntry().getKey().toString());
        compound.put(TAG_ASYNC_REQUESTS,
          getAsyncRequests().stream()
            .filter(token -> getColony().getRequestManager().getRequestForToken(token) != null)
            .map(StandardFactoryController.getInstance()::serialize)
            .collect(NBTUtils.toListNBT()));
        compound.putInt(TAG_ACTIONS_DONE, actionsDone);

        if (workBuildingPos != null)
        {
            BlockPosUtil.write(compound, TAG_WORK_POS, workBuildingPos);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        this.asyncRequests.clear();
        if (compound.contains(TAG_ASYNC_REQUESTS))
        {
            this.asyncRequests.addAll(NBTUtils.streamCompound(compound.getList(TAG_ASYNC_REQUESTS, Tag.TAG_COMPOUND))
                                        .map(StandardFactoryController.getInstance()::deserialize)
                                        .map(o -> (IToken<?>) o)
                                        .collect(Collectors.toSet()));
        }
        if (compound.contains(TAG_ACTIONS_DONE))
        {
            actionsDone = compound.getInt(TAG_ACTIONS_DONE);
        }

        if (compound.contains(TAG_WORK_POS))
        {
            workBuildingPos = BlockPosUtil.read(compound, TAG_WORK_POS);
        }
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buffer)
    {
        buffer.writeUtf(getJobRegistryEntry().getKey().toString());
        buffer.writeInt(getAsyncRequests().size());
        for (final IToken<?> token : getAsyncRequests())
        {
            StandardFactoryController.getInstance().serialize(buffer, token);
        }
        buffer.writeRegistryId(IJobRegistry.getInstance(), getJobRegistryEntry());
    }

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    @Override
    public Set<IToken<?>> getAsyncRequests()
    {
        return asyncRequests;
    }

    @Override
    public void markRequestSync(final IToken<?> id)
    {
        final IRequest<?> request = citizen.getColony().getRequestManager().getRequestForToken(id);

        if (request != null)
        {
            citizen.triggerInteraction(new RequestBasedInteraction(Component.translatable(RequestSystemTranslationConstants.REQUEST_RESOLVER_NORMAL,
              request.getLongDisplayString()), ChatPriority.BLOCKING, Component.translatable(RequestSystemTranslationConstants.REQUEST_RESOLVER_NORMAL), request.getId()));
        }

        asyncRequests.remove(id);
    }

    @Override
    public void createAI()
    {
        final AI tempAI = generateAI();

        if (tempAI == null)
        {
            Log.getLogger().error("Failed to create AI for citizen!", new Exception());
            if (citizen == null)
            {
                Log.getLogger().error("CitizenData is null for job: " + nameTag + " jobClass: " + this.getClass(), new Exception());
                return;
            }
            Log.getLogger()
              .error(
                "Affected Citizen name:" + citizen.getName() + " id:" + citizen.getId() + " job:" + citizen.getJob() + " jobForAICreation:" + nameTag + " class:" + this.getClass()
                  + " entityPresent:"
                  + citizen.getEntity().isPresent());
            return;
        }

        citizen.getEntity().get().getCitizenJobHandler().setWorkAI(tempAI);
    }

    @Override
    public boolean hasCheckedForFoodToday()
    {
        return searchedForFoodToday;
    }

    @Override
    public void setCheckedForFood()
    {
        searchedForFoodToday = true;
    }

    @Override
    public String getNameTagDescription()
    {
        return this.nameTag;
    }

    @Override
    public final void setNameTag(final String nameTag)
    {
        this.nameTag = nameTag;
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {

    }

    @Override
    public boolean onStackPickUp(@NotNull final ItemStack pickedUpStack)
    {
        if (getCitizen().getWorkBuilding() != null)
        {
            if (getCitizen().getWorkBuilding().overruleNextOpenRequestOfCitizenWithStack(getCitizen(), pickedUpStack.copy()))
            {
                return true;
            }
        }

        if (getCitizen().getHomeBuilding() != null)
        {
            return getCitizen().getHomeBuilding().overruleNextOpenRequestOfCitizenWithStack(getCitizen(), pickedUpStack.copy());
        }

        return false;
    }

    @Override
    public ICitizenData getCitizen()
    {
        return citizen;
    }

    @Override
    public void onWakeUp()
    {
        searchedForFoodToday = false;
    }

    @Override
    public boolean canAIBeInterrupted()
    {
        if (getWorkerAI() != null)
        {
            return getWorkerAI().canBeInterrupted();
        }

        return true;
    }

    @Override
    public int getActionsDone()
    {
        return actionsDone;
    }

    @Override
    public void incrementActionsDone()
    {
        actionsDone++;
    }

    @Override
    public void incrementActionsDone(final int numberOfActions)
    {
        actionsDone += numberOfActions;
    }

    @Override
    public void clearActionsDone()
    {
        this.actionsDone = 0;
    }

    @Override
    public AI getWorkerAI()
    {
        if (citizen.getEntity().isPresent())
        {
            return (AI) citizen.getEntity().get().getCitizenJobHandler().getWorkAI();
        }

        return null;
    }

    @Override
    public boolean isIdling()
    {
        return (getWorkerAI() != null && getWorkerAI().getState() == AIWorkerState.IDLE);
    }

    @Override
    public void resetAI()
    {
        if (getWorkerAI() != null)
        {
            getWorkerAI().resetAI();
        }
    }

    @Override
    public boolean allowsAvoidance()
    {
        return true;
    }

    @Override
    public double getDiseaseModifier()
    {
        return 1;
    }

    @Override
    public void onRemoval()
    {
        citizen.setJob(null);

        if (getWorkerAI() != null)
        {
            getWorkerAI().onRemoval();
        }

        if (workBuilding != null)
        {
            for (final IAssignsJob oldJobModule : workBuilding.getModulesByType(IAssignsJob.class))
            {
                if (oldJobModule.hasAssignedCitizen(citizen))
                {
                    oldJobModule.removeCitizen(citizen);
                }
            }
        }

        workBuilding = null;
        workModule = null;
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        return false;
    }

    @Override
    public void processOfflineTime(final long time)
    {
        // Do Nothing.
    }
}
