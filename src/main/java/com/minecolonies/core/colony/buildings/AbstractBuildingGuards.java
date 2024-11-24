package com.minecolonies.core.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.settings.*;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.core.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobRandomPos;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.items.ItemBannerRallyGuards;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.ARCHER_USE_ARROWS;
import static com.minecolonies.api.research.util.ResearchConstants.TELESCOPE;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_BUILDING_NAME;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_RALLYING_POINT_OUT_OF_RANGE;
import static com.minecolonies.core.util.ServerUtils.getPlayerFromUUID;

/**
 * Abstract class for Guard huts.
 */
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S1448"})
public abstract class AbstractBuildingGuards extends AbstractBuilding implements IGuardBuilding
{
    /**
     * Settings.
     */
    public static final ISettingKey<BoolSetting>       RETREAT      =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "retreat"));
    public static final ISettingKey<BoolSetting>            HIRE_TRAINEE =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "hiretrainee"));
    public static final ISettingKey<GuardPatrolModeSetting> PATROL_MODE =
      new SettingKey<>(GuardPatrolModeSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "patrolmode"));
    public static final ISettingKey<GuardFollowModeSetting> FOLLOW_MODE =
      new SettingKey<>(GuardFollowModeSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "followmode"));
    public static final ISettingKey<GuardTaskSetting>       GUARD_TASK  =
      new SettingKey<>(GuardTaskSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "guardtask"));


    //manual patroll. retreat, hire from training

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_JOB            = "guardType";
    private static final String NBT_PATROL_TARGETS = "patrol targets";
    private static final String NBT_TARGET         = "target";
    private static final String NBT_GUARD          = "guard";
    private static final String NBT_MINE_POS       = "minePos";
    private static final String NBT_PLAYER_UUID    = "playeruuid";

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
     * The UUID of the player the guard has been set to follow.
     */
    private UUID followPlayerUUID;

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

        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.bow.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack) && ItemStackUtils.doesItemServeAsWeapon(itemStack), new Tuple<>(1, true));

        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ArmorItem
                                 && ((ArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET, new Tuple<>(1, true));

        keepX.put(itemStack -> {
            if (ItemStackUtils.isEmpty(itemStack) || !(itemStack.getItem() instanceof ArrowItem))
            {
                return false;
            }

            return getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0;
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
        if (getAllAssignedCitizen() != null)
        {
            for (final ICitizenData optCitizen : getAllAssignedCitizen())
            {
                if (optCitizen.getEntity().isPresent())
                {
                    final AttributeModifier healthModBuildingHP = new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, getBonusHealth(), AttributeModifier.Operation.ADDITION);
                    AttributeModifierUtils.addHealthModifier(optCitizen.getEntity().get(), healthModBuildingHP);
                }
            }
        }

        super.onUpgradeComplete(newLevel);
    }

    //// ---- NBT Overrides ---- \\\\

    //// ---- Overrides ---- \\\\

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        final ListTag wayPointTagList = compound.getList(NBT_PATROL_TARGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.size(); ++i)
        {
            final CompoundTag blockAtPos = wayPointTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(blockAtPos, NBT_TARGET);
            patrolTargets.add(pos);
        }

        guardPos = NbtUtils.readBlockPos(compound.getCompound(NBT_GUARD));
        if (compound.contains(NBT_MINE_POS))
        {
            minePos = NbtUtils.readBlockPos(compound.getCompound(NBT_MINE_POS));
        }

        if (compound.contains(NBT_PLAYER_UUID))
        {
            followPlayerUUID = compound.getUUID(NBT_PLAYER_UUID);
        }

    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        @NotNull final ListTag wayPointTagList = new ListTag();
        for (@NotNull final BlockPos pos : patrolTargets)
        {
            @NotNull final CompoundTag wayPointCompound = new CompoundTag();
            BlockPosUtil.write(wayPointCompound, NBT_TARGET, pos);

            wayPointTagList.add(wayPointCompound);
        }
        compound.put(NBT_PATROL_TARGETS, wayPointTagList);
        compound.put(NBT_GUARD, NbtUtils.writeBlockPos(guardPos));
        if (minePos != null)
        {
            compound.put(NBT_MINE_POS, NbtUtils.writeBlockPos(minePos));
        }

        if (followPlayerUUID != null)
        {
            compound.putUUID(NBT_PLAYER_UUID, followPlayerUUID);
        }

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf, final boolean fullSync)
    {
        super.serializeToView(buf, fullSync);
        buf.writeInt(patrolTargets.size());

        for (final BlockPos pos : patrolTargets)
        {
            buf.writeBlockPos(pos);
        }

        buf.writeInt(this.getAllAssignedCitizen().size());
        for (final ICitizenData citizen : this.getAllAssignedCitizen())
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
        return getModule(BuildingModules.GUARD_SETTINGS).getSetting(GUARD_TASK).getValue();
    }

    @Override
    @Nullable
    public Player getPlayerToFollowOrRally()
    {
        if (rallyLocation != null && rallyLocation instanceof EntityLocation)
        {
            return ((EntityLocation) rallyLocation).getPlayerEntity();
        }
        else if (getTask().equals(GuardTaskSetting.FOLLOW))
        {
            return getPlayerFromUUID(followPlayerUUID, this.colony.getWorld());
        }

        return null;
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
    public void onColonyTick(final IColony colony)
    {
        super.onColonyTick(colony);

        if (patrolTimer > 0 && getSetting(GUARD_TASK).getValue().equals(GuardTaskSetting.PATROL))
        {
            patrolTimer--;
            if (patrolTimer <= 0 && !getAllAssignedCitizen().isEmpty())
            {
                // Next patrol point
                startPatrolNext();
            }
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

        if (getAllAssignedCitizen().size() <= arrivedAtPatrol.size() || patrolTimer <= 0)
        {
            // Next patrol point
            startPatrolNext();
        }
    }

    /**
     * Set the patroll timer.
     * @param timer the timer to set.
     */
    public void setPatrolTimer(final int timer)
    {
        this.patrolTimer = timer;
    }

    /**
     * Starts the patrol to the next point
     */
    private void startPatrolNext()
    {
        getNextPatrolTarget(true);
        patrolTimer = 5;

        for (final ICitizenData curguard : getAllAssignedCitizen())
        {
            if (curguard.getEntity().isPresent())
            {
                if (curguard.getJob() instanceof AbstractJobGuard guardEntity)
                {
                    ((AbstractEntityAIGuard<?, ?>) guardEntity.getWorkerAI()).setNextPatrolTargetAndMove(lastPatrolPoint);
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
            lastPatrolPoint = getAllAssignedCitizen().iterator().next().getLastPosition();
            return lastPatrolPoint;
        }

        if (!getSetting(PATROL_MODE).getValue().equals(GuardPatrolModeSetting.MANUAL) || patrolTargets == null || patrolTargets.isEmpty())
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
                pos = colony.getBuildingManager().getRandomBuilding(b -> b.getBuildingLevel() >= 1);
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

    @Override
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
    public boolean shallRetrieveOnLowHealth()
    {
        return getSetting(RETREAT).getValue();
    }

    @Override
    public boolean shallPatrolManually()
    {
        return getSetting(PATROL_MODE).getValue().equals(GuardPatrolModeSetting.MANUAL);
    }

    @Override
    public boolean isTightGrouping()
    {
        return getSetting(FOLLOW_MODE).getValue().equals(GuardFollowModeSetting.TIGHT);
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
        Player followPlayer = getPlayerFromUUID(followPlayerUUID, this.colony.getWorld());
        if (getSetting(GUARD_TASK).getValue().equals(GuardTaskSetting.FOLLOW) && followPlayer != null && followPlayer.level.dimension() == this.colony.getDimension())
        {
            return followPlayer.blockPosition();
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
            if (getColony().getResearchManager().getResearchEffects().getEffectStrength(TELESCOPE) <= 0 || BlockPosUtil.getDistance2D(rallyLocation.getInDimensionLocation(), colony.getCenter()) > 500)
            {
                outOfRange = true;
            }
        }

        if (rallyLocation instanceof EntityLocation)
        {
            final Player player = ((EntityLocation) rallyLocation).getPlayerEntity();
            if (player == null)
            {
                setRallyLocation(null);
                return null;
            }

            if (outOfRange)
            {
                MessageUtils.format(WARNING_RALLYING_POINT_OUT_OF_RANGE).sendTo(player);
                setRallyLocation(null);
                return null;
            }

            final int size = player.getInventory().getContainerSize();
            for (int i = 0; i < size; i++)
            {
                final ItemStack stack = player.getInventory().getItem(i);
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
        else if (rallyLocation instanceof StaticLocation)
        {
            if (outOfRange)
            {
                MessageUtils.format(WARNING_RALLYING_POINT_OUT_OF_RANGE).sendTo(colony.getImportantMessageEntityPlayers());
                setRallyLocation(null);
                return null;
            }
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

        for (final ICitizenData iCitizenData : getAllAssignedCitizen())
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
    public void setPlayerToFollow(final Player player)
    {
        this.followPlayerUUID = player.getUUID();

        for (final ICitizenData iCitizenData : getAllAssignedCitizen())
        {
            final AbstractJobGuard<?> job = iCitizenData.getJob(AbstractJobGuard.class);
            if (job != null && job.getWorkerAI() != null)
            {
                job.getWorkerAI().registerTarget(new AIOneTimeEventTarget(AIWorkerState.PREPARING));
            }
        }
    }

    /**
     * Bonus guard hp per bulding level
     *
     * @return the bonus health.
     */
    public int getBonusHealth()
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

    /**
     * The client view for the Guard building.
     */
    public static class View extends AbstractBuildingView
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
        public void deserialize(@NotNull final FriendlyByteBuf buf)
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
