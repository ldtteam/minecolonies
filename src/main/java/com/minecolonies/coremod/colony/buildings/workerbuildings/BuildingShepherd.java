package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowHutShepherd;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.network.messages.ShepherdSetDyeSheepsMessage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import io.netty.buffer.ByteBuf;

/**
 * Creates a new building for the Shepherd.
 */
public class BuildingShepherd extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String SHEPHERD = "Shepherd";

    /**
     * NBT Tag for dyeSheeps boolean.
     */
    private static final String NBT_DYE_SHEEPS = "autoDye";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Dyes sheeps randomly
     */
    private boolean dyeSheeps = false;

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingShepherd(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SHEPHERD;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SHEPHERD;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobShepherd(citizen);
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(dyeSheeps);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setBoolean(NBT_DYE_SHEEPS, this.dyeSheeps);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.dyeSheeps = compound.getBoolean(NBT_DYE_SHEEPS);
        if (!compound.hasKey(NBT_DYE_SHEEPS))
        {
            this.dyeSheeps = true;
        }
    }

    /**
     * Returns current state of automatical sheep dyeing, true = enabled
     */
    public boolean isDyeSheeps()
    {
        return dyeSheeps;
    }

    /**
     * Sets state of automatical sheep dyeing, true = enabled
     */
    public void setDyeSheeps(final boolean dyeSheeps)
    {
        this.dyeSheeps = dyeSheeps;
        markDirty();
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Dye sheeps automatically or not.
         */
        private boolean dyeSheeps = false;

        /**
         * Instantiates the view of the building.
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutShepherd(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }

        /**
         * Called from button handler
         */
        public void setDyeSheeps(final boolean dyeSheeps)
        {
            this.dyeSheeps = dyeSheeps;
            MineColonies.getNetwork().sendToServer(new ShepherdSetDyeSheepsMessage(this));
        }

        /**
         * Returns current state of automatical sheep dyeing, true = enabled
         */
        public boolean isDyeSheeps()
        {
            return dyeSheeps;
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            dyeSheeps = buf.readBoolean();
        }
    }
}
