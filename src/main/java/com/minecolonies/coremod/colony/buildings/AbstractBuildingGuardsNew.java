package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutGuardTowerNew;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Abstract class for Guard huts.
 */
public abstract class AbstractBuildingGuardsNew extends AbstractBuildingWorker
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TASK           = "TASK";
    private static final String NBT_JOB            = "job";
    private static final String NBT_ASSIGN         = "assign";
    private static final String NBT_RETRIEVE       = "retrieve";
    private static final String NBT_PATROL         = "patrol";
    private static final String NBT_PATROL_TARGETS = "patrol targets";
    private static final String NBT_TARGET         = "target";
    private static final String NBT_GUARD          = "guard";
    private static final String NBT_MOBS           = "mobs";
    private static final String NBT_MOB_VIEW       = "mobview";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    ////// --------------------------- GuardTask Enum --------------------------- \\\\\\
    public enum GuardTask
    {
        FOLLOW,
        GUARD,
        PATROL
    }
    ////// --------------------------- GuardTask Enum --------------------------- \\\\\\

    ////// --------------------------- GuardJob Enum --------------------------- \\\\\\
    public enum GuardJob
    {
        RANGER("com.minecolonies.coremod.job.Ranger", "com.minecolonies.coremod.gui.workerHuts.ranger"),
        KNIGHT("com.minecolonies.coremod.job.Knight", "com.minecolonies.coremod.gui.workerHuts.knight");

        public final String jobName;
        public final String buttonName;

        GuardJob(final String name, final String buttonName)
        {
            this.jobName = name;
            this.buttonName = buttonName;
        }

        public AbstractJobGuard getGuardJob(final CitizenData citizen)
        {
            if (this == RANGER)
            {
                return new JobRanger(citizen);
            }
            else if (this == KNIGHT)
            {
                return new JobKnight(citizen);
            }
            return new JobRanger(citizen);
        }
    }
    ////// --------------------------- GuardJob Enum --------------------------- \\\\\\
    /**
     * Worker gets this distance times building level away from his/her hut to
     * patrol.
     */
    public static final int PATROL_DISTANCE = 40;

    /**
     * The max vision bonus multiplier for the hut.
     */
    private static final int MAX_VISION_BONUS_MULTIPLIER = 3;

    /**
     * The health multiplier each level after level 4.
     */
    private static final int HEALTH_MULTIPLIER = 2;

    /**
     * Vision bonus per level.
     */
    private static final int VISION_BONUS = 5;

    /**
     * Whether the job will be assigned manually.
     */
    private boolean assignManually = false;

    /**
     * Whether to retrieve the guard on low health.
     */
    private boolean retrieveOnLowHealth = false;

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
     * The job of the guard, Any possible {@link GuardJob}.
     */
    private GuardJob job = GuardJob.KNIGHT;

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
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingGuardsNew(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SWORD, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.BOW, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);
        keepX.put(itemStack -> itemStack.getItem() instanceof ItemTool, 1);
        keepX.put(itemStack -> itemStack.getItem() instanceof ItemArmor, 1);
    }

    //// ---- NBT Overrides ---- \\\\

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        task = GuardTask.values()[compound.getInteger(NBT_TASK)];
        final int jobId = compound.getInteger(NBT_JOB);
        job = jobId == -1 ? null : GuardJob.values()[jobId];
        assignManually = compound.getBoolean(NBT_ASSIGN);
        retrieveOnLowHealth = compound.getBoolean(NBT_RETRIEVE);
        patrolManually = compound.getBoolean(NBT_PATROL);

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
            mobsToAttack.add(mobEntry);
        }

        guardPos = NBTUtil.getPosFromTag(compound.getCompoundTag(NBT_GUARD));
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(NBT_TASK, task.ordinal());
        compound.setInteger(NBT_JOB, job == null ? -1 : job.ordinal());
        compound.setBoolean(NBT_ASSIGN, assignManually);
        compound.setBoolean(NBT_RETRIEVE, retrieveOnLowHealth);
        compound.setBoolean(NBT_PATROL, patrolManually);

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
    }

    //// ---- NBT Overrides ---- \\\\

    //// ---- Overrides ---- \\\\

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(assignManually);
        buf.writeBoolean(retrieveOnLowHealth);
        buf.writeBoolean(patrolManually);
        buf.writeInt(task.ordinal());
        buf.writeInt(job == null ? -1 : job.ordinal());
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
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return job.getGuardJob(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return job.jobName;
    }

    /**
     * We use this to set possible health multipliers and give achievements.
     *
     * @param newLevel The new level.
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        if (getWorkerEntities() != null)
        {
            for (final Optional<EntityCitizen> optCitizen : getWorkerEntities())
            {
                if (optCitizen.isPresent() && newLevel > MAX_VISION_BONUS_MULTIPLIER)
                {
                    optCitizen.get().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(SharedMonsterAttributes.MAX_HEALTH.getDefaultValue() + getBonusHealth());
                }
            }
        }

        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingGuard);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeGuardMax);
        }
    }

    @Override
    public void removeCitizen(final CitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<EntityCitizen> optCitizen = citizen.getCitizenEntity();
            if (optCitizen.isPresent())
            {
                optCitizen.get().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(SharedMonsterAttributes.MAX_HEALTH.getDefaultValue());
                optCitizen.get().getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(SharedMonsterAttributes.ARMOR.getDefaultValue());
            }
        }
        super.removeCitizen(citizen);
    }

    @Override
    public void setWorker(final CitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<EntityCitizen> optCitizen = citizen.getCitizenEntity();
            if (optCitizen.isPresent())
            {
                optCitizen
                  .get()
                  .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                  .setBaseValue(SharedMonsterAttributes.MAX_HEALTH.getDefaultValue() + getBonusHealth());
                optCitizen
                  .get()
                  .getEntityAttribute(SharedMonsterAttributes.ARMOR)
                  .setBaseValue(SharedMonsterAttributes.ARMOR.getDefaultValue() + getDefenceBonus());
            }
        }
        super.setWorker(citizen);
    }

    //// ---- Overrides ---- \\\\

    //// ---- Abstract Methods ---- \\\\

    /**
     * Get an Defence bonus related to the building.
     *
     * @return an Integer.
     */
    public abstract int getDefenceBonus();

    /**
     * Get an Offence bonus related to the building.
     *
     * @return an Integer.
     */
    public abstract int getOffenceBonus();

    //// ---- Abstract Methods ---- \\\\

    /**
     * Getter for the patrol distance the guard currently has.
     *
     * @return The distance in whole numbers.
     */
    public int getPatrolDistance()
    {
        return this.getBuildingLevel() * PATROL_DISTANCE;
    }

    /**
     * Get the guard's {@link GuardJob}.
     *
     * @return The job of the guard.
     */
    public GuardJob getJob()
    {
        return this.job;
    }

    /**
     * Set the guard's {@link GuardJob}.
     *
     * @param job The job to set.
     */
    public void setJob(final GuardJob job)
    {
        this.job = job;
        for (final CitizenData citizen : getWorker())
        {
            citizen.setJob(createJob(citizen));
        }
        this.markDirty();
    }

    /**
     * Get the guard's {@link GuardTask}.
     *
     * @return The task of the guard.
     */
    public GuardTask getTask()
    {
        return this.task;
    }

    /**
     * Set the guard's {@link GuardTask}.
     *
     * @param task The task to set.
     */
    public void setTask(final GuardTask task)
    {
        this.task = task;
        this.markDirty();
    }

    /**
     * Get the guard's RetrieveOnLowHeath.
     *
     * @return if so.
     */
    public boolean shallRetrieveOnLowHealth()
    {
        return retrieveOnLowHealth;
    }

    /**
     * Set the guard's RetrieveOnLowHealth.
     *
     * @param retrieve true if retrieve.
     */
    public void setRetrieveOnLowHealth(final boolean retrieve)
    {
        this.retrieveOnLowHealth = retrieve;
    }

    /**
     * Get whether the guard should patrol manually.
     *
     * @return if so.
     */
    public boolean shallPatrolManually()
    {
        return patrolManually;
    }

    /**
     * Set whether the guard should patrol manually.
     *
     * @param patrolManually true if manual.
     */
    public void setPatrolManually(final boolean patrolManually)
    {
        this.patrolManually = patrolManually;
    }

    /**
     * Whether the player will assign guards manually or not.
     *
     * @return true if so
     */
    public boolean shallAssignManually()
    {
        return assignManually;
    }

    /**
     * Set whether the player is assigning guards manually.
     *
     * @param assignManually true if so
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.assignManually = assignManually;
    }

    /**
     * Get the position the guard should guard.
     *
     * @return the {@link BlockPos} of the guard position.
     */
    public BlockPos getGuardPos()
    {
        return guardPos;
    }

    /**
     * Set where the guard should guard.
     *
     * @param guardPos the {@link BlockPos} to guard.
     */
    public void setGuardPos(final BlockPos guardPos)
    {
        this.guardPos = guardPos;
    }

    /**
     * Get the Map of mobs to attack.
     *
     * @return the map.
     */
    public List<MobEntryView> getMobsToAttack()
    {
        return mobsToAttack;
    }

    /**
     * Set the Map of mobs to attack.
     *
     * @param list The new map.
     */
    public void setMobsToAttack(final List<MobEntryView> list)
    {
        this.mobsToAttack.clear();
        this.mobsToAttack = list;
    }

    /**
     * Gets the player to follow.
     *
     * @return the entity player.
     */
    public BlockPos getPlayerToFollow()
    {
        if (task.equals(GuardTask.FOLLOW) && followPlayer != null)
        {
            return followPlayer.getPosition();
        }
        task = GuardTask.GUARD;
        return this.getLocation();
    }

    /**
     * Sets the player to follow.
     *
     * @param player the player to follow.
     */
    public void setPlayerToFollow(final EntityPlayer player)
    {
        this.followPlayer = player;
    }

    /**
     * If no vision multiplier give health bonus.
     *
     * @return the bonus health.
     */
    private int getBonusHealth()
    {
        if (getBuildingLevel() > MAX_VISION_BONUS_MULTIPLIER)
        {
            return (getBuildingLevel() - MAX_VISION_BONUS_MULTIPLIER) * HEALTH_MULTIPLIER;
        }
        return 0;
    }

    /**
     * Adds new patrolTargets.
     *
     * @param target the target to add
     */
    public void addPatrolTargets(final BlockPos target)
    {
        this.patrolTargets.add(target);
        this.markDirty();
    }

    /**
     * Resets the patrolTargets list.
     */
    public void resetPatrolTargets()
    {
        this.patrolTargets = new ArrayList<>();
        this.markDirty();
    }

    /**
     * Getter for the bonus vision.
     *
     * @return an integer for the additional range.
     */
    public int getBonusVision()
    {
        if (getBuildingLevel() <= MAX_VISION_BONUS_MULTIPLIER)
        {
            return getBuildingLevel() * VISION_BONUS;
        }
        return MAX_VISION_BONUS_MULTIPLIER * VISION_BONUS;
    }

    public List<MobEntryView> calculateMobs()
    {
        final List<MobEntryView> mobs = new ArrayList<>();

        int i = 0;
        for (final EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection())
        {
            if (entry.newInstance(getColony().getWorld()) instanceof EntityMob)
            {
                i++;
                mobs.add(new MobEntryView(entry.getRegistryName(), true, i));
            }
        }

        return mobs;
    }

    /**
     * The client view for the Guard building.
     */
    public static class View extends AbstractBuildingWorker.View
    {

        /**
         * Assign the job manually, knight, guard, or *Other* (Future usage)
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
         * The {@link GuardJob} of the guard
         */
        private GuardJob job = null;

        /**
         * The list of manual patrol targets.
         */
        private List<BlockPos> patrolTargets = new ArrayList<>();

        /**
         * Hashmap of mobs we may or may not attack.
         */
        private ArrayList<MobEntryView> mobsToAttack = new ArrayList<>();

        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
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
            return new WindowHutGuardTowerNew(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            assignManually = buf.readBoolean();
            retrieveOnLowHealth = buf.readBoolean();
            patrolManually = buf.readBoolean();
            task = GuardTask.values()[buf.readInt()];
            final int jobId = buf.readInt();
            job = jobId == -1 ? null : GuardJob.values()[jobId];

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
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            if (GuardJob.KNIGHT.equals(job))
            {
                return Skill.STRENGTH;
            }
            else
            {
                return Skill.INTELLIGENCE;
            }
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            if (GuardJob.KNIGHT.equals(job))
            {
                return Skill.ENDURANCE;
            }
            else
            {
                return Skill.STRENGTH;
            }
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

        public void setPatrolManually(final boolean patrolManually)
        {
            this.patrolManually = patrolManually;
        }

        public boolean isPatrolManually()
        {
            return patrolManually;
        }

        public void setTask(final GuardTask task)
        {
            this.task = task;
        }

        public GuardTask getTask()
        {
            return task;
        }

        public BlockPos getGuardPos()
        {
            return guardPos;
        }

        public void setJob(final GuardJob job)
        {
            this.job = job;
        }

        public GuardJob getJob()
        {
            return job;
        }

        public List<BlockPos> getPatrolTargets()
        {
            return patrolTargets;
        }

        public List<MobEntryView> getMobsToAttack()
        {
            return mobsToAttack;
        }
    }
}
