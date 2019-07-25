package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowHutCowboy;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCowboy;
import com.minecolonies.coremod.network.messages.CowboySetMilkCowsMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the Cowboy.
 */
public class BuildingCowboy extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String COWBOY          = "Cowboy";

    /**
     * NBT Tag for milkCows boolean.
     */
    private static final String NBT_MILK_COWS = "MILK_COWS";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Milk Cows or not.
     */
    private boolean milkCows = true;

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingCowboy(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COWBOY;
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
        return COWBOY;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobCowboy(citizen);
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(milkCows);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);
        compound.putBoolean(NBT_MILK_COWS, this.milkCows);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        this.milkCows = compound.getBoolean(NBT_MILK_COWS);
    }

    public boolean isMilkingCows()
    {
        return milkCows;
    }

    public void setMilkCows(final boolean milkCows)
    {
        this.milkCows = milkCows;
        markDirty();
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Milk Cows or not.
         */
        private boolean milkCows = true;

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
            return new WindowHutCowboy(this);
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

        public void setMilkCows(final boolean milkCows)
        {
            MineColonies.getNetwork().sendToServer(new CowboySetMilkCowsMessage(this, milkCows));
            this.milkCows = milkCows;
        }

        public boolean isMilkCows()
        {
            return milkCows;
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            milkCows = buf.readBoolean();
        }
    }
}
