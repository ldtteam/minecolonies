package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_HAND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_MAXIMUM;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuildingHut
{
    /**
     * Minimal level to ask for wood tools. (WOOD_HUT_LEVEL + 1 == stone)
     */
    public static final int WOOD_HUT_LEVEL = 0;

    /**
     * Tag used to store the worker to nbt.
     */
    private static final String TAG_WORKER = "worker";

    /**
     * The citizenData of the assigned worker.
     */
    private CitizenData worker;

    /**
     * Available skills of the citizens.
     */
    public enum Skill
    {
        STRENGTH,
        ENDURANCE,
        CHARISMA,
        INTELLIGENCE,
        DEXTERITY,
        PLACEHOLDER
    }

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingWorker(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    public abstract String getJobName();

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    public abstract AbstractJob createJob(CitizenData citizen);

    /**
     * Returns the worker of the current building.
     *
     * @return {@link CitizenData} of the current building
     */
    public CitizenData getWorker()
    {
        return worker;
    }

    /**
     * Override this method if you want to keep some items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @param stack the stack to decide on
     * @return true if the stack should remain in inventory
     */
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return false;
    }

    /**
     * Set the worker of the current building.
     *
     * @param citizen {@link CitizenData} of the worker
     */
    public void setWorker(final CitizenData citizen)
    {
        if (worker == citizen)
        {
            return;
        }

        // If we have a worker, it no longer works here
        if (worker != null)
        {
            final EntityCitizen tempCitizen = worker.getCitizenEntity();
            worker.setWorkBuilding(null);
            if(tempCitizen != null)
            {
                tempCitizen.setLastJob(getJobName());
            }
            setNeedsTool(ToolType.NONE, TOOL_LEVEL_HAND);
        }

        worker = citizen;

        // If we set a worker, inform it of such
        if (worker != null)
        {
            final EntityCitizen tempCitizen = citizen.getCitizenEntity();
            if(tempCitizen != null && !tempCitizen.getLastJob().equals(getJobName()))
            {
                citizen.resetExperienceAndLevel();
            }
            worker.setWorkBuilding(this);
        }

        markDirty();
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    public EntityCitizen getWorkerEntity()
    {
        if (worker == null)
        {
            return null;
        }
        return worker.getCitizenEntity();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_WORKER))
        {
            // Bypass setWorker, which marks dirty
            worker = getColony().getCitizen(compound.getInteger(TAG_WORKER));
            if (worker != null)
            {
                worker.setWorkBuilding(this);
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (worker != null)
        {
            compound.setInteger(TAG_WORKER, worker.getId());
        }
    }

    @Override
    public void onDestroyed()
    {
        if (hasWorker())
        {
            // EntityCitizen will detect the workplace is gone and fix up it's
            // Entity properly
            removeCitizen(worker);
        }

        super.onDestroyed();
    }

    /**
     * executed when a new day start.
     */
    public void onWakeUp()
    {
    }


    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    public boolean hasWorker()
    {
        return worker != null;
    }

    /**
     * Returns if the {@link CitizenData} is the same as {@link #worker}.
     *
     * @param citizen {@link CitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    public boolean isWorker(final CitizenData citizen)
    {
        return citizen == worker;
    }

    @Override
    public void removeCitizen(final CitizenData citizen)
    {
        if (isWorker(citizen))
        {
            setWorker(null);
        }
    }


    /**
     * Get the max tool level useable by the worker.
     *
     * @return the integer.
     */
    public int getMaxToolLevel()
    {
        if (getBuildingLevel()>=getMaxBuildingLevel())
        {
            return TOOL_LEVEL_MAXIMUM;
        }
        else if (getBuildingLevel() <= WOOD_HUT_LEVEL)
        {
            return TOOL_LEVEL_WOOD_OR_GOLD;
        }
        return getBuildingLevel()-WOOD_HUT_LEVEL;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        // If we have no active worker, grab one from the Colony
        // TODO Maybe the Colony should assign jobs out, instead?
        if (!hasWorker() && (getBuildingLevel() > 0 || this instanceof BuildingBuilder)
              && !this.getColony().isManualHiring())
        {
            final CitizenData joblessCitizen = getColony().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                setWorker(joblessCitizen);
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(worker == null ? 0 : worker.getId());
    }

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingHut.View
    {
        private int workerId;

        /**
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Returns the id of the worker.
         *
         * @return 0 if there is no worker else the correct citizen id.
         */
        public int getWorkerId()
        {
            return workerId;
        }

        /**
         * Sets the id of the worker.
         *
         * @param workerId the id to set.
         */
        public void setWorkerId(final int workerId)
        {
            this.workerId = workerId;
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);

            workerId = buf.readInt();
        }

        @NotNull
        public Skill getPrimarySkill()
        {
            return Skill.PLACEHOLDER;
        }

        @NotNull
        public Skill getSecondarySkill()
        {
            return Skill.PLACEHOLDER;
        }
    }
}
