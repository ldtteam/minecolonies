package com.minecolonies.coremod.colony.buildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.modules.settings.*;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobRandomPos;
import com.minecolonies.coremod.items.ItemBannerRallyGuards;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.ARCHER_USE_ARROWS;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Abstract class for Guard huts.
 */
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S1448"})
public abstract class AbstractBuildingGuards extends AbstractBuildingWorker implements IGuardBuilding
{
    /**
     * Settings.
     */
    public static final ISettingKey<GuardJobSetting>   JOB          =
      new SettingKey<>(GuardJobSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "guardjob"));
    public static final ISettingKey<BoolSetting>       RETREAT      =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "retreat"));
    public static final ISettingKey<BoolSetting>       HIRE_TRAINEE =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "hiretrainee"));
    public static final ISettingKey<PatrolModeSetting> PATROL_MODE  =
      new SettingKey<>(PatrolModeSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "patrolmode"));
    public static final ISettingKey<FollowModeSetting> FOLLOW_MODE  =
      new SettingKey<>(FollowModeSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "followmode"));
    public static final ISettingKey<GuardTaskSetting>  GUARD_TASK   =
      new SettingKey<>(GuardTaskSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "guardtask"));


    //manual patroll. retreat, hire from training

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_JOB            = "guardType";
    private static final String NBT_PATROL_TARGETS = "patrol targets";
    private static final String NBT_TARGET         = "target";
    private static final String NBT_GUARD          = "guard";
    private static final String NBT_MINE_POS       = "minePos";

    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    ////// --------------------------- GuardJob Enum --------------------------- \\\\\\

    /**
     * Base patrol range
     */
    private static final int PATROL_BASE_DIST = 50;

    /**
     * The Bonus Health for each building level
     */
    private static final int BONUS_HEALTH_PER_LEVEL = 2;

    /**
     * Vision range per building level.
     */
    private static final int VISION_RANGE_PER_LEVEL = 3;

    /**
     * Base Vision range per building level.
     */
    private static final int BASE_VISION_RANGE = 15;

    /**
     * The position at which the guard should guard at.
     */
    private BlockPos guardPos = this.getID();

    /**
     * The list of manual patrol targets.
     */
    protected List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * The player the guard has been set to follow.
     */
    private PlayerEntity followPlayer;

    /**
     * The location the guard has been set to rally to.
     */
    private ILocation rallyLocation;

    /**
     * A temporary next patrol point, which gets consumed and used once
     */
    protected BlockPos tempNextPatrolPoint = null;

    /**
     * Pathing future for the next patrol target.
     */
    private PathResult pathResult;

    /**
     * The location of the assigned mine
     */
    private BlockPos minePos;

    /**
     * List of hostiles.
     */
    public static final String HOSTILE_LIST = "hostiles";

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingGuards(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.BOW, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack) && ItemStackUtils.doesItemServeAsWeapon(itemStack), new Tuple<>(1, true));

        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.FEET, new Tuple<>(1, true));

        keepX.put(itemStack -> {
            if (ItemStackUtils.isEmpty(itemStack) || !(itemStack.getItem() instanceof ArrowItem))
            {
                return false;
            }

            return getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0
                     && getGuardType() == ModGuardTypes.ranger;
        }, new Tuple<>(128, true));
    }

    //// ---- NBT Overrides ---- \\\\

    /**
     * We use this to set possible health multipliers and give achievements.
     *
     * @param newLevel The new level.
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        getGuardType();

        if (getAssignedEntities() != null)
        {
            for (final Optional<AbstractEntityCitizen> optCitizen : getAssignedEntities())
            {
                if (optCitizen.isPresent())
                {
                    final AttributeModifier healthModBuildingHP = new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, getBonusHealth(), AttributeModifier.Operation.ADDITION);
                    AttributeModifierUtils.addHealthModifier(optCitizen.get(), healthModBuildingHP);
                }
            }
        }

        super.onUpgradeComplete(newLevel);
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        // Only change HP values if assign successful
        if (super.assignCitizen(citizen) && citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
            if (optCitizen.isPresent())
            {
                final AbstractEntityCitizen citizenEntity = optCitizen.get();
                AttributeModifierUtils.addHealthModifier(citizenEntity,
                  new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, getBonusHealth(), AttributeModifier.Operation.ADDITION));
                AttributeModifierUtils.addHealthModifier(citizenEntity,
                  new AttributeModifier(GUARD_HEALTH_MOD_CONFIG_NAME,
                    MineColonies.getConfig().getServer().guardHealthMult.get() - 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            // Set new home, since guards are housed at their workerbuilding.
            final IBuilding building = citizen.getHomeBuilding();
            if (building != null && !building.getID().equals(this.getID()))
            {
                building.removeCitizen(citizen);
            }
            citizen.setHomeBuilding(this);
            // Start timeout to not be stuck with an old patrol target
            patrolTimer = 5;

            return true;
        }
        return false;
    }

    //// ---- NBT Overrides ---- \\\\

    //// ---- Overrides ---- \\\\

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.contains(NBT_JOB))
        {
            getSetting(JOB).set(compound.getString(NBT_JOB));
        }

        final ListNBT wayPointTagList = compound.getList(NBT_PATROL_TARGETS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.size(); ++i)
        {
            final CompoundNBT blockAtPos = wayPointTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(blockAtPos, NBT_TARGET);
            patrolTargets.add(pos);
        }

        guardPos = NBTUtil.readBlockPos(compound.getCompound(NBT_GUARD));
        if (compound.contains(NBT_MINE_POS))
        {
            minePos = NBTUtil.readBlockPos(compound.getCompound(NBT_MINE_POS));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT wayPointTagList = new ListNBT();
        for (@NotNull final BlockPos pos : patrolTargets)
        {
            @NotNull final CompoundNBT wayPointCompound = new CompoundNBT();
            BlockPosUtil.write(wayPointCompound, NBT_TARGET, pos);

            wayPointTagList.add(wayPointCompound);
        }
        compound.put(NBT_PATROL_TARGETS, wayPointTagList);
        compound.put(NBT_GUARD, NBTUtil.writeBlockPos(guardPos));
        if (minePos != null)
        {
            compound.put(NBT_MINE_POS, NBTUtil.writeBlockPos(minePos));
        }

        return compound;
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
            if (optCitizen.isPresent())
            {
                AttributeModifierUtils.removeAllHealthModifiers(optCitizen.get());
                optCitizen.get().setItemSlot(EquipmentSlotType.CHEST, ItemStackUtils.EMPTY);
                optCitizen.get().setItemSlot(EquipmentSlotType.FEET, ItemStackUtils.EMPTY);
                optCitizen.get().setItemSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
                optCitizen.get().setItemSlot(EquipmentSlotType.LEGS, ItemStackUtils.EMPTY);
                optCitizen.get().setItemSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
                optCitizen.get().setItemSlot(EquipmentSlotType.OFFHAND, ItemStackUtils.EMPTY);
            }
            citizen.setHomeBuilding(null);
        }
        super.removeCitizen(citizen);
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(patrolTargets.size());

        for (final BlockPos pos : patrolTargets)
        {
            buf.writeBlockPos(pos);
        }

        buf.writeInt(this.getAssignedCitizen().size());
        for (final ICitizenData citizen : this.getAssignedCitizen())
        {
            buf.writeInt(citizen.getId());
        }

        if (minePos != null)
        {
            buf.writeBoolean(true);
            buf.writeBlockPos(minePos);
        }
        else
        {
            buf.writeBoolean(false);
        }
    }

    @Override
    public String getTask()
    {
        return this.getSetting(GUARD_TASK).getValue();
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerToFollowOrRally()
    {
        return rallyLocation != null && rallyLocation instanceof EntityLocation ? ((EntityLocation) rallyLocation).getPlayerEntity() : followPlayer;
    }

    /**
     * The guards which arrived at the patrol positions
     */
    private final Set<AbstractEntityCitizen> arrivedAtPatrol = new HashSet<>();

    /**
     * The last patrol position
     */
    private BlockPos lastPatrolPoint;

    /**
     * The patrol waiting for others timeout
     */
    private int patrolTimer = 0;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        boolean hiredFromTraining = false;

        // If we have no active worker, attempt to grab one from the appropriate trainer
        if (getSetting(HIRE_TRAINEE).getValue() && !isFull() && ((getBuildingLevel() > 0 && isBuilt()))
              && (this.getHiringMode() == HiringMode.DEFAULT && !this.getColony().isManualHiring() || this.getHiringMode() == HiringMode.AUTO))
        {
            ICitizenData trainingCitizen = null;
            int maxSkill = 0;

            for (ICitizenData trainee : colony.getCitizenManager().getCitizens())
            {
                if ((this.getGuardType() == ModGuardTypes.ranger && trainee.getJob() instanceof JobArcherTraining)
                      || (this.getGuardType() == ModGuardTypes.knight && trainee.getJob() instanceof JobCombatTraining)
                           && trainee.getCitizenSkillHandler().getLevel(getGuardType().getPrimarySkill()) > maxSkill)
                {
                    maxSkill = trainee.getCitizenSkillHandler().getLevel(getGuardType().getPrimarySkill());
                    trainingCitizen = trainee;
                }
            }

            if (trainingCitizen != null)
            {
                hiredFromTraining = true;
                assignCitizen(trainingCitizen);
            }
        }

        //If we hired, we may have more than one to hire, so let's skip the superclass until next time. 
        if (!hiredFromTraining)
        {
            super.onColonyTick(colony);
        }

        if (patrolTimer > 0 && getSetting(GUARD_TASK).getValue().equals(GuardTaskSetting.PATROL))
        {
            patrolTimer--;
            if (patrolTimer <= 0 && !getAssignedCitizen().isEmpty())
            {
                // Next patrol point
                startPatrolNext();
            }
        }
    }

    /**
     * Called when the job setting changes.
     */
    public void onJobChange()
    {
        for (final ICitizenData citizen : getAssignedCitizen())
        {
            cancelAllRequestsOfCitizen(citizen);
            citizen.setJob(createJob(citizen));
        }
    }

    @Override
    public boolean requiresManualTarget()
    {
        return false;
    }

    @Override
    public void arrivedAtPatrolPoint(final AbstractEntityCitizen guard)
    {
        // Start waiting timer for other guards
        if (arrivedAtPatrol.isEmpty())
        {
            patrolTimer = 1;
        }

        arrivedAtPatrol.add(guard);

        if (getAssignedCitizen().size() <= arrivedAtPatrol.size() || patrolTimer <= 0)
        {
            // Next patrol point
            startPatrolNext();
        }
    }

    /**
     * Starts the patrol to the next point
     */
    private void startPatrolNext()
    {
        getNextPatrolTarget(true);
        patrolTimer = 5;

        for (final ICitizenData curguard : getAssignedCitizen())
        {
            if (curguard.getEntity().isPresent())
            {
                if (curguard.getEntity().get().getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
                {
                    ((AbstractEntityAIGuard<?, ?>) curguard.getEntity().get().getCitizenJobHandler().getColonyJob().getWorkerAI()).setNextPatrolTarget(lastPatrolPoint);
                }
            }
        }
        arrivedAtPatrol.clear();
    }

    @Override
    @Nullable
    public BlockPos getNextPatrolTarget(final boolean newTarget)
    {
        if (!newTarget && lastPatrolPoint != null)
        {
            return lastPatrolPoint;
        }

        if (tempNextPatrolPoint != null)
        {
            lastPatrolPoint = tempNextPatrolPoint;
            tempNextPatrolPoint = null;
            return lastPatrolPoint;
        }

        if (lastPatrolPoint == null)
        {
            lastPatrolPoint = getAssignedCitizen().get(0).getLastPosition();
            return lastPatrolPoint;
        }

        if (!getSetting(PATROL_MODE).getValue().equals(PatrolModeSetting.MANUAL) || patrolTargets == null || patrolTargets.isEmpty())
        {
            BlockPos pos = null;
            if (this.pathResult != null)
            {
                if (this.pathResult.isDone())
                {
                    if (pathResult.getPath() != null)
                    {
                        pos = this.pathResult.getPath().getTarget();
                    }
                    this.pathResult = null;
                }
            }
            else if (colony.getWorld().random.nextBoolean())
            {
                final PathJobRandomPos job = new PathJobRandomPos(colony.getWorld(), lastPatrolPoint, 20, 40, null);
                this.pathResult = job.getResult();
                Pathfinding.enqueue(job);
            }
            else
            {
                pos = colony.getBuildingManager().getRandomBuilding(b -> true);
            }

            if (pos != null)
            {
                if (BlockPosUtil.getDistance2D(pos, getPosition()) > getPatrolDistance())
                {
                    lastPatrolPoint = getPosition();
                    return lastPatrolPoint;
                }
                lastPatrolPoint = pos;
            }
            return lastPatrolPoint;
        }

        if (patrolTargets.contains(lastPatrolPoint))
        {
            int index = patrolTargets.indexOf(lastPatrolPoint) + 1;

            if (index >= patrolTargets.size())
            {
                index = 0;
            }

            lastPatrolPoint = patrolTargets.get(index);
            return lastPatrolPoint;
        }
        lastPatrolPoint = patrolTargets.get(0);
        return lastPatrolPoint;
    }

    @Override
    public int getPatrolDistance()
    {
        return PATROL_BASE_DIST + this.getBuildingLevel() * PATROL_DISTANCE;
    }

    /**
     * Sets a one time consumed temporary next position to patrol towards
     *
     * @param pos Position to set
     */
    public void setTempNextPatrolPoint(final BlockPos pos)
    {
        tempNextPatrolPoint = pos;
    }

    /**
     * Return the position of the mine to guard
     *
     * @return the position of the mine
     */
    public BlockPos getMinePos()
    {
        return minePos;
    }

    /**
     * Set the position of the mine the guard is patrolling Check whether the given position is actually a mine
     *
     * @param pos the position of the mine
     */
    public void setMinePos(BlockPos pos)
    {
        if (pos == null)
        {
            this.minePos = null;
        }
        else if (colony.getBuildingManager().getBuilding(pos) instanceof BuildingMiner)
        {
            this.minePos = pos;
        }
    }

    @Override
    public GuardType getGuardType()
    {
        return getSetting(JOB).getGuardType();
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return getGuardType().getGuardJobProducer().apply(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return getGuardType().getJobTranslationKey();
    }

    @Override
    public boolean shallRetrieveOnLowHealth()
    {
        return getSetting(RETREAT).getValue();
    }

    @Override
    public boolean shallPatrolManually()
    {
        return getSetting(PATROL_MODE).getValue().equals(PatrolModeSetting.MANUAL);
    }

    @Override
    public boolean isTightGrouping()
    {
        return getSetting(FOLLOW_MODE).getValue().equals(FollowModeSetting.TIGHT);
    }

    @Override
    public BlockPos getGuardPos()
    {
        return guardPos;
    }

    @Override
    public void setGuardPos(final BlockPos guardPos)
    {
        this.guardPos = guardPos;
    }

    @Override
    public BlockPos getPositionToFollow()
    {
        if (getSetting(GUARD_TASK).getValue().equals(GuardTaskSetting.FOLLOW) && followPlayer != null)
        {
            return new BlockPos(followPlayer.position());
        }

        return this.getPosition();
    }

    @Override
    @Nullable
    public ILocation getRallyLocation()
    {
        if (rallyLocation == null)
        {
            return null;
        }

        boolean outOfRange = false;
        final IColony colonyAtPosition = IColonyManager.getInstance().getColonyByPosFromDim(rallyLocation.getDimension(), rallyLocation.getInDimensionLocation());
        if (colonyAtPosition == null || colonyAtPosition.getID() != colony.getID())
        {
            outOfRange = true;
        }

        if (rallyLocation instanceof EntityLocation)
        {
            final PlayerEntity player = ((EntityLocation) rallyLocation).getPlayerEntity();
            if (player == null)
            {
                setRallyLocation(null);
                return null;
            }

            if (outOfRange)
            {
                LanguageHandler.sendPlayerMessage(player, "item.minecolonies.banner_rally_guards.outofrange");
                setRallyLocation(null);
                return null;
            }

            final int size = player.inventory.getContainerSize();
            for (int i = 0; i < size; i++)
            {
                final ItemStack stack = player.inventory.getItem(i);
                if (stack.getItem() instanceof ItemBannerRallyGuards)
                {
                    if (((ItemBannerRallyGuards) (stack.getItem())).isActiveForGuardTower(stack, this))
                    {
                        return rallyLocation;
                    }
                }
            }
            // Note: We do not reset the rallyLocation here.
            // So, if the player doesn't properly deactivate the banner, this will cause relatively minor lag.
            // But, in exchange, the player does not have to reactivate the banner so often, and it also works
            // if the user moves the banner around in the inventory.
            return null;
        }

        return rallyLocation;
    }

    @Override
    public void setRallyLocation(final ILocation location)
    {
        boolean reduceSaturation = false;
        if (rallyLocation != null && location == null)
        {
            reduceSaturation = true;
        }

        rallyLocation = location;

        for (final ICitizenData iCitizenData : getAssignedCitizen())
        {
            if (reduceSaturation && iCitizenData.getSaturation() < LOW_SATURATION)
            {
                // In addition to the scaled saturation reduction during rallying, stopping a rally
                // will - if only LOW_SATURATION is left - set the saturation level to 0.
                iCitizenData.decreaseSaturation(LOW_SATURATION);
            }
        }
    }

    @Override
    public void setPlayerToFollow(final PlayerEntity player)
    {
        this.followPlayer = player;

        for (final ICitizenData iCitizenData : getAssignedCitizen())
        {
            final AbstractJobGuard<?> job = iCitizenData.getJob(AbstractJobGuard.class);
            if (job != null && job.getWorkerAI() != null)
            {
                job.getWorkerAI().registerTarget(new AIOneTimeEventTarget(AIWorkerState.DECIDE));
            }
        }
    }

    /**
     * Bonus guard hp per bulding level
     *
     * @return the bonus health.
     */
    protected int getBonusHealth()
    {
        return getBuildingLevel() * BONUS_HEALTH_PER_LEVEL;
    }

    /**
     * Adds new patrolTargets.
     *
     * @param target the target to add
     */
    @Override
    public void addPatrolTargets(final BlockPos target)
    {
        this.patrolTargets.add(target);
        this.markDirty();
    }

    /**
     * Resets the patrolTargets list.
     */
    @Override
    public void resetPatrolTargets()
    {
        this.patrolTargets = new ArrayList<>();
        this.markDirty();
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return getGuardType().getPrimarySkill();
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return getGuardType().getSecondarySkill();
    }

    /**
     * Get the Vision bonus range for the building level
     *
     * @return an integer for the additional range.
     */
    @Override
    public int getBonusVision()
    {
        return BASE_VISION_RANGE + getBuildingLevel() * VISION_RANGE_PER_LEVEL;
    }

    /**
     * Populates the mobs list from the ForgeRegistries.
     */
    @Override
    public void calculateMobs()
    {

    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }

    /**
     * The client view for the Guard building.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * The list of manual patrol targets.
         */
        private List<BlockPos> patrolTargets = new ArrayList<>();

        @NotNull
        private final List<Integer> guards = new ArrayList<>();

        /**
         * Location of the assigned mine
         */
        private BlockPos minePos;

        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return a BlockOut window.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, getSchematicName());
        }

        /**
         * Getter for the list of residents.
         *
         * @return an unmodifiable list.
         */
        @NotNull
        public List<Integer> getGuards()
        {
            return Collections.unmodifiableList(guards);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);

            final int targetSize = buf.readInt();
            patrolTargets = new ArrayList<>();

            for (int i = 0; i < targetSize; i++)
            {
                patrolTargets.add(buf.readBlockPos());
            }

            guards.clear();
            final int numResidents = buf.readInt();
            for (int i = 0; i < numResidents; ++i)
            {
                guards.add(buf.readInt());
            }

            if (buf.readBoolean())
            {
                minePos = buf.readBlockPos();
            }
            else
            {
                minePos = null;
            }
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return getGuardType().getPrimarySkill();
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return getGuardType().getSecondarySkill();
        }

        /**
         * Get the current guard type.
         *
         * @return the type.
         */
        public GuardType getGuardType()
        {
            return IGuardTypeRegistry.getInstance().getValue(new ResourceLocation(getModuleView(SettingsModuleView.class).getSetting(JOB).getValue()));
        }

        public List<BlockPos> getPatrolTargets()
        {
            return new ArrayList<>(patrolTargets);
        }

        /**
         * Return the position of the mine the guard is patrolling
         *
         * @return the position of the mine
         */
        public BlockPos getMinePos() { return minePos; }

        /**
         * Set the position of the mine the guard is patrolling
         *
         * @param pos the position of the mine
         */
        public void setMinePos(BlockPos pos) { this.minePos = pos; }
    }
}
