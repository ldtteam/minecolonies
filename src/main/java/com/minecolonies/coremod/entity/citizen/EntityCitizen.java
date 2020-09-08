package com.minecolonies.coremod.entity.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Rank;
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
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.research.AdditionModifierResearchEffect;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookAtWithoutMovingGoal;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

import javax.annotation.Nonnull;
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
import static com.minecolonies.api.util.constant.TranslationConstants.CITIZEN_RENAME_NOT_ALLOWED;
import static com.minecolonies.api.util.constant.TranslationConstants.CITIZEN_RENAME_SAME;

/**
 * The Class used to represent the citizen entities.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.ExcessiveClassLength"})
public class EntityCitizen extends AbstractEntityCitizen
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
    private ICitizenColonyHandler  citizenColonyHandler;
    /**
     * The citizen job handler.
     */
    private ICitizenJobHandler     citizenJobHandler;
    /**
     * The citizen sleep handler.
     */
    private ICitizenSleepHandler   citizenSleepHandler;
    /**
     * The citizen sleep handler.
     */
    private ICitizenDiseaseHandler citizenDiseaseHandler;
    /**
     * The path-result of trying to move away
     */
    private PathResult             moveAwayPath;
    /**
     * Indicate if the citizen is mourning or not.
     */
    private boolean          mourning            = false;
    /**
     * Indicates if the citizen is hiding from the rain or not.
     */
    private boolean          hidingFromRain      = false;
    /**
     * IsChild flag
     */
    private boolean          child               = false;
    /**
     * Whether the citizen is currently running away
     */
    private boolean          currentlyFleeing    = false;
    /**
     * Timer for the call for help cd.
     */
    private int              callForHelpCooldown = 0;
    /**
     * Distance walked for consuming food
     */
    private float            lastDistanceWalked  = 0;
    /**
     * Citizen data view.
     */
    private ICitizenDataView citizenDataView;

    /**
     * The location used for requests
     */
    private ILocation location = null;

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

        this.moveController = new MovementHandler(this);
        this.enablePersistence();
        this.setCustomNameVisible(MineColonies.getConfig().getCommon().alwaysRenderNameTag.get());
        initTasks();

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
        if (CompatibilityUtils.getWorldFromCitizen(this).isRemote)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), world.getDimension().getType().getId());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getCitizen(citizenId);
                    if (citizenDataView != null)
                    {
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
        this.goalSelector.addGoal(++priority, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(priority, new EntityAIOpenFenceGate(this, true));
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
    @Override
    public boolean processInteract(final PlayerEntity player, @NotNull final Hand hand)
    {
        final IColonyView iColonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), player.world.getDimension().getType().getId());
        if (iColonyView != null && !iColonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return false;
        }

        if (!ItemStackUtils.isEmpty(player.getHeldItem(hand)) && player.getHeldItem(hand).getItem() instanceof NameTagItem)
        {
            return super.processInteract(player, hand);
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isRemote && iColonyView != null)
        {
            if (player.isSneaking())
            {
                Network.getNetwork().sendToServer(new OpenInventoryMessage(iColonyView, this.getName().getFormattedText(), this.getEntityId()));
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
        return true;
    }

    @Override
    public String getScoreboardName()
    {
        return getName().getFormattedText() + " (" + getCivilianID() + ")";
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
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), world.getDimension().getType().getId());
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
    public void writeAdditional(final CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putInt(TAG_STATUS, citizenStatusHandler.getStatus().ordinal());
        if (citizenColonyHandler.getColony() != null && citizenData != null)
        {
            compound.putInt(TAG_COLONY_ID, citizenColonyHandler.getColony().getID());
            compound.putInt(TAG_CITIZEN, citizenData.getId());
        }
        compound.putBoolean(TAG_MOURNING, mourning);

        citizenDiseaseHandler.write(compound);
    }

    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        super.readAdditional(compound);

        citizenStatusHandler.setStatus(Status.values()[compound.getInt(TAG_STATUS)]);
        citizenColonyHandler.setColonyId(compound.getInt(TAG_COLONY_ID));
        citizenId = compound.getInt(TAG_CITIZEN);

        if (isServerWorld())
        {
            citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
        }

        if (compound.keySet().contains(TAG_MOURNING))
        {
            mourning = compound.getBoolean(TAG_MOURNING);
        }

        citizenDiseaseHandler.read(compound);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons. use this to react to sunlight and start to burn.
     */
    @Override
    public void livingTick()
    {
        super.livingTick();
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
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), world.getDimension().getType().getId());
            if (colonyView != null)
            {
                this.citizenDataView = colonyView.getCitizen(citizenId);
                this.getNavigator().getPathingOptions().setCanUseRails(canPathOnRails());
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
        citizenData.setLastPosition(getPosition());
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
        final ItemStack hat = getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (LocalDate.now(Clock.systemDefaultZone()).getMonth() == Month.DECEMBER
              && MineColonies.getConfig().getCommon().holidayFeatures.get()
              && !(getCitizenJobHandler().getColonyJob() instanceof JobStudent))
        {
            if (hat.isEmpty())
            {
                this.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.santaHat));
            }
        }
        else if (!hat.isEmpty() && hat.getItem() == ModItems.santaHat)
        {
            this.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
        }
        this.setCustomNameVisible(MineColonies.getConfig().getCommon().alwaysRenderNameTag.get());

        if (!citizenColonyHandler.getColony().getStyle().equals(getDataManager().get(DATA_STYLE)))
        {
            getDataManager().set(DATA_STYLE, citizenColonyHandler.getColony().getStyle());
        }
        if (!citizenData.getTextureSuffix().equals(getDataManager().get(DATA_TEXTURE_SUFFIX)))
        {
            getDataManager().set(DATA_TEXTURE_SUFFIX, citizenData.getTextureSuffix());
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
            if (this.getActivePotionEffect(Effects.SLOWNESS) == null)
            {
                this.addPotionEffect(new EffectInstance(Effects.SLOWNESS, TICKS_SECOND * 30));
            }
        }
        else
        {
            this.removePotionEffect(Effects.SLOWNESS);
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
        if (world.isRemote)
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), world.getDimension().getType().getId());
            if (colonyView != null)
            {
                final UnlockAbilityResearchEffect effect = colonyView.getResearchManager().getResearchEffects().getEffect(RAILS, UnlockAbilityResearchEffect.class);
                if (effect != null)
                {
                    return effect.getEffect();
                }
            }
            return false;
        }
        final UnlockAbilityResearchEffect effect =
          getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(RAILS, UnlockAbilityResearchEffect.class);
        if (effect != null)
        {
            return effect.getEffect();
        }
        return false;
    }

    /**
     * Reduces saturation for walking every 25 blocks.
     */
    private void decreaseWalkingSaturation()
    {
        if (distanceWalkedModified - lastDistanceWalked > ACTIONS_EACH_BLOCKS_WALKED)
        {
            lastDistanceWalked = distanceWalkedModified;
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
            double limitDecrease = 0;
            final AdditionModifierResearchEffect satLimitDecrease =
              getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(SATLIMIT, AdditionModifierResearchEffect.class);
            if (satLimitDecrease != null)
            {
                limitDecrease = satLimitDecrease.getEffect();
            }

            double healAmount = 1;
            if (citizenData.getSaturation() >= FULL_SATURATION - limitDecrease)
            {
                healAmount += 1;
            }
            else if (citizenData.getSaturation() < LOW_SATURATION)
            {
                healAmount = 0;
                return;
            }

            final AdditionModifierResearchEffect healEffect =
              getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(REGENERATION, AdditionModifierResearchEffect.class);
            if (healEffect != null)
            {
                healAmount *= (1.0 + healEffect.getEffect());
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
        if (WorldUtil.isDayTime(world))
        {
            SoundUtils.playRandomSound(world, this.getPosition(), citizenData);
        }
    }

    @Override
    public boolean isChild()
    {
        return child;
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        dataManager.register(DATA_COLONY_ID, citizenColonyHandler == null ? 0 : citizenColonyHandler.getColonyId());
        dataManager.register(DATA_CITIZEN_ID, citizenId);
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
        if (citizenJobHandler.getColonyJob() != null && MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get())
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
        this.size = new EntitySize(width, height, false);
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
        this.getDataManager().set(DATA_IS_CHILD, isChild);
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
            SoundUtils.playSoundAtCitizenWith(world, getPosition(), EventType.DANGER, getCitizenData());
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
        return this.getCitizenData() != null && this.getCitizenData().getSaturation() <= AVERAGE_SATURATION && !this.getCitizenData().justAte();
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
            setVisibleStatusIfNone(RAIDED);
            desiredActivity = DesiredActivity.SLEEP;
            return false;
        }

        if (getCitizenColonyHandler().getColony().isMourning() && mourning)
        {
            setVisibleStatusIfNone(MOURNING);
            desiredActivity = DesiredActivity.MOURN;
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
                citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() * 2);
                citizenData.markDirty();
                citizenStatusHandler.setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.sleeping"));
                desiredActivity = DesiredActivity.SLEEP;
                return false;
            }
        }

        if (citizenSleepHandler.isAsleep() && !citizenDiseaseHandler.isSick())
        {
            citizenSleepHandler.onWakeUp();
        }

        // Raining
        if (CompatibilityUtils.getWorldFromCitizen(this).isRaining() && !shouldWorkWhileRaining())
        {
            citizenStatusHandler.setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.waiting"),
              new TranslationTextComponent("com.minecolonies.coremod.status.rainStop"));
            setVisibleStatusIfNone(BAD_WEATHER);
            desiredActivity = DesiredActivity.IDLE;
            return false;
        }

        if (isChild() && getCitizenJobHandler().getColonyJob() instanceof JobPupil && world.getDayTime() % 24000 > NOON)
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
        final UnlockAbilityResearchEffect effect =
          getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(WORKING_IN_RAIN, UnlockAbilityResearchEffect.class);
        if (effect != null)
        {
            return effect.getEffect();
        }

        return MineColonies.getConfig().getCommon().workersAlwaysWorkInRain.get() ||
                 (citizenColonyHandler.getWorkBuilding() != null && citizenColonyHandler.getWorkBuilding().canWorkDuringTheRain());
    }

    /**
     * Returns a value that indicate if the citizen is in mourning.
     *
     * @return indicate if the citizen is mouring
     */
    @Override
    public boolean isMourning()
    {
        return mourning;
    }

    /**
     * Call this to set if the citizen should mourn or not.
     *
     * @param mourning indicate if the citizen should mourn
     */
    @Override
    public void setMourning(final boolean mourning)
    {
        this.mourning = mourning;
    }

    @Override
    public float getRotationYaw()
    {
        return this.rotationYaw;
    }

    @Override
    public float getRotationPitch()
    {
        return this.rotationPitch;
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
    public boolean attackEntityFrom(@NotNull final DamageSource damageSource, final float damage)
    {
        if (handleInWallDamage(damageSource))
        {
            return false;
        }

        final Entity sourceEntity = damageSource.getTrueSource();
        if (handleSourceEntityForDamage(sourceEntity))
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
        if (damageSource.getDamageType().equals(DamageSource.IN_WALL.getDamageType()))
        {
            TeleportHelper.teleportCitizen(this, world, getPosition().add(0, 1, 0));
            return true;
        }

        return damageSource.getDamageType().equals(DamageSource.IN_WALL.getDamageType()) && citizenSleepHandler.isAsleep()
                 || Compatibility.isDynTreePresent() && damageSource.damageType.equals(Compatibility.getDynamicTreeDamage()) || this.isInvulnerable();
    }

    private boolean handleSourceEntityForDamage(final Entity sourceEntity)
    {
        if (sourceEntity instanceof EntityCitizen)
        {
            if (((EntityCitizen) sourceEntity).citizenColonyHandler.getColonyId() == citizenColonyHandler.getColonyId())
            {
                return true;
            }

            final IColony attackerColony = ((EntityCitizen) sourceEntity).citizenColonyHandler.getColony();
            if (attackerColony != null && citizenColonyHandler.getColony() != null)
            {
                final IPermissions permission = attackerColony.getPermissions();
                citizenColonyHandler.getColony().getPermissions().addPlayer(permission.getOwner(), permission.getOwnerName(), Rank.HOSTILE);
            }
        }

        if (sourceEntity instanceof ServerPlayerEntity && getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
        {
            return !IGuardBuilding.checkIfGuardShouldTakeDamage(this, (PlayerEntity) sourceEntity);
        }
        return false;
    }

    @Override
    public void move(final MoverType typeIn, final Vec3d pos)
    {
        //todo someaddons: removse this on the minimum AI rework.
        if (pos.x != 0 || pos.z != 0)
        {
            if (getCitizenData() != null && getCitizenData().isAsleep())
            {
                getCitizenSleepHandler().onWakeUp();
            }
        }
        super.move(typeIn, pos);
    }

    @Override
    public float getAIMoveSpeed()
    {
        return (float) Math.min(MAX_SPEED_FACTOR, super.getAIMoveSpeed());
    }

    private boolean handleDamagePerformed(@NotNull final DamageSource damageSource, final float damage, final Entity sourceEntity)
    {
        float damageInc = Math.min(damage, (getMaxHealth() * 0.2f));

        if (!world.isRemote)
        {
            performMoveAway(sourceEntity);
        }
        setLastAttackedEntity(damageSource.getTrueSource());

        if (!world.isRemote)
        {
            if (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard && citizenData != null)
            {
                if (citizenJobHandler.getColonyJob() instanceof JobKnight)
                {
                    final MultiplierModifierResearchEffect effect =
                      citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffect(BLOCK_ATTACKS, MultiplierModifierResearchEffect.class);
                    if (effect != null)
                    {
                        if (getRandom().nextDouble() < effect.getEffect())
                        {
                            return false;
                        }
                    }
                }

                if (citizenData.getWorkBuilding() instanceof AbstractBuildingGuards && ((AbstractBuildingGuards) citizenData.getWorkBuilding()).shallRetrieveOnLowHealth()
                      && getHealth() < ((int) getMaxHealth() * 0.2D))
                {
                    final MultiplierModifierResearchEffect effect =
                      citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffect(FLEEING_DAMAGE, MultiplierModifierResearchEffect.class);
                    if (effect != null)
                    {
                        damageInc *= 1 - effect.getEffect();
                    }
                }
            }
        }

        final boolean result = super.attackEntityFrom(damageSource, damageInc);

        if (damageSource.isMagicDamage() || damageSource.isFireDamage())
        {
            return result;
        }

        if (!world.isRemote)
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
                moveAwayPath = this.getNavigator().moveAwayFromLivingEntity(this, 5, INITIAL_RUN_SPEED_AVOID);
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
            moveAwayPath = this.getNavigator().moveAwayFromLivingEntity(attacker, 15, INITIAL_RUN_SPEED_AVOID);
        }
    }

    @Override
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {
        if (!(attacker instanceof LivingEntity) || !MineColonies.getConfig().getCommon().citizenCallForHelp.get() || callForHelpCooldown != 0)
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
                      && BlockPosUtil.getDistanceSquared(entry.getEntity().get().getPosition(), getPosition()) < guardHelpRange && entry.getJob().getWorkerAI() != null
                      && ((AbstractEntityAIGuard<?, ?>) entry.getJob().getWorkerAI()).canHelp())
                {
                    possibleGuards.add(entry.getEntity().get());
                }
            }
        }

        Collections.sort(possibleGuards, Comparator.comparingInt(guard -> (int) getPosition().distanceSq(guard.getPosition())));

        for (int i = 0; i < possibleGuards.size() && i <= CALL_TO_HELP_AMOUNT; i++)
        {
            ((AbstractEntityAIGuard<?, ?>) possibleGuards.get(i).getCitizenData().getJob().getWorkerAI()).startHelpCitizen(this, (LivingEntity) attacker);
        }
    }

    /**
     * Called when the mob's health reaches 0.
     *
     * @param damageSource the attacking entity.
     */
    @Override
    public void onDeath(@NotNull final DamageSource damageSource)
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
            if (!(citizenJobHandler.getColonyJob() instanceof AbstractJobGuard)
                  && (damageSource != DamageSource.IN_WALL))
            {
                citizenColonyHandler.getColony().setNeedToMourn(true, citizenData.getName());
            }
            if (citizenData.getJob() != null)
            {
                citizenData.getJob().onRemoval();
            }
            citizenColonyHandler.getColony().getCitizenManager().removeCivilian(getCitizenData());
            InventoryUtils.dropItemHandler(citizenData.getInventory(), world, (int) posX, (int) posY, (int) posZ);
        }
        super.onDeath(damageSource);
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
    protected void dropInventory()
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
    public int getTotalArmorValue()
    {
        if (citizenJobHandler.getColonyJob() instanceof JobKnight)
        {
            final MultiplierModifierResearchEffect
              effect = citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffect(MELEE_ARMOR, MultiplierModifierResearchEffect.class);
            if (effect != null)
            {
                return (int) (super.getTotalArmorValue() * (1 + effect.getEffect()));
            }
        }
        else if (citizenJobHandler.getColonyJob() instanceof JobRanger)
        {
            final MultiplierModifierResearchEffect
              effect = citizenColonyHandler.getColony().getResearchManager().getResearchEffects().getEffect(ARCHER_ARMOR, MultiplierModifierResearchEffect.class);
            if (effect != null)
            {
                return (int) (super.getTotalArmorValue() * (1 + effect.getEffect()));
            }
        }
        return super.getTotalArmorValue();
    }

    @Override
    protected void damageShield(final float damage)
    {
        if (getHeldItem(getActiveHand()).getItem() instanceof ShieldItem)
        {
            if (getHealth() > damage * GUARD_BLOCK_DAMAGE)
            {
                final float blockDamage = CombatRules.getDamageAfterAbsorb(damage * GUARD_BLOCK_DAMAGE,
                  (float) this.getTotalArmorValue(),
                  (float) this.getAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getValue());
                setHealth(getHealth() - Math.max(GUARD_BLOCK_DAMAGE, blockDamage));
            }
            citizenItemHandler.damageItemInHand(this.getActiveHand(), (int) (damage * GUARD_BLOCK_DAMAGE));
        }
        super.damageShield(damage);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, final Direction facing)
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
        if (getCitizenColonyHandler().getColony() != null)
        {
            return getCitizenColonyHandler().getColony().getTeam();
        }
        else
        {
            // Note: This function has always returned null on the client-side when the colony is not available yet.
            return null;
        }
    }

    @Override
    public void setCustomName(@Nullable final ITextComponent name)
    {
        if (citizenData != null && citizenColonyHandler.getColony() != null && name != null)
        {
            if (!name.getFormattedText().contains(citizenData.getName()) && MineColonies.getConfig().getCommon().allowGlobalNameChanges.get() >= 0)
            {
                if (MineColonies.getConfig().getCommon().allowGlobalNameChanges.get() == 0 &&
                      MineColonies.getConfig().getCommon().specialPermGroup.get()
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
                        if (citizen.getName().equals(name.getFormattedText()))
                        {
                            LanguageHandler.sendPlayersMessage(citizenColonyHandler.getColony().getMessagePlayerEntities(), CITIZEN_RENAME_SAME);
                            return;
                        }
                    }
                    this.citizenData.setName(name.getFormattedText());
                    this.citizenData.markDirty();
                    super.setCustomName(name);
                }
                return;
            }
            super.setCustomName(name);
        }
    }

    @Override
    public void spawnExplosionParticle()
    {
        super.spawnExplosionParticle();
    }

    @Override
    protected void updateEquipmentIfNeeded(final ItemEntity itemEntity)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public boolean preventDespawn()
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
    public BlockPos getHomePosition()
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

        return super.getHomePosition();
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
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(citizenColonyHandler.getColonyId());
        buffer.writeVarInt(citizenId);
        return new ContainerCitizenInventory(id, inv, buffer);
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
}
