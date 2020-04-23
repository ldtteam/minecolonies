package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutShepherd;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.network.messages.ShepherdSetDyeSheepsMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the Shepherd.
 */
public class BuildingShepherd extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String SHEPHERD = "shepherd";

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
    public BuildingShepherd(final IColony c, final BlockPos l)
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
    public Skill getPrimarySkill()
    {
        return Skill.Focus;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobShepherd(citizen);
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(dyeSheeps);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.shepherd;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        this.dyeSheeps = compound.getBoolean(NBT_DYE_SHEEPS);
        if (!compound.keySet().contains(NBT_DYE_SHEEPS))
        {
            this.dyeSheeps = true;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.putBoolean(NBT_DYE_SHEEPS, this.dyeSheeps);

        return compound;
    }

    /**
     * Returns current state of automatical sheep dyeing, true = enabled
     * @return true if so.
     */
    public boolean isDyeSheeps()
    {
        return dyeSheeps;
    }

    /**
     * Sets state of automatical sheep dyeing, true = enabled
     * @param dyeSheeps true if sheeps should be dyed.
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
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutShepherd(this);
        }

        /**
         * Called from button handler
         * @param dyeSheeps true if the sheeps should be dyed.
         */
        public void setDyeSheeps(final boolean dyeSheeps)
        {
            this.dyeSheeps = dyeSheeps;
            Network.getNetwork().sendToServer(new ShepherdSetDyeSheepsMessage(this));
        }

        /**
         * Returns current state of automatical sheep dyeing.
         * @return true if so.
         */
        public boolean isDyeSheeps()
        {
            return dyeSheeps;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            dyeSheeps = buf.readBoolean();
        }
    }
}
