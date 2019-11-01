package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutGuardTower;
import com.minecolonies.coremod.network.messages.GuardMobAttackListMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_BUILDING_NAME;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_CONFIG_NAME;
import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Abstract class for Guard huts.
 */
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S1448"})
public abstract class AbstractBuildingGuards extends AbstractBuildingWorker implements IGuardBuilding
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TASK           = "TASK";
    private static final String NBT_JOB            = "guardType";
    private static final String NBT_ASSIGN         = "assign";
    private static final String NBT_RETRIEVE       = "retrieve";
    private static final String NBT_PATROL         = "patrol";
    private static final String NBT_TIGHT_GROUPING = "tightGrouping";
    private static final String NBT_PATROL_TARGETS = "patrol targets";
    private static final String NBT_TARGET         = "target";
    private static final String NBT_GUARD          = "guard";
    private static final String NBT_MOBS           = "mobs";
    private static final String NBT_MOB_VIEW       = "mobview";

    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    ////// --------------------------- GuardJob Enum --------------------------- \\\\\\

    /**
     * The Bonus Health for each building level
     */
    private static final int BONUS_HEALTH_PER_LEVEL = 2;

    /**
     * The health modifier which changes the HP
     */
    private final AttributeModifier healthModConfig = new AttributeModifier(GUARD_HEALTH_MOD_CONFIG_NAME, Configurations.gameplay.guardHealthMult - 1, 1);

    /**
     * Vision range per building level.
     */
    private static final int VISION_RANGE_PER_LEVEL = 5;

    /**
     * Whether the guardType will be assigned manually.
     */
    private boolean assignManually = false;

    /**
     * Whether to retrieve the guard on low health.
     */
    private boolean retrieveOnLowHealth = false;

    /**
     * The level for getting our achievement
     */
    private static final int ACHIEVEMENT_LEVEL = 1;

    /**
     * Whether to patrol manually or not.
     */
    private boolean patrolManually = false;

    /**
     * The task of the guard, following the {@link GuardTask} enum.
     */
    private GuardTask task = GuardTask.GUARD;

    /**
     * The position at which the guard should guard at.
     */
    private BlockPos guardPos = this.getID();

    /**
     * The guardType of the guard, Any possible {@link GuardType}.
     */
    private GuardType job = null;

    /**
     * The list of manual patrol targets.
     */
    private List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * Hashmap of mobs we may or may not attack.
     */
    private List<MobEntryView> mobsToAttack = new ArrayList<>();

    /**
     * The player the guard has been set to follow.
     */
    private EntityPlayer followPlayer;

    /**
     * Indicates if in Follow mode what type of follow is use.
     * True - tight grouping, false - lose grouping.
     */
    private boolean tightGrouping;

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
                                 && itemStack.getItem() instanceof ItemArmor
                                 && ((ItemArmor) itemStack.getItem()).armorType == EntityEquipmentSlot.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ItemArmor
                                 && ((ItemArmor) itemStack.getItem()).armorType == EntityEquipmentSlot.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ItemArmor
                                 && ((ItemArmor) itemStack.getItem()).armorType == EntityEquipmentSlot.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                                 && itemStack.getItem() instanceof ItemArmor
                                 && ((ItemArmor) itemStack.getItem()).armorType == EntityEquipmentSlot.FEET, new Tuple<>(1, true));
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
                    optCitizen.get().removeHealthModifier(GUARD_HEALTH_MOD_BUILDING_NAME);

                    final AttributeModifier healthModBuildingHP = new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, getBonusHealth(), 0);
                    optCitizen.get().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(healthModBuildingHP);
                }
            }
        }

        super.onUpgradeComplete(newLevel);

        if (newLevel == ACHIEVEMENT_LEVEL)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingGuard);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeGuardMax);
        }
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        // Only change HP values if assign successful
        if (super.assignCitizen(citizen) && citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getCitizenEntity();
            if (optCitizen.isPresent())
            {
                final AttributeModifier healthModBuildingHP = new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, getBonusHealth(), 0);
                optCitizen.get().increaseHPForGuards();
                optCitizen
                  .get()
                  .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                  .applyModifier(healthModBuildingHP);
                optCitizen
                  .get()
                  .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                  .applyModifier(healthModConfig);
                optCitizen
                  .get()
                  .getEntityAttribute(SharedMonsterAttributes.ARMOR)
                  .setBaseValue(SharedMonsterAttributes.ARMOR.getDefaultValue() + getDefenceBonus());
            }
            colony.getCitizenManager().calculateMaxCitizens();

            // Set new home, since guards are housed at their workerbuilding.
            final IBuilding building = citizen.getHomeBuilding();
            if (building != null && !building.getID().equals(this.getID()))
            {
                building.removeCitizen(citizen);
            }
            citizen.setHomeBuilding(this);

            return true;
        }
        return false;
    }

    //// ---- NBT Overrides ---- \\\\

    //// ---- Overrides ---- \\\\

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        task = GuardTask.values()[compound.getInteger(NBT_TASK)];
        final ResourceLocation jobName = new ResourceLocation(compound.getString(NBT_JOB));
        job = IGuardTypeDataManager.getInstance().getFrom(jobName);
        assignManually = compound.getBoolean(NBT_ASSIGN);
        retrieveOnLowHealth = compound.getBoolean(NBT_RETRIEVE);
        patrolManually = compound.getBoolean(NBT_PATROL);
        if (compound.hasKey(NBT_TIGHT_GROUPING))
        {
            tightGrouping = compound.getBoolean(NBT_TIGHT_GROUPING);
        }
        else
        {
            tightGrouping = true;
        }

        final NBTTagList wayPointTagList = compound.getTagList(NBT_PATROL_TARGETS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockAtPos = wayPointTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, NBT_TARGET);
            patrolTargets.add(pos);
        }

        final NBTTagList mobsTagList = compound.getTagList(NBT_MOBS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < mobsTagList.tagCount(); i++)
        {
            final NBTTagCompound mobCompound = mobsTagList.getCompoundTagAt(i);
            final MobEntryView mobEntry = MobEntryView.readFromNBT(mobCompound, NBT_MOB_VIEW);
            if (mobEntry.getEntityEntry() != null)
            {
                mobsToAttack.add(mobEntry);
            }
        }

        guardPos = NBTUtil.getPosFromTag(compound.getCompoundTag(NBT_GUARD));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();

        compound.setInteger(NBT_TASK, task.ordinal());
        compound.setString(NBT_JOB, job == null ? "" : job.getRegistryName().toString());
        compound.setBoolean(NBT_ASSIGN, assignManually);
        compound.setBoolean(NBT_RETRIEVE, retrieveOnLowHealth);
        compound.setBoolean(NBT_PATROL, patrolManually);
        compound.setBoolean(NBT_TIGHT_GROUPING, tightGrouping);

        @NotNull final NBTTagList wayPointTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : patrolTargets)
        {
            @NotNull final NBTTagCompound wayPointCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(wayPointCompound, NBT_TARGET, pos);

            wayPointTagList.appendTag(wayPointCompound);
        }
        compound.setTag(NBT_PATROL_TARGETS, wayPointTagList);

        @NotNull final NBTTagList mobsTagList = new NBTTagList();
        for (@NotNull final MobEntryView entry : mobsToAttack)
        {
            @NotNull final NBTTagCompound mobCompound = new NBTTagCompound();
            MobEntryView.writeToNBT(mobCompound, NBT_MOB_VIEW, entry);
            mobsTagList.appendTag(mobCompound);
        }
        compound.setTag(NBT_MOBS, mobsTagList);

        compound.setTag(NBT_GUARD, NBTUtil.createPosTag(guardPos));

        return compound;
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getCitizenEntity();
            if (optCitizen.isPresent())
            {
                optCitizen.get().removeAllHealthModifiers();
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStackUtils.EMPTY);
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStackUtils.EMPTY);
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStackUtils.EMPTY);
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStackUtils.EMPTY);
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
                optCitizen.get().setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStackUtils.EMPTY);
            }
        }
        super.removeCitizen(citizen);
        colony.getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(assignManually);
        buf.writeBoolean(retrieveOnLowHealth);
        buf.writeBoolean(patrolManually);
        buf.writeBoolean(tightGrouping);
        buf.writeInt(task.ordinal());
        ByteBufUtils.writeUTF8String(buf, job == null ? "" : job.getRegistryName().toString());
        buf.writeInt(patrolTargets.size());

        for (final BlockPos pos : patrolTargets)
        {
            BlockPosUtil.writeToByteBuf(buf, pos);
        }

        if (mobsToAttack.isEmpty())
        {
            mobsToAttack.addAll(calculateMobs());
        }

        buf.writeInt(mobsToAttack.size());
        for (final MobEntryView entry : mobsToAttack)
        {
            MobEntryView.writeToByteBuf(buf, entry);
        }

        BlockPosUtil.writeToByteBuf(buf, guardPos);

        buf.writeInt(this.getAssignedCitizen().size());
        for (final ICitizenData citizen : this.getAssignedCitizen())
        {
            buf.writeInt(citizen.getId());
        }
    }

    /**
     * Get the guard's {@link GuardTask}.
     *
     * @return The task of the guard.
     */
    @Override
    public GuardTask getTask()
    {
        return this.task;
    }

    /**
     * Set the guard's {@link GuardTask}.
     *
     * @param task The task to set.
     */
    @Override
    public void setTask(final GuardTask task)
    {
        this.task = task;
        this.markDirty();
    }

    /**
     * Entity of player to follow.
     *
     * @return the entityPlayer reference.
     */
    @Override
    public EntityPlayer getFollowPlayer()
    {
        return followPlayer;
    }

    //// ---- Overrides ---- \\\\

    //// ---- Abstract Methods ---- \\\\

    //// ---- Abstract Methods ---- \\\\

    /**
     * Returns a patrolTarget to patrol to.
     *
     * @param currentPatrolTarget previous target.
     * @return the position of the next target.
     */
    @Override
    @Nullable
    public BlockPos getNextPatrolTarget(final BlockPos currentPatrolTarget)
    {
        if (!patrolManually)
        {
            if (currentPatrolTarget == null)
            {
                return getPosition();
            }
            else
            {
                final BlockPos pos = BlockPosUtil.getRandomPosition(getColony().getWorld(), currentPatrolTarget, getPosition());
                if (BlockPosUtil.getDistance2D(pos, getPosition()) > getPatrolDistance())
                {
                    return getPosition();
                }
                return pos;
            }
        }

        if (patrolTargets == null || patrolTargets.isEmpty())
        {
            return null;
        }

        if (currentPatrolTarget == null)
        {
            return patrolTargets.get(0);
        }

        if (patrolTargets.contains(currentPatrolTarget))
        {
            int index = patrolTargets.indexOf(currentPatrolTarget) + 1;

            if (index >= patrolTargets.size())
            {
                index = 0;
            }

            return patrolTargets.get(index);
        }
        return patrolTargets.get(0);
    }

    /**
     * Getter for the patrol distance the guard currently has.
     *
     * @return The distance in whole numbers.
     */
    @Override
    public int getPatrolDistance()
    {
        return this.getBuildingLevel() * PATROL_DISTANCE;
    }

    /**
     * The client view for the Guard building.
     */
    public static class View extends AbstractBuildingWorker.View
    {

        /**
         * Assign the guardType manually, knight, guard, or *Other* (Future usage)
         */
        private boolean assignManually = false;

        /**
         * Retrieve the guard on low health.
         */
        private boolean retrieveOnLowHealth = false;

        /**
         * Patrol manually or automatically.
         */
        private boolean patrolManually = false;

        /**
         * The {@link GuardTask} of the guard.
         */
        private GuardTask task = GuardTask.GUARD;

        /**
         * Position the guard should guard.
         */
        private BlockPos guardPos = this.getID();

        /**
         * The {@link GuardType} of the guard
         */
        private GuardType guardType = null;

        /**
         * Indicates whether tight grouping is use or
         * lose grouping.
         */
        private boolean tightGrouping = true;

        /**
         * The list of manual patrol targets.
         */
        private List<BlockPos> patrolTargets = new ArrayList<>();

        /**
         * Hashmap of mobs we may or may not attack.
         */
        private List<MobEntryView> mobsToAttack = new ArrayList<>();

        @NotNull
        private final List<Integer> guards = new ArrayList<>();

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
            return new WindowHutGuardTower(this);
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
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            assignManually = buf.readBoolean();
            retrieveOnLowHealth = buf.readBoolean();
            patrolManually = buf.readBoolean();
            tightGrouping = buf.readBoolean();


            task = GuardTask.values()[buf.readInt()];
            final ResourceLocation jobId = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
            guardType = IGuardTypeRegistry.getInstance().getValue(jobId);

            final int targetSize = buf.readInt();
            patrolTargets = new ArrayList<>();

            for (int i = 0; i < targetSize; i++)
            {
                patrolTargets.add(BlockPosUtil.readFromByteBuf(buf));
            }

            final int mobSize = buf.readInt();
            for (int i = 0; i < mobSize; i++)
            {
                final MobEntryView mobEntry = MobEntryView.readFromByteBuf(buf);
                mobsToAttack.add(mobEntry);
            }

            guardPos = BlockPosUtil.readFromByteBuf(buf);

            final int numResidents = buf.readInt();
            for (int i = 0; i < numResidents; ++i)
            {
                guards.add(buf.readInt());
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

        public void setAssignManually(final boolean assignManually)
        {
            this.assignManually = assignManually;
        }

        public boolean isAssignManually()
        {
            return assignManually;
        }

        public void setRetrieveOnLowHealth(final boolean retrieveOnLowHealth)
        {
            this.retrieveOnLowHealth = retrieveOnLowHealth;
        }

        public boolean isRetrieveOnLowHealth()
        {
            return retrieveOnLowHealth;
        }

        /**
         * Set whether to use tight grouping or lose grouping.
         *
         * @param tightGrouping - indicates if you are using tight grouping
         */
        public void setTightGrouping(final boolean tightGrouping)
        {
            this.tightGrouping = tightGrouping;
        }

        /**
         * Returns whether tight grouping in Follow mode is being used.
         *
         * @return whether tight grouping is being used.
         */
        public boolean isTightGrouping()
        {
            return tightGrouping;
        }

        public void setPatrolManually(final boolean patrolManually)
        {
            this.patrolManually = patrolManually;
        }

        public void setMobsToAttack(final List<MobEntryView> mobsToAttack)
        {
            this.mobsToAttack = new ArrayList<>(mobsToAttack);
        }

        public boolean isPatrolManually()
        {
            return patrolManually;
        }

        public void setTask(final GuardTask task)
        {
            this.task = task;
            this.getColony().markDirty();
        }

        public GuardTask getTask()
        {
            return task;
        }

        public BlockPos getGuardPos()
        {
            return guardPos;
        }

        public GuardType getGuardType()
        {
            return guardType;
        }

        public void setGuardType(final GuardType job)
        {
            this.guardType = job;
        }

        public List<BlockPos> getPatrolTargets()
        {
            return new ArrayList<>(patrolTargets);
        }

        public List<MobEntryView> getMobsToAttack()
        {
            return new ArrayList<>(mobsToAttack);
        }
    }

    /**
     * Get the guard's {@link GuardType}.
     *
     * @return The guardType of the guard.
     */
    @Override
    public GuardType getGuardType()
    {
        if (job == null)
        {
            final List<GuardType> guardTypes = new ArrayList<>(IGuardTypeRegistry.getInstance().getValuesCollection());
            job = guardTypes.get(new Random().nextInt(guardTypes.size()));
        }
        return this.job;
    }

    /**
     * Set the guard's {@link GuardType}.
     *
     * @param job The guardType to set.
     */
    @Override
    public void setGuardType(final GuardType job)
    {
        this.job = job;
        for (final ICitizenData citizen : getAssignedCitizen())
        {
            citizen.setJob(createJob(citizen));
        }
        this.markDirty();
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
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
    public List<BlockPos> getPatrolTargets()
    {
        return new ArrayList<>(patrolTargets);
    }

    /**
     * Get the guard's RetrieveOnLowHeath.
     *
     * @return if so.
     */
    @Override
    public boolean shallRetrieveOnLowHealth()
    {
        return retrieveOnLowHealth;
    }

    /**
     * Set the guard's RetrieveOnLowHealth.
     *
     * @param retrieve true if retrieve.
     */
    @Override
    public void setRetrieveOnLowHealth(final boolean retrieve)
    {
        this.retrieveOnLowHealth = retrieve;
    }

    /**
     * Get whether the guard should patrol manually.
     *
     * @return if so.
     */
    @Override
    public boolean shallPatrolManually()
    {
        return patrolManually;
    }

    /**
     * Set whether the guard should patrol manually.
     *
     * @param patrolManually true if manual.
     */
    @Override
    public void setPatrolManually(final boolean patrolManually)
    {
        this.patrolManually = patrolManually;
    }

    /**
     * Whether the player will assign guards manually or not.
     *
     * @return true if so
     */
    @Override
    public boolean shallAssignManually()
    {
        return assignManually;
    }

    /**
     * Set whether the player is assigning guards manually.
     *
     * @param assignManually true if so
     */
    @Override
    public void setAssignManually(final boolean assignManually)
    {
        this.assignManually = assignManually;
    }

    /**
     * Returns whether tight grouping in Follow mode is being used.
     *
     * @return whether tight grouping is being used.
     */
    @Override
    public boolean isTightGrouping()
    {
        return tightGrouping;
    }

    /**
     * Set whether to use tight grouping or lose grouping.
     *
     * @param tightGrouping - indicates if you are using tight grouping
     */
    @Override
    public void setTightGrouping(final boolean tightGrouping)
    {
        this.tightGrouping = tightGrouping;
    }

    /**
     * Get the position the guard should guard.
     *
     * @return the {@link BlockPos} of the guard position.
     */
    @Override
    public BlockPos getGuardPos()
    {
        return guardPos;
    }

    /**
     * Set where the guard should guard.
     *
     * @param guardPos the {@link BlockPos} to guard.
     */
    @Override
    public void setGuardPos(final BlockPos guardPos)
    {
        this.guardPos = guardPos;
    }

    /**
     * Get the Map of mobs to attack.
     *
     * @return the map.
     */
    @Override
    public List<MobEntryView> getMobsToAttack()
    {
        mobsToAttack.sort(Comparator.comparing(MobEntryView::getPriority, Comparator.reverseOrder()));
        return new ArrayList<>(mobsToAttack);
    }

    /**
     * Set the Map of mobs to attack.
     *
     * @param list The new map.
     */
    @Override
    public void setMobsToAttack(final List<MobEntryView> list)
    {
        this.mobsToAttack.clear();
        this.mobsToAttack = new ArrayList<>(list);
    }

    /**
     * Gets the player to follow.
     *
     * @return the entity player.
     */
    @Override
    public BlockPos getPlayerToFollow()
    {
        if (task.equals(GuardTask.FOLLOW) && followPlayer != null)
        {
            return followPlayer.getPosition();
        }
        task = GuardTask.GUARD;
        markDirty();
        return this.getPosition();
    }

    /**
     * Sets the player to follow.
     *
     * @param player the player to follow.
     */
    @Override
    public void setPlayerToFollow(final EntityPlayer player)
    {
        if (this.getColony().getWorld() != null)
        {
            this.getColony().getWorld().getScoreboard().addPlayerToTeam(player.getName(), TEAM_COLONY_NAME + getColony().getID());
            player.addPotionEffect(new PotionEffect(GLOW_EFFECT, GLOW_EFFECT_DURATION_TEAM, GLOW_EFFECT_MULTIPLIER));

            if (followPlayer != null)
            {
                try
                {
                    this.getColony()
                      .getWorld()
                      .getScoreboard()
                      .removePlayerFromTeam(followPlayer.getName(), this.getColony().getWorld().getScoreboard().getTeam(TEAM_COLONY_NAME + getColony().getID()));
                    player.removePotionEffect(GLOW_EFFECT);
                }
                catch (final Exception e)
                {
                    Log.getLogger().warn("Unable to remove player " + followPlayer.getName() + " from team " + TEAM_COLONY_NAME + getColony().getID());
                }
            }
        }
        this.followPlayer = player;
    }

    /**
     * Bonus guard hp per bulding level
     *
     * @return the bonus health.
     */
    private int getBonusHealth()
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
        return getBuildingLevel() * VISION_RANGE_PER_LEVEL;
    }

    /**
     * Populates the mobs list from the ForgeRegistries.
     *
     * @return the list of MobEntrys to attack.
     */
    @Override
    public List<MobEntryView> calculateMobs()
    {
        final List<MobEntryView> mobs = new ArrayList<>();

        int i = 0;
        for (final EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection())
        {
            if (EntityMob.class.isAssignableFrom(entry.getEntityClass()))
            {
                i++;
                mobs.add(new MobEntryView(entry.getRegistryName(), true, i));
            }
            else
            {
                for (final String location : Configurations.gameplay.guardResourceLocations)
                {
                    if (entry.getRegistryName() != null && entry.getRegistryName().toString().equals(location))
                    {
                        i++;
                        mobs.add(new MobEntryView(entry.getRegistryName(), true, i));
                    }
                }
            }
        }

        getColony().getPackageManager().getCloseSubscribers().forEach(player -> MineColonies
                                                                             .getNetwork()
                                                                             .sendTo(new GuardMobAttackListMessage(getColony().getID(),
                                                                                 getID(),
                                                                                 mobsToAttack),
                                                                               player));

        return mobs;
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }


}
