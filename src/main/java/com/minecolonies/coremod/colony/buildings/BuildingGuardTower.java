package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutGuardTower;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Building class of the guard tower.
 */
public class BuildingGuardTower extends AbstractBuildingWorker
{
    /**
     * Worker gets this distance times building level away from his building to patrol.
     */
    public static final int PATROL_DISTANCE = 40;

    /**
     * Name description of the guard hat.
     */
    private static final String GUARD_TOWER = "GuardTower";

    /**
     * TAG to store his task to nbt.
     */
    private static final String TAG_TASK            = "TASK";
    private static final String TAG_JOB             = "job";
    /**
     * Max level of the guard hut.
     */
    private static final int    GUARD_HUT_MAX_LEVEL = 5;

    /**
     * The max vision bonus multiplier.
     */
    private static final int MAX_VISION_BONUS_MULTIPLIER = 3;

    /**
     * The health multiplier each level after level 4.
     */
    private static final int HEALTH_MULTIPLIER = 2;

    /**
     * Base max health of the guard.
     */
    private static final double BASE_MAX_HEALTH = 20D;

    /**
     * Vision bonus per level.
     */
    private static final int    VISION_BONUS       = 5;
    private static final String TAG_ASSIGN         = "assign";
    private static final String TAG_RETRIEVE       = "retrieve";
    private static final String TAG_PATROL         = "patrol";
    private static final String TAG_PATROL_TARGETS = "patrol targets";
    private static final String TAG_TARGET         = "target";
    private static final String TAG_GUARD          = "guard";

    /**
     * Assign the job manually, knight or ranger.
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
     * The task of the guard, following the Task enum.
     */
    private Task task = Task.GUARD;

    /**
     * Position the guard should guard at.
     */
    private BlockPos guardPos = this.getID();

    /**
     * The job of the guard, following the GuarJob enum.
     */
    private GuardJob job = null;

    /**
     * The list of manual patrol targets.
     */
    private ArrayList<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * The player to follow.
     */
    private EntityPlayer followPlayer;

    /**
     * Constructor for the guardTower building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingGuardTower(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the job/ai of the guard.
     *
     * @return the enum job.
     */
    public GuardJob getJob()
    {
        return this.job;
    }

    /**
     * Sets the job/ai of the guard.
     *
     * @param job the job to set.
     */
    public void setJob(final GuardJob job)
    {
        this.job = job;
        this.markDirty();
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Guard schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return GUARD_TOWER;
    }

    /**
     * Gets the max level of the baker's hut.
     *
     * @return The max level of the baker's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return GUARD_HUT_MAX_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        if (this.getWorkerEntity() != null && newLevel > MAX_VISION_BONUS_MULTIPLIER)
        {
            this.getWorkerEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH + getBonusHealth());
        }

        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingGuard);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeGuardMax);
        }
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
     * Getter for the patrol distance the guard currently has.
     *
     * @return the distance in whole numbers.
     */
    public int getPatrolDistance()
    {
        return this.getBuildingLevel() * PATROL_DISTANCE;
    }

    /**
     * Gets the player to follow.
     *
     * @return the entity player.
     */
    public BlockPos getPlayerToFollow()
    {
        if (task.equals(Task.FOLLOW) && followPlayer != null)
        {
            return followPlayer.getPosition();
        }
        task = Task.GUARD;
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
     * Sets the guards guard target.
     *
     * @param target the target to set.
     */
    public void setGuardTarget(final BlockPos target)
    {
        this.guardPos = target;
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
     * Getter of the guard task.
     *
     * @return the task of the guard (Patrol, Follow, Guard).
     */
    public Task getTask()
    {
        return this.task;
    }

    /**
     * Set the task of the guard.
     *
     * @param task the task to set.
     */
    public void setTask(final Task task)
    {
        this.task = task;
        this.markDirty();
    }

    /**
     * Getter for the player.
     *
     * @return the player.
     */
    public EntityPlayer getPlayer()
    {
        return followPlayer;
    }

    /**
     * Returns a patrolTarget to patrol to.
     *
     * @param currentPatrolTarget previous target.
     * @return the position of the next target.
     */
    @Nullable
    public BlockPos getNextPatrolTarget(final BlockPos currentPatrolTarget)
    {
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

    @Override
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack)
                && (stack.getItem() instanceof ItemArmor
                || stack.getItem() instanceof ItemTool
                || stack.getItem() instanceof ItemSword
                || stack.getItem() instanceof ItemBow);
    }

    /**
     * The name of the baker's job.
     *
     * @return The name of the baker's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return GUARD_TOWER;
    }

    /**
     * Create a Guard job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Guard job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobGuard(citizen);
    }

    @Override
    public void setWorker(final CitizenData citizen)
    {
        if (citizen == null && this.getWorkerEntity() != null)
        {
            this.getWorkerEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        }
        else if (citizen != null && citizen.getCitizenEntity() != null)
        {
            citizen.getCitizenEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH + getBonusHealth());
        }
        super.setWorker(citizen);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        task = Task.values()[compound.getInteger(TAG_TASK)];
        final int jobId = compound.getInteger(TAG_JOB);
        job = jobId == -1 ? null : GuardJob.values()[jobId];
        assignManually = compound.getBoolean(TAG_ASSIGN);
        retrieveOnLowHealth = compound.getBoolean(TAG_RETRIEVE);
        patrolManually = compound.getBoolean(TAG_PATROL);

        final NBTTagList wayPointTagList = compound.getTagList(TAG_PATROL_TARGETS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockAtPos = wayPointTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, TAG_TARGET);
            patrolTargets.add(pos);
        }

        guardPos = BlockPosUtil.readFromNBT(compound, TAG_GUARD);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_TASK, task.ordinal());
        compound.setInteger(TAG_JOB, job == null ? -1 : job.ordinal());
        compound.setBoolean(TAG_ASSIGN, assignManually);
        compound.setBoolean(TAG_RETRIEVE, retrieveOnLowHealth);
        compound.setBoolean(TAG_PATROL, patrolManually);

        @NotNull final NBTTagList wayPointTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : patrolTargets)
        {
            @NotNull final NBTTagCompound wayPointCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(wayPointCompound, TAG_TARGET, pos);


            wayPointTagList.appendTag(wayPointCompound);
        }
        compound.setTag(TAG_PATROL_TARGETS, wayPointTagList);

        BlockPosUtil.writeToNBT(compound, TAG_GUARD, guardPos);
    }

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

        BlockPosUtil.writeToByteBuf(buf, guardPos);
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

    /**
     * Check if the guard should retrieve on low health.
     *
     * @return true if so.
     */
    public boolean shallRetrieveOnLowHealth()
    {
        return retrieveOnLowHealth;
    }

    /**
     * Set if the guard should retrieve on low health.
     *
     * @param retrieveOnLowHealth state to set.
     */
    public void setRetrieveOnLowHealth(final boolean retrieveOnLowHealth)
    {
        this.retrieveOnLowHealth = retrieveOnLowHealth;
        this.markDirty();
    }

    /**
     * Check if the guard should patrol manually (Use defined values).
     *
     * @return true if so.
     */
    public boolean shallPatrolManually()
    {
        return patrolManually;
    }

    /**
     * Set if the guard should patrol manually.
     *
     * @param patrolManually the state to set.
     */
    public void setPatrolManually(final boolean patrolManually)
    {
        this.patrolManually = patrolManually;
        this.markDirty();
    }

    /**
     * Getter of the position the gurd should guard.
     *
     * @return the blockPos position.
     */
    public BlockPos getGuardPos()
    {
        return guardPos;
    }

    /**
     * Set if the jobs should be assigned manually.
     *
     * @param assignManually the state to set.
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.assignManually = assignManually;
        this.markDirty();
    }

    /**
     * Possible job/AI.
     */
    public enum GuardJob
    {
        KNIGHT,
        RANGER,
    }

    /**
     * Possible tasks.
     */
    public enum Task
    {
        FOLLOW,
        GUARD,
        PATROL
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Assign the job manually, knight or ranger.
         */
        public boolean assignManually = false;

        /**
         * Retrieve the guard on low health.
         */
        public boolean retrieveOnLowHealth = false;

        /**
         * Patrol manually or automatically.
         */
        public boolean patrolManually = false;

        /**
         * The task of the guard, following the Task enum.
         */
        public Task task = Task.GUARD;

        /**
         * Position the guard should guard at.
         */
        public BlockPos guardPos = this.getID();

        /**
         * The job of the guard, following the GuarJob enum.
         */
        public GuardJob job = null;

        /**
         * The list of manual patrol targets.
         */
        public List<BlockPos> patrolTargets = new ArrayList<>();

        /**
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutGuardTower(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            assignManually = buf.readBoolean();
            retrieveOnLowHealth = buf.readBoolean();
            patrolManually = buf.readBoolean();
            task = Task.values()[buf.readInt()];
            final int jobId = buf.readInt();
            job = jobId == -1 ? null : GuardJob.values()[jobId];

            final int size = buf.readInt();
            patrolTargets = new ArrayList<>();

            for (int i = 0; i < size; i++)
            {
                patrolTargets.add(BlockPosUtil.readFromByteBuf(buf));
            }

            guardPos = BlockPosUtil.readFromByteBuf(buf);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            if(GuardJob.KNIGHT.equals(job))
            {
                return Skill.STRENGTH;
            }

            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            if(GuardJob.KNIGHT.equals(job))
            {
                return Skill.ENDURANCE;
            }

            return Skill.STRENGTH;
        }
    }
}


