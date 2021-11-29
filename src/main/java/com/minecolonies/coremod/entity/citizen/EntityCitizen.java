package com.minecolonies.coremod.entity.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.combat.threat.ThreatTable;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenDiedEvent;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.MovementHandler;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookAtWithoutMovingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static com.minecolonies.api.entity.citizen.VisibleCitizenStatus.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble.*;

/**
 * The Class used to represent the citizen entities.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.ExcessiveClassLength"})
public class EntityCitizen extends AbstractEntityCitizen implements IThreatTableEntity
{
    /**
     * Cooldown for calling help, in ticks.
     */
    private static final int    CALL_HELP_CD        = 100;
    /**
     * The amount of damage a guard takes on blocking.
     */
    private static final float  GUARD_BLOCK_DAMAGE  = 0.5f;
    /**
     * Max speed factor.
     */
    private static final double MAX_SPEED_FACTOR    = 0.5;
    private static final int    CALL_TO_HELP_AMOUNT = 2;

    /**
     * The citizen status handler.
     */
    private final ICitizenStatusHandler     citizenStatusHandler;
    /**
     * It's citizen Id.
     */
    private       int                       citizenId = 0;
    /**
     * The Walk to proxy (Shortest path through intermediate blocks).
     */
    private       IWalkToProxy              proxy;
    /**
     * Reference to the data representation inside the colony.
     */
    private       ICitizenData              citizenData;
    /**
     * The citizen experience handler.
     */
    private       ICitizenExperienceHandler citizenExperienceHandler;
    /**
     * The citizen chat handler.
     */
    private       ICitizenChatHandler       citizenChatHandler;
    /**
     * The citizen item handler.
     */
    private       ICitizenItemHandler       citizenItemHandler;
    /**
     * The citizen inv handler.
     */
    private       ICitizenInventoryHandler  citizenInventoryHandler;

    /**
     * The citizen colony handler.
     */
    private ICitizenColonyHandler citizenColonyHandler;

    /**
     * The citizen job handler.
     */
    private ICitizenJobHandler citizenJobHandler;

    /**
     * The citizen sleep handler.
     */
    private ICitizenSleepHandler citizenSleepHandler;

    /**
     * The citizen sleep handler.
     */
    private ICitizenDiseaseHandler citizenDiseaseHandler;

    /**
     * The path-result of trying to move away
     */
    private PathResult moveAwayPath;

    /**
     * IsChild flag
     */
    private boolean child = false;

    /**
     * Whether the citizen is currently running away
     */
    private boolean currentlyFleeing = false;

    /**
     * Timer for the call for help cd.
     */
    private int callForHelpCooldown = 0;

    /**
     * Distance walked for consuming food
     */
    private float lastDistanceWalked = 0;

    /**
     * Citizen data view.
     */
    private ICitizenDataView citizenDataView;

    /**
     * The location used for requests
     */
    private ILocation location = null;

    /**
     * Cached team name the entity belongs to.
     */
    private String cachedTeamName;

    /**
     * Our entities threat list
     */
    private final ThreatTable threatTable = new ThreatTable<>(this);

    /**
     * The entities states
     */
    private enum EntityState implements IState
    {
        INIT,
        ACTIVE_SERVER,
        ACTIVE_CLIENT,
        INACTIVE;
    }

    /**
     * The statemachine for citizens
     */
    private ITickRateStateMachine<EntityState> entityStatemachine = new TickRateStateMachine<>(EntityState.INIT, e -> Log.getLogger().warn(e));

    /**
     * The desired activity of the citizen
     */
    private DesiredActivity desiredActivity = DesiredActivity.IDLE;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the entity type.
     * @param world the world.
     */
    public EntityCitizen(final EntityType<? extends AgeableEntity> type, final World world)
    {
        super(type, world);
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);
        this.citizenChatHandler = new CitizenChatHandler(this);
        this.citizenStatusHandler = new CitizenStatusHandler(this);
        this.citizenItemHandler = new CitizenItemHandler(this);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenColonyHandler = new CitizenColonyHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenDiseaseHandler = new CitizenDiseaseHandler(this);

        this.moveControl = new MovementHandler(this);
        this.setPersistenceRequired();
        this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());

        entityStatemachine.addTransition(new TickingTransition<>(EntityState.INIT, () -> true, this::initialize, 40));

        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_CLIENT, () -> {
            citizenColonyHandler.updateColonyClient();
            return false;
        }, () -> null, 1));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_CLIENT, this::shouldBeInactive, () -> EntityState.INACTIVE, TICKS_20));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_CLIENT, this::refreshCitizenDataView, () -> null, TICKS_20));

        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::updateSaturation, () -> null, HEAL_CITIZENS_AFTER));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::updateVisualData, () -> null, 200));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::onServerUpdateHandlers, () -> null, TICKS_20));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::onTickDecrements, () -> null, 1));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::shouldBeInactive, () -> EntityState.INACTIVE, TICKS_20));
        entityStatemachine.addTransition(new TickingTransition<>(EntityState.ACTIVE_SERVER, this::determineDesiredActivity, () -> null, 100));

        entityStatemachine.addTransition(new TickingTransition<>(EntityState.INACTIVE, this::isAlive, () -> EntityState.INIT, 100));
    }

    /**
     * Whether the entity should be inactive
     *
     * @return
     */
    private boolean shouldBeInactive()
    {
        if (citizenData == null && citizenDataView == null)
        {
            return true;
        }
        return !isAlive();
    }

    /**
     * Initializes vital colony and data connections before the entity is active
     */
    private EntityState initialize()
    {
        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.cachedTeamName = colonyView.getTeamName();
                    this.citizenDataView = colonyView.getCitizen(citizenId);
                    if (citizenDataView != null)
                    {
                        initTasks();
                        return EntityState.ACTIVE_CLIENT;
                    }
                }
            }
        }
        else
        {
            citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
            if (citizenData != null && isAlive() && citizenColonyHandler.getColony() != null)
            {
                initTasks();
                return EntityState.ACTIVE_SERVER;
            }
        }

        return null;
    }

    /**
     * Initiates citizen goalSelector Suppressing Sonar Rule Squid:S881 The rule thinks we should extract ++priority in a proper statement. But in this case the rule does not apply
     * because that would remove the readability.
     */
    @SuppressWarnings(INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION)
    private void initTasks()
    {
        int priority = 0;
        this.goalSelector.addGoal(priority, new SwimGoal(this));
        this.goalSelector.addGoal(++priority,
          new EntityAICitizenAvoidEntity(this, MonsterEntity.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        this.goalSelector.addGoal(++priority, new EntityAIEatTask(this));
        this.goalSelector.addGoal(++priority, new EntityAISickTask(this));
        this.goalSelector.addGoal(++priority, new EntityAISleep(this));
        this.goalSelector.addGoal(priority, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.goalSelector.addGoal(++priority, new LookAtWithoutMovingGoal(this, PlayerEntity.class, WATCH_CLOSEST2, 1.0F));
        this.goalSelector.addGoal(++priority, new LookAtWithoutMovingGoal(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.goalSelector.addGoal(++priority, new EntityAICitizenWander(this, DEFAULT_SPEED, 1.0D));
        this.goalSelector.addGoal(++priority, new LookAtGoal(this, LivingEntity.class, WATCH_CLOSEST));
        this.goalSelector.addGoal(++priority, new EntityAIMournCitizen(this, DEFAULT_SPEED));
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    @NotNull
    @Override
    public ActionResultType checkAndHandleImportantInteractions(final PlayerEntity player, @NotNull final Hand hand)
    {
        final IColonyView iColonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), player.level.dimension());
        if (iColonyView != null && !iColonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return ActionResultType.FAIL;
        }

        if (!ItemStackUtils.isEmpty(player.getItemInHand(hand)) && player.getItemInHand(hand).getItem() instanceof NameTagItem)
        {
            return super.checkAndHandleImportantInteractions(player, hand);
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide && iColonyView != null)
        {
            if (player.isShiftKeyDown())
            {
                Network.getNetwork().sendToServer(new OpenInventoryMessage(iColonyView, this.getName().getString(), this.getId()));
            }
            else
            {
                final ICitizenDataView citizenDataView = getCitizenDataView();
                if (citizenDataView != null)
                {
                    MineColonies.proxy.showCitizenWindow(citizenDataView);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public String getScoreboardName()
    {
        return getName().getString() + " (" + getCivilianID() + ")";
    }

    /**
     * Getter of the dataview, the clientside representation of the citizen.
     *
     * @return the view.
     */
    @Override
    public ICitizenDataView getCitizenDataView()
    {
        if (this.citizenDataView == null)
        {
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getCitizen(citizenId);
                    return this.citizenDataView;
                }
            }
        }
        else
        {
            return this.citizenDataView;
        }

        return null;
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_STATUS, citizenStatusHandler.getStatus().ordinal());
        if (citizenColonyHandler.getColony() != null && citizenData != null)
        {
            compound.putInt(TAG_COLONY_ID, citizenColonyHandler.getColony().getID());
            compound.putInt(TAG_CITIZEN, citizenData.getId());
        }
        citizenDiseaseHandler.write(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT compound)
    {
        super.readAdditionalSaveData(compound);

        citizenStatusHandler.setStatus(Status.values()[compound.getInt(TAG_STATUS)]);
        citizenColonyHandler.setColonyId(compound.getInt(TAG_COLONY_ID));
        citizenId = compound.getInt(TAG_CITIZEN);
        citizenDiseaseHandler.read(compound);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons. use this to react to sunlight and start to burn.
     */
    @Override
    public void aiStep()
    {
        super.aiStep();
        entityStatemachine.tick();
    }

    /**
     * Refreshes the saved view data
     *
     * @return false
     */
    public boolean refreshCitizenDataView()
    {
        if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
            if (colonyView != null)
            {
                this.citizenDataView = colonyView.getCitizen(citizenId);
                this.getNavigation().getPathingOptions().setCanUseRails(canPathOnRails());
                this.getNavigation().getPathingOptions().setCanClimbVines(canClimbVines());
            }
        }
        return false;
    }

    /**
     * Decrements values each tick
     *
     * @return false
     */
    private boolean onTickDecrements()
    {
        decrementCallForHelpCooldown();
        decreaseWalkingSaturation();
        return false;
    }

    /**
     * Updates handlers on living tick, each 20 ticks.
     */
    private boolean onServerUpdateHandlers()
    {
        // Every 20 ticks
        citizenExperienceHandler.gatherXp();
        citizenItemHandler.pickupItems();
        citizenData.setLastPosition(blockPosition());
        citizenDiseaseHandler.tick();
        onLivingSoundUpdate();
        return false;
    }

    /**
     * Updates visual data for the citizen
     *
     * @return false
     */
    private boolean updateVisualData()
    {
        final ItemStack hat = getItemBySlot(EquipmentSlotType.HEAD);
        if (LocalDate.now(Clock.systemDefaultZone()).getMonth() == Month.DECEMBER
              && MineColonies.getConfig().getServer().holidayFeatures.get()
              && !(getCitizenJobHandler().getColonyJob() instanceof JobStudent))
        {
            if (hat.isEmpty())
            {
                this.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.santaHat));
            }
        }
        else if (!hat.isEmpty() && hat.getItem() == ModItems.santaHat)
        {
            this.setItemSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
        }
        this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());

        if (!citizenColonyHandler.getColony().getTextureStyleId().equals(getEntityData().get(DATA_STYLE)))
        {
            getEntityData().set(DATA_STYLE, citizenColonyHandler.getColony().getTextureStyleId());
        }
        if (!citizenData.getTextureSuffix().equals(getEntityData().get(DATA_TEXTURE_SUFFIX)))
        {
            getEntityData().set(DATA_TEXTURE_SUFFIX, citizenData.getTextureSuffix());
        }

        return false;
    }

    /**
     * Adds potion effect and regenerates life, depending on saturation
     */
    private boolean updateSaturation()
    {
        checkHeal();
        if (citizenData.getSaturation() <= 0)
        {
            if (this.getEffect(Effects.MOVEMENT_SLOWDOWN) == null)
            {
                this.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, TICKS_SECOND * 30));
            }
        }
        else
        {
            this.removeEffect(Effects.MOVEMENT_SLOWDOWN);
        }
        return false;
    }

    private void decrementCallForHelpCooldown()
    {
        if (callForHelpCooldown > 0)
        {
            callForHelpCooldown--;
        }
    }

    /**
     * A boolean check to test if the citizen can path on rails.
     *
     * @return true if so.
     */
    public boolean canPathOnRails()
    {
        if (level.isClientSide)
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
            if (colonyView != null)
            {
                return colonyView.getResearchManager().getResearchEffects().getEffectStrength(RAILS) > 0;
            }
            return false;
        }
        return getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(RAILS) > 0;
    }

    /**
     * A boolean check to test if the citizen can climb vines.
     *
     * @return true if so.
     */
    public boolean canClimbVines()
    {
        if (level.isClientSide)
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
            if (colonyView != null)
            {
                return colonyView.getResearchManager().getResearchEffects().getEffectStrength(VINES) > 0;
            }
            return false;
        }
        return getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(VINES) > 0;
    }


    /**
     * Reduces saturation for walking every 25 blocks.
     */
    private void decreaseWalkingSaturation()
    {
        if (walkDist - lastDistanceWalked > ACTIONS_EACH_BLOCKS_WALKED)
        {
            lastDistanceWalked = walkDist;
            decreaseSaturationForContinuousAction();
        }
    }

    /**
     * Checks the citizens health status and heals the citizen if necessary.
     */
    private void checkHeal()
    {
        if (getHealth() < getMaxHealth())
        {
            final double limitDecrease = getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SATLIMIT);

            final double healAmount;
            if (citizenData.getSaturation() >= FULL_SATURATION + limitDecrease)
            {
                healAmount = 2 * (1.0 + getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }
            else if (citizenData.getSaturation() < LOW_SATURATION)
            {
                return;
            }
            else
            {
                healAmount = 1 * (1.0 + getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }

            heal((float) healAmount);
            if (healAmount > 0.1D)
            {
                citizenData.markDirty();
            }
        }
    }

    /**
     * Plays a random sound by chance during day
     */
    private void onLivingSoundUpdate()
    {
        if (WorldUtil.isDayTime(level))
        {
            SoundUtils.playRandomSound(level, this.blockPosition(), citizenData);
        }
    }

    @Override
    public boolean isBaby()
    {
        return child;
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_COLONY_ID, citizenColonyHandler == null ? 0 : citizenColonyHandler.getColonyId());
        entityData.define(DATA_CITIZEN_ID, citizenId);
    }

    /**
     * Set the metadata for rendering.
     *
     * @param metadata the metadata required.
     */
    @Override
    public void setRenderMetadata(final String metadata)
    {
        super.setRenderMetadata(metadata);

        //Display some debug info always available while testing
        //Will help track down some hard to find bugs (Pathfinding etc.)
        if (citizenJobHandler.getColonyJob() != null && MineColonies.getConfig().getServer().enableInDevelopmentFeatures.get())
        {
            setCustomName(new StringTextComponent(
              citizenData.getName() + " (" + citizenStatusHandler.getStatus() + ")[" + citizenJobHandler.getColonyJob().getNameTagDescription() + "]"));
        }
    }

    /**
     * Get the ILocation of the citizen.
     *
     * @return an ILocation object which contains the dimension and is unique.
     */
    @Override
    public ILocation getLocation()
    {
        if (location == null)
        {
            location = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ILOCATION, this);
        }
        return location;
    }

    /**
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    @Override
    public boolean isWorkerAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (proxy == null)
        {
            proxy = new EntityCitizenWalkToProxy(this);
        }
        return proxy.walkToBlock(site, range, true);
    }

    /**
     * Getter for the citizendata. Tries to get it from the colony is the data is null.
     *
     * @return the data.
     */
    @Override
    public ICitizenData getCitizenData()
    {
        return citizenData;
    }

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    @Override
    public void setCivilianData(@Nullable final ICivilianData data)
    {
        if (data != null)
        {
            this.citizenData = (ICitizenData) data;
            data.initEntityValues();
        }
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @Override
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return getCitizenData().getInventory();
    }

    @Override
    @NotNull
    public IItemHandler getItemHandlerCitizen()
    {
        return getInventoryCitizen();
    }

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    @Override
    public void markDirty()
    {
        if (citizenData != null)
        {
            citizenData.markDirty();
        }
    }

    /**
     * Sets the size of the citizen entity
     *
     * @param width  Width
     * @param height Height
     */
    @Override
    public void setCitizensize(final @NotNull float width, final @NotNull float height)
    {
        this.dimensions = new EntitySize(width, height, false);
    }

    /**
     * Sets whether this entity is a child
     *
     * @param isChild boolean
     */
    @Override
    public void setIsChild(final boolean isChild)
    {
        if (isChild && !this.child)
        {
            goalSelector.addGoal(50, new EntityAICitizenChild(this));
            setCitizensize((float) CITIZEN_WIDTH / 2, (float) CITIZEN_HEIGHT / 2);
        }
        else
        {
            if (!isChild && this.child)
            {
                getCitizenJobHandler().setModelDependingOnJob(citizenJobHandler.getColonyJob());
            }
            setCitizensize((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT);
        }
        this.child = isChild;
        this.getEntityData().set(DATA_IS_CHILD, isChild);
        markDirty();
    }

    /**
     * Play move away sound when running from an entity.
     */
    @Override
    public void playMoveAwaySound()
    {
        if (citizenJobHandler.getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWith(level, blockPosition(), EventType.DANGER, getCitizenData());
        }
    }

    /**
     * Get the path proxy of the citizen.
     *
     * @return the proxy.
     */
    @Override
    public IWalkToProxy getProxy()
    {
        return proxy;
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            citizenData.markDirty();
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForContinuousAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            citizenData.markDirty();
        }
    }

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    @Override
    public int getCivilianID()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    @Override
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    /**
     * The Handler for all experience related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    /**
     * The Handler for all chat related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenChatHandler getCitizenChatHandler()
    {
        return citizenChatHandler;
    }

    /**
     * The Handler for all status related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenStatusHandler getCitizenStatusHandler()
    {
        return citizenStatusHandler;
    }

    /**
     * The Handler for all item related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenItemHandler getCitizenItemHandler()
    {
        return citizenItemHandler;
    }

    /**
     * The Handler for all inventory related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenInventoryHandler getCitizenInventoryHandler()
    {
        return citizenInventoryHandler;
    }

    @Override
    public void setCitizenInventoryHandler(final ICitizenInventoryHandler citizenInventoryHandler)
    {
        this.citizenInventoryHandler = citizenInventoryHandler;
    }

    /**
     * The Handler for all colony related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenColonyHandler getCitizenColonyHandler()
    {
        return citizenColonyHandler;
    }

    @Override
    public void setCitizenColonyHandler(final ICitizenColonyHandler citizenColonyHandler)
    {
        this.citizenColonyHandler = citizenColonyHandler;
    }

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenJobHandler getCitizenJobHandler()
    {
        return citizenJobHandler;
    }

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenSleepHandler getCitizenSleepHandler()
    {
        return citizenSleepHandler;
    }

    /**
     * The Handler to check if a citizen is sick.
     *
     * @return the instance of the handler.
     */
    @Override
    public ICitizenDiseaseHandler getCitizenDiseaseHandler()
    {
        return citizenDiseaseHandler;
    }

    @Override
    public void setCitizenDiseaseHandler(final ICitizenDiseaseHandler citizenDiseaseHandler)
    {
        this.citizenDiseaseHandler = citizenDiseaseHandler;
    }

    /**
     * Check if the citizen can eat now by considering the state and the job goalSelector.
     *
     * @return true if so.
     */
    @Override
    public boolean isOkayToEat()
    {
        return !getCitizenSleepHandler().isAsleep() && getDesiredActivity() != DesiredActivity.SLEEP && (citizenJobHandler.getColonyJob() == null
                                                                                                           || citizenJobHandler.getColonyJob().canAIBeInterrupted());
    }

    /**
     * Check if the citizen can be fed.
     *
     * @return true if so.
     */
    @Override
    public boolean shouldBeFed()
    {
        return this.getCitizenData() != null && this.getCitizenData().getSaturation() <= AVERAGE_SATURATION && !this.getCitizenData().justAte() && isOkayToEat();
    }

    /**
     * Check if the citizen is just idling at their job and can eat now.
     *
     * @return true if so.
     */
    @Override
    public boolean isIdlingAtJob()
    {
        return isOkayToEat() && (citizenJobHandler.getColonyJob() == null || citizenJobHandler.getColonyJob().isIdling());
    }

    /**
     * Determines the desired activity
     */
    private boolean determineDesiredActivity()
    {
        if (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard)
        {
            desiredActivity = DesiredActivity.WORK;
            return false;
        }

        if (getCitizenColonyHandler().getColony().getRaiderManager().isRaided())
        {
            citizenData.triggerInteraction(new StandardInteraction(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAID), ChatPriority.IMPORTANT));
            setVisibleStatusIfNone(RAIDED);
            desiredActivity = DesiredActivity.SLEEP;
            return false;
        }

        // Sleeping
        if (!WorldUtil.isPastTime(CompatibilityUtils.getWorldFromCitizen(this), NIGHT - 2000))
        {
            if (desiredActivity == DesiredActivity.SLEEP)
            {
                setVisibleStatusIfNone(SLEEP);
                return false;
            }

            if (citizenSleepHandler.shouldGoSleep())
            {
                citizenData.onGoSleep();
                citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() * 2);
                citizenData.markDirty();
                citizenStatusHandler.setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.sleeping"));
                desiredActivity = DesiredActivity.SLEEP;
                return false;
            }
        }

        // Mourning
        if (citizenData.getCitizenMournHandler().isMourning() && citizenData.getCitizenMournHandler().shouldMourn())
        {
            if (!getCitizenColonyHandler().getColony().getRaiderManager().isRaided())
            {
                citizenData.triggerInteraction(new StandardInteraction(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING, citizenData.getCitizenMournHandler().getDeceasedCitizens().iterator().next()), new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING), ChatPriority.IMPORTANT));
            }
            setVisibleStatusIfNone(MOURNING);
            desiredActivity = DesiredActivity.MOURN;
            return false;
        }

        if (citizenSleepHandler.isAsleep() && !citizenDiseaseHandler.isSick())
        {
            citizenSleepHandler.onWakeUp();
        }

        // Raining
        if (CompatibilityUtils.getWorldFromCitizen(this).isRaining() && !shouldWorkWhileRaining() && !WorldUtil.isNetherType(level))
        {
            citizenStatusHandler.setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.waiting"),
              new TranslationTextComponent("com.minecolonies.coremod.status.rainStop"));
            setVisibleStatusIfNone(BAD_WEATHER);
            if (!citizenData.getColony().getRaiderManager().isRaided()
                  && !citizenData.getCitizenMournHandler().isMourning())
            {
                citizenData.triggerInteraction(new StandardInteraction(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAINING), ChatPriority.HIDDEN));
            }
            desiredActivity = DesiredActivity.SLEEP;
            return false;
        }

        if (isBaby() && getCitizenJobHandler().getColonyJob() instanceof JobPupil && level.getDayTime() % 24000 > NOON)
        {
            setVisibleStatusIfNone(HOUSE);
            desiredActivity = DesiredActivity.IDLE;
            return false;
        }

        if (getCitizenJobHandler().getColonyJob() != null)
        {
            desiredActivity = DesiredActivity.WORK;
            return false;
        }

        setVisibleStatusIfNone(HOUSE);
        desiredActivity = DesiredActivity.IDLE;
        return false;
    }

    /**
     * Sets the visible status if there is none
     *
     * @param status status to set
     */
    private void setVisibleStatusIfNone(final VisibleCitizenStatus status)
    {
        if (getCitizenData().getStatus() == null)
        {
            getCitizenData().setVisibleStatus(status);
        }
    }

    @Override
    @NotNull
    public DesiredActivity getDesiredActivity()
    {
        return desiredActivity;
    }

    /**
     * Checks if the citizen should work even when it rains.
     *
     * @return true if his building level is bigger than 5.
     */
    private boolean shouldWorkWhileRaining()
    {
        return MineColonies.getConfig().getServer().workersAlwaysWorkInRain.get() ||
                 getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(WORKING_IN_RAIN) > 0 ||
                 (citizenColonyHandler.getWorkBuilding() != null
                    && citizenColonyHandler.getWorkBuilding().hasModule(WorkerBuildingModule.class)
                    && citizenColonyHandler.getWorkBuilding().getFirstModuleOccurance(WorkerBuildingModule.class).canWorkDuringTheRain());
    }

    @Override
    public float getRotationYaw()
    {
        return this.yRot;
    }

    @Override
    public float getRotationPitch()
    {
        return this.xRot;
    }

    @Override
    public boolean isDead()
    {
        return !isAlive();
    }

    @Override
    public void setCitizenSleepHandler(final ICitizenSleepHandler citizenSleepHandler)
    {
        this.citizenSleepHandler = citizenSleepHandler;
    }

    @Override
    public void setCitizenJobHandler(final ICitizenJobHandler citizenJobHandler)
    {
        this.citizenJobHandler = citizenJobHandler;
    }

    @Override
    public void setCitizenItemHandler(final ICitizenItemHandler citizenItemHandler)
    {
        this.citizenItemHandler = citizenItemHandler;
    }

    @Override
    public void setCitizenChatHandler(final ICitizenChatHandler citizenChatHandler)
    {
        this.citizenChatHandler = citizenChatHandler;
    }

    @Override
    public void setCitizenExperienceHandler(final ICitizenExperienceHandler citizenExperienceHandler)
    {
        this.citizenExperienceHandler = citizenExperienceHandler;
    }

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if (handleInWallDamage(damageSource))
        {
            return false;
        }

        final Entity sourceEntity = damageSource.getEntity();
        if (!checkIfValidDamageSource(damageSource, damage))
        {
            return false;
        }

        if (getCitizenJobHandler().getColonyJob() != null && getCitizenJobHandler().getColonyJob().ignoresDamage(damageSource))
        {
            return false;
        }

        // Maxdmg cap so citizens need a certain amount of hits to die, so we get more gameplay value and less scaling issues.
        return handleDamagePerformed(damageSource, damage, sourceEntity);
    }

    ///////// -------------------- The Handlers -------------------- /////////

    private boolean handleInWallDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource.getMsgId().equals(DamageSource.IN_WALL.getMsgId()))
        {
            TeleportHelper.teleportCitizen(this, level, blockPosition().offset(0, 1, 0));
            return true;
        }

        return damageSource.getMsgId().equals(DamageSource.IN_WALL.getMsgId()) && citizenSleepHandler.isAsleep()
                 || Compatibility.isDynTreePresent() && damageSource.msgId.equals(Compatibility.getDynamicTreeDamage()) || this.isInvulnerable();
    }

    /**
     * Check if the damage source is valid.
     *
     * @param source the damage source.
     * @param damage the dealt damage.
     * @return true if valid.
     */
    private boolean checkIfValidDamageSource(final DamageSource source, final float damage)
    {
        final Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof EntityCitizen)
        {
            if (((EntityCitizen) sourceEntity).citizenColonyHandler.getColonyId() == citizenColonyHandler.getColonyId())
            {
                return false;
            }

            final IColony attackerColony = ((EntityCitizen) sourceEntity).citizenColonyHandler.getColony();
            if (attackerColony != null && citizenColonyHandler.getColony() != null)
            {
                final IPermissions permission = attackerColony.getPermissions();
                citizenColonyHandler.getColony().getPermissions().addPlayer(permission.getOwner(), permission.getOwnerName(), permission.getRank(permission.HOSTILE_RANK_ID));
            }
        }

        if (sourceEntity instanceof PlayerEntity)
        {
            if (sourceEntity instanceof ServerPlayerEntity)
            {
                if (citizenColonyHandler.getColony().getRaiderManager().isRaided())
                {
                    return false;
                }

                if (damage > 1 && !getCitizenColonyHandler().getColony().getPermissions().hasPermission((PlayerEntity) sourceEntity, Action.HURT_CITIZEN))
                {
                    return false;
                }

                if (getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
                {
                    return IGuardBuilding.checkIfGuardShouldTakeDamage(this, (PlayerEntity) sourceEntity);
                }
            }
            else
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(getCitizenColonyHandler().getColonyId(), level.dimension());
                return damage <= 1 || colonyView == null || colonyView.getPermissions().hasPermission((PlayerEntity) sourceEntity, Action.HURT_CITIZEN);
            }
        }
        return true;
    }

    @Override
    public void move(final MoverType typeIn, final Vector3d pos)
    {
        //todo someaddons: remove this on the minimum AI rework.
        if (pos.x != 0 || pos.z != 0)
        {
            if (getCitizenData() != null && getCitizenData().isAsleep())
            {
                getCitizenSleepHandler().onWakeUp();
            }
            else if (getPose() == Pose.SLEEPING)
            {
                updatePose(Pose.STANDING);
            }
        }
        super.move(typeIn, pos);
    }

    @Override
    public float getSpeed()
    {
        return (float) Math.min(MAX_SPEED_FACTOR, super.getSpeed());
    }

    private boolean handleDamagePerformed(@NotNull final DamageSource damageSource, final float damage, final Entity sourceEntity)
    {
        float damageInc = Math.min(damage, (getMaxHealth() * 0.2f));

        if (!level.isClientSide)
        {
            performMoveAway(sourceEntity);
        }
        setLastHurtMob(damageSource.getEntity());

        if (!level.isClientSide)
        {
            if (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard && citizenData != null)
            {
                if (citizenJobHandler.getColonyJob() instanceof JobKnight)
                {
                    if (citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(BLOCK_ATTACKS) > 0)
                    {
                        if (getRandom().nextDouble() < citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(BLOCK_ATTACKS))
                        {
                            return false;
                        }
                    }
                }

                if (citizenData.getWorkBuilding() instanceof AbstractBuildingGuards && ((AbstractBuildingGuards) citizenData.getWorkBuilding()).shallRetrieveOnLowHealth()
                      && getHealth() < ((int) getMaxHealth() * 0.2D))
                {
                    damageInc *= 1 - citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(FLEEING_DAMAGE);
                }
            }
        }

        final boolean result = super.hurt(damageSource, damageInc);

        if (result && damageSource.getEntity() instanceof LivingEntity)
        {
            threatTable.addThreat((LivingEntity) damageSource.getEntity(), (int) damageInc);
        }

        if (damageSource.isMagic() || damageSource.isFire())
        {
            return result;
        }

        if (!level.isClientSide)
        {
            citizenItemHandler.updateArmorDamage(damageInc);
            if (citizenData != null)
            {
                getCitizenData().getCitizenHappinessHandler().getModifier("damage").reset();
            }
        }

        return result;
    }

    /**
     * Run away from an attacker
     *
     * @param attacker the attacking Entity
     */
    private void performMoveAway(@Nullable final Entity attacker)
    {
        this.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.avoiding"));

        // Environmental damage
        if (!(attacker instanceof LivingEntity))
        {
            if (moveAwayPath == null || !moveAwayPath.isInProgress())
            {
                moveAwayPath = this.getNavigation().moveAwayFromLivingEntity(this, 5, INITIAL_RUN_SPEED_AVOID);
            }
            return;
        }

        // Makes the avoidance AI take over.
        currentlyFleeing = true;

        if ((getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard))
        {
            // 30 Blocks range
            callForHelp(attacker, 900);
            return;
        }
        else
        {
            callForHelp(attacker, MAX_GUARD_CALL_RANGE);
        }
        if (moveAwayPath == null || !moveAwayPath.isInProgress())
        {
            moveAwayPath = this.getNavigation().moveAwayFromLivingEntity(attacker, 15, INITIAL_RUN_SPEED_AVOID);
        }
    }

    @Override
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {
        if (!(attacker instanceof LivingEntity) || !MineColonies.getConfig().getServer().citizenCallForHelp.get() || callForHelpCooldown != 0)
        {
            return;
        }

        // Don't call for help when a guard gets woken up
        if (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard && citizenJobHandler.getColonyJob(AbstractJobGuard.class).isAsleep())
        {
            return;
        }

        callForHelpCooldown = CALL_HELP_CD;

        List<AbstractEntityCitizen> possibleGuards = new ArrayList<>();

        for (final ICitizenData entry : getCitizenColonyHandler().getColony().getCitizenManager().getCitizens())
        {
            if (entry.getEntity().isPresent())
            {
                // Checking for guard nearby
                if (entry.getJob() instanceof AbstractJobGuard && entry.getId() != citizenData.getId()
                      && BlockPosUtil.getDistanceSquared(entry.getEntity().get().blockPosition(), blockPosition()) < guardHelpRange && entry.getJob().getWorkerAI() != null)
                {
                    final ThreatTable table = ((EntityCitizen) entry.getEntity().get()).getThreatTable();
                    table.addThreat((LivingEntity) attacker, 0);
                    if (((AbstractEntityAIGuard<?, ?>) entry.getJob().getWorkerAI()).canHelp())
                    {
                        possibleGuards.add(entry.getEntity().get());
                    }
                }
            }
        }

        Collections.sort(possibleGuards, Comparator.comparingInt(guard -> (int) blockPosition().distSqr(guard.blockPosition())));

        for (int i = 0; i < possibleGuards.size() && i <= CALL_TO_HELP_AMOUNT; i++)
        {
            ((AbstractEntityAIGuard<?, ?>) possibleGuards.get(i).getCitizenData().getJob().getWorkerAI()).startHelpCitizen((LivingEntity) attacker);
        }
    }

    @Override
    protected void doPush(final Entity entity)
    {
        if (!citizenSleepHandler.isAsleep())
        {
            super.doPush(entity);
        }

        if (!level.isClientSide && entity instanceof AbstractEntityCitizen)
        {
            getCitizenDiseaseHandler().onCollission((AbstractEntityCitizen) entity);
        }
    }

    /**
     * Called when the mob's health reaches 0.
     *
     * @param damageSource the attacking entity.
     */
    @Override
    public void die(@NotNull final DamageSource damageSource)
    {
        currentlyFleeing = false;
        if (citizenColonyHandler.getColony() != null && getCitizenData() != null)
        {
            citizenColonyHandler.getColony().getRaiderManager().onLostCitizen(getCitizenData());

            citizenExperienceHandler.dropExperience();
            this.remove();
            if (!(citizenJobHandler.getColonyJob() instanceof AbstractJobGuard))
            {
                citizenColonyHandler.getColony().getCitizenManager().updateModifier("death");
            }
            triggerDeathAchievement(damageSource, citizenJobHandler.getColonyJob());
            citizenChatHandler.notifyDeath(damageSource);
            if (!(citizenJobHandler.getColonyJob() instanceof AbstractJobGuard))
            {
                citizenColonyHandler.getColony().getCitizenManager().updateCitizenMourn(citizenData, true);
            }

            if (citizenColonyHandler.getColony().isCoordInColony(level, blockPosition()))
            {
                getCitizenColonyHandler().getColony().getGraveManager().createCitizenGrave(level, blockPosition(), citizenData);
            }
            else
            {
                InventoryUtils.dropItemHandler(citizenData.getInventory(), level, (int) getX(), (int) getY(), (int) getZ());
            }

            if (citizenData.getJob() != null)
            {
                citizenData.getJob().onRemoval();
            }
            citizenColonyHandler.getColony().getCitizenManager().removeCivilian(getCitizenData());

            final String deathCause =
              new StringTextComponent(damageSource.getLocalizedDeathMessage(this).getString()).getString().replaceFirst(this.getDisplayName().getString(), "Citizen");
            citizenColonyHandler.getColony().getEventDescriptionManager().addEventDescription(new CitizenDiedEvent(blockPosition(), citizenData.getName(), deathCause));
        }
        super.die(damageSource);
    }

    /**
     * Trigger the corresponding death achievement.
     *
     * @param source The damage source.
     * @param job    The job of the citizen.
     */
    private void triggerDeathAchievement(final DamageSource source, final IJob<?> job)
    {
        // If the job is null, then we can trigger jobless citizen achievement
        if (job != null)
        {
            job.triggerDeathAchievement(source, this);
        }
    }

    @Override
    protected void dropEquipment()
    {
        //Drop actual inventory
        for (int i = 0; i < getInventoryCitizen().getSlots(); i++)
        {
            final ItemStack itemstack = getCitizenData().getInventory().getStackInSlot(i);
            if (ItemStackUtils.getSize(itemstack) > 0)
            {
                citizenItemHandler.entityDropItem(itemstack);
            }
        }
    }

    @Override
    public int getArmorValue()
    {
        if (citizenJobHandler.getColonyJob() instanceof JobKnight)
        {
            return (int) (super.getArmorValue() * (1 + citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(MELEE_ARMOR)));
        }
        else if (citizenJobHandler.getColonyJob() instanceof JobRanger)
        {
            return (int) (super.getArmorValue() * (1 + citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_ARMOR)));
        }
        return super.getArmorValue();
    }

    @Override
    protected void hurtCurrentlyUsedShield(final float damage)
    {
        if (getItemInHand(getUsedItemHand()).getItem() instanceof ShieldItem)
        {
            if (getHealth() > damage * GUARD_BLOCK_DAMAGE)
            {
                final float blockDamage = CombatRules.getDamageAfterAbsorb(damage * GUARD_BLOCK_DAMAGE,
                  (float) this.getArmorValue(),
                  (float) this.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
                setHealth(getHealth() - Math.max(GUARD_BLOCK_DAMAGE, blockDamage));
            }
            citizenItemHandler.damageItemInHand(this.getUsedItemHand(), (int) (damage * GUARD_BLOCK_DAMAGE));
        }
        super.hurtCurrentlyUsedShield(damage);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, final Direction facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            final ICitizenData data = getCitizenData();
            if (data == null)
            {
                return super.getCapability(capability, facing);
            }
            final InventoryCitizen inv = data.getInventory();

            return LazyOptional.of(() -> (T) inv);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof EntityCitizen)
        {
            final EntityCitizen citizen = (EntityCitizen) obj;
            return citizen.citizenColonyHandler.getColonyId() == this.citizenColonyHandler.getColonyId() && citizen.citizenId == this.citizenId;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (citizenColonyHandler == null)
        {
            return super.hashCode();
        }
        return Objects.hash(citizenId, citizenColonyHandler.getColonyId());
    }

    /**
     * Removes the entity from world.
     */
    @Override
    public void remove()
    {
        citizenColonyHandler.onCitizenRemoved();
        super.remove();
    }

    @Override
    public Team getTeam()
    {
        if (level == null || (level.isClientSide && cachedTeamName == null))
        {
            return null;
        }

        if (level.isClientSide)
        {
            return level.getScoreboard().getPlayerTeam(this.cachedTeamName);
        }

        return level.getScoreboard().getPlayerTeam(getScoreboardName());
    }

    @Override
    public void setCustomName(@Nullable final ITextComponent name)
    {
        if (citizenData != null && citizenColonyHandler.getColony() != null && name != null)
        {
            if (!name.getString().contains(citizenData.getName()) && MineColonies.getConfig().getServer().allowGlobalNameChanges.get() >= 0)
            {
                if (MineColonies.getConfig().getServer().allowGlobalNameChanges.get() == 0 &&
                      MineColonies.getConfig().getServer().specialPermGroup.get()
                        .stream()
                        .noneMatch(owner -> owner.equals(citizenColonyHandler.getColony().getPermissions().getOwnerName())))
                {
                    LanguageHandler.sendPlayersMessage(citizenColonyHandler.getColony().getMessagePlayerEntities(), CITIZEN_RENAME_NOT_ALLOWED);
                    return;
                }


                if (citizenColonyHandler.getColony() != null)
                {
                    for (final ICitizenData citizen : citizenColonyHandler.getColony().getCitizenManager().getCitizens())
                    {
                        if (citizen.getName().equals(name.getString()))
                        {
                            LanguageHandler.sendPlayersMessage(citizenColonyHandler.getColony().getMessagePlayerEntities(), CITIZEN_RENAME_SAME);
                            return;
                        }
                    }
                    this.citizenData.setName(name.getString());
                    this.citizenData.markDirty();
                    super.setCustomName(name);
                }
                return;
            }
            super.setCustomName(name);
        }
    }

    @Override
    public void spawnAnim()
    {
        super.spawnAnim();
    }

    @Override
    protected void pickUpItem(final ItemEntity itemEntity)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    /**
     * Returns the home position of each citizen (His house or town hall).
     *
     * @return location
     */
    @NotNull
    @Override
    public BlockPos getRestrictCenter()
    {
        @Nullable final IBuilding homeBuilding = citizenColonyHandler.getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getPosition();
        }
        else if (citizenColonyHandler.getColony() != null && citizenColonyHandler.getColony().getBuildingManager().getTownHall() != null)
        {
            return citizenColonyHandler.getColony().getBuildingManager().getTownHall().getPosition();
        }

        return super.getRestrictCenter();
    }

    /**
     * Prevent riding entities except ours.
     *
     * @param entity entity to ride on
     * @param force  force flag
     * @return true if successful.
     */
    @Override
    public boolean startRiding(final Entity entity, final boolean force)
    {
        if (entity instanceof SittingEntity || force)
        {
            return super.startRiding(entity, force);
        }
        return false;
    }

    @Override
    public boolean isCurrentlyFleeing()
    {
        return currentlyFleeing;
    }

    @Override
    public void setFleeingState(final boolean fleeing)
    {
        currentlyFleeing = fleeing;
    }

    @Nullable
    @Override
    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
    {
        return new ContainerCitizenInventory(id, inv, citizenColonyHandler.getColonyId(), citizenId);
    }

    @Override
    public void setTexture()
    {
        super.setTexture();
    }

    /**
     * Setter for the citizen pose.
     *
     * @param pose the pose to set.
     */
    public void updatePose(final Pose pose)
    {
        setPose(pose);
    }

    @Override
    public void refreshDimensions()
    {
        final EntitySize oldSize = this.dimensions;
        final Pose pose = this.getPose();
        final EntitySize newSize = this.getDimensions(pose);
        final net.minecraftforge.event.entity.EntityEvent.Size sizeEvent =
          net.minecraftforge.event.ForgeEventFactory.getEntitySizeForge(this, pose, newSize, this.getEyeHeight(pose, newSize));
        final EntitySize afterEventSize = sizeEvent.getNewSize();
        this.dimensions = afterEventSize;
        this.eyeHeight = sizeEvent.getNewEyeHeight();
        if (afterEventSize.width < oldSize.width)
        {
            double d0 = (double) afterEventSize.width / 2.0D;
            this.setBoundingBox(new AxisAlignedBB(this.getX() - d0,
              this.getY(),
              this.getZ() - d0,
              this.getX() + d0,
              this.getY() + (double) afterEventSize.height,
              this.getZ() + d0));
        }
        else
        {
            final AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX,
              axisalignedbb.minY,
              axisalignedbb.minZ,
              axisalignedbb.minX + (double) afterEventSize.width,
              axisalignedbb.minY + (double) afterEventSize.height,
              axisalignedbb.minZ + (double) afterEventSize.width));
            if (afterEventSize.width > oldSize.width && !this.firstTick && !this.level.isClientSide)
            {
                final float f = oldSize.width - afterEventSize.width;
                this.move(MoverType.SELF, new Vector3d((double) f, 0.0D, (double) f));
            }
        }
    }

    /**
     * Whether this entity is active and loaded
     *
     * @return
     */
    public boolean isActive()
    {
        return level.isClientSide ? entityStatemachine.getState() == EntityState.ACTIVE_CLIENT : entityStatemachine.getState() == EntityState.ACTIVE_SERVER;
    }

    @Override
    public ThreatTable getThreatTable()
    {
        return threatTable;
    }
}
