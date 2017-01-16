package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuildingHut
{
    /**
     * A list of ItemStacks with needed items and their quantity.
     * This list is a diff between itemsNeeded in AbstractEntityAiBasic and
     * the players inventory and their hut combined.
     * So look here for what is currently still needed
     * to fulfill the workers needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    @NotNull
    private List<ItemStack> itemsCurrentlyNeeded = new ArrayList<>();

    /**
     * A list which contains the position of all containers which belong to the worker building.
     */
    private final List<BlockPos> containerList = new ArrayList<>();

    /**
     * This flag tells if we need a shovel, will be set on tool needs.
     */
    private boolean needsShovel = false;

    /**
     * This flag tells if we need an axe, will be set on tool needs.
     */
    private boolean needsAxe = false;

    /**
     * This flag tells if we need a hoe, will be set on tool needs.
     */
    private boolean needsHoe = false;

    /**
     * This flag tells if we need a pickaxe, will be set on tool needs.
     */
    private boolean needsPickaxe = false;

    /**
     * This flag tells if we need a weapon, will be set on tool needs.
     */
    private boolean needsWeapon = false;

    /**
     * The minimum pickaxe level we need to fulfill the tool request.
     */
    private int needsPickaxeLevel = -1;

    /**
     * Tag used to store the containers to NBT.
     */
    private static final String TAG_CONTAINERS = "Containers";

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

        final NBTTagList containerTagList = compound.getTagList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.tagCount(); ++i)
        {
            final NBTTagCompound containerCompound = containerTagList.getCompoundTagAt(i);
            @Nullable final BlockPos pos = NBTUtil.getPosFromTag(containerCompound);
            if (pos != null)
            {
                containerList.add(pos);
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

        @NotNull final NBTTagList containerTagList = new NBTTagList();
        for (@NotNull final BlockPos pos: containerList)
        {
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag(TAG_CONTAINERS, containerTagList);
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
     * Add a new container to the building.
     * @param pos position to add.
     */
    public void addContainerPosition(BlockPos pos)
    {
        containerList.add(pos);
    }

    /**
     * Remove a container from the building.
     * @param pos position to remove.
     */
    public void removeContainerPosition(BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     * @return a unmodifiable list of the container positions.
     */
    public List<BlockPos> getAdditionalCountainers()
    {
        return Collections.unmodifiableList(containerList);
    }

    //------------------------- Starting Required Tools/Item handling -------------------------//

    /**
     * Check if any items are needed at the moment.
     * @return true if so.
     */
    public boolean areItemsNeeded()
    {
        return !itemsCurrentlyNeeded.isEmpty();
    }

    /**
     * Check if the worker requires a shovel.
     * @return true if so.
     */
    public boolean needsShovel()
    {
        return needsShovel;
    }

    /**
     * Check if the worker requires a axe.
     * @return true if so.
     */
    public boolean needsAxe()
    {
        return needsAxe;
    }

    /**
     * Check if the worker requires a hoe.
     * @return true if so.
     */
    public boolean needsHoe()
    {
        return needsHoe;
    }

    /**
     * Check if the worker requires a pickaxe.
     * @return true if so.
     */
    public boolean needsPickaxe()
    {
        return needsPickaxe;
    }

    /**
     * Check if the worker requires a weapon.
     * @return true if so.
     */
    public boolean needsWeapon()
    {
        return needsWeapon;
    }

    /**
     * Check the required pickaxe level..
     * @return the mining level of the pickaxe.
     */
    public int getNeededPickaxeLevel()
    {
        return needsPickaxeLevel;
    }

    /**
     * Set if the worker needs a shovel.
     * @param needsShovel true or false.
     */
    public void setNeedsShovel(final boolean needsShovel)
    {
        this.needsShovel = needsShovel;
    }

    /**
     * Set if the worker needs a axe.
     * @param needsShovel true or false.
     */
    public void setNeedsAxe(final boolean needsAxe)
    {
        this.needsAxe = needsAxe;
    }

    /**
     * Set if the worker needs a hoe.
     * @param needsShovel true or false.
     */
    public void setNeedsHoe(final boolean needsHoe)
    {
        this.needsHoe = needsHoe;
    }

    /**
     * Set if the worker needs a pickaxe.
     * @param needsShovel true or false.
     */
    public void setNeedsPickaxe(final boolean needsPickaxe)
    {
        this.needsPickaxe = needsPickaxe;
    }

    /**
     * Set if the worker needs a weapon.
     * @param needsShovel true or false.
     */
    public void setNeedsWeapon(final boolean needsWeapon)
    {
        this.needsWeapon = needsWeapon;
    }

    /**
     * Add a neededItem to the currentlyNeededItem list.
     * @param stack the stack to add.
     */
    public void addNeededItems(@Nullable ItemStack stack)
    {
        if(stack != null)
        {
            itemsCurrentlyNeeded.add(stack);
        }
    }

    /**
     * Getter for the neededItems.
     * @return an unmodifiable list.
     */
    public List<ItemStack> getNeededItems()
    {
        return Collections.unmodifiableList(itemsCurrentlyNeeded);
    }

    /**
     * Getter for the first of the currentlyNeededItems.
     * @return copy of the itemStack.
     */
    @Nullable
    public ItemStack getFirstNeededItem()
    {
        if(itemsCurrentlyNeeded.isEmpty())
        {
            return null;
        }
        return itemsCurrentlyNeeded.get(0).copy();
    }

    /**
     * Clear the currentlyNeededItem list.
     */
    public void clearNeededItems()
    {
        itemsCurrentlyNeeded.clear();
    }

    /**
     * Overwrite the itemsCurrentlyNeededList with a new one.
     * @param newList the new list to set.
     */
    public void setItemsCurrentlyNeeded(@NotNull List<ItemStack> newList)
    {
        this.itemsCurrentlyNeeded = new ArrayList<>(newList);
    }

    /**
     * Set the needed pickaxe level of the worker.
     * @param needsShovel the mining level.
     */
    public void setNeedsPickaxeLevel(final int needsPickaxeLevel)
    {
        this.needsPickaxeLevel = needsPickaxeLevel;
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

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
