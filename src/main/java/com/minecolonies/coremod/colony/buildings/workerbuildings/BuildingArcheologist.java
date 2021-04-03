package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuildingArcheologist extends AbstractBuildingStructureBuilder
{

    /**
     * Description of the job executed in the hut.
     */
    private static final String ARCHEOLOGIST = "archeologist";

    /**
     * The current dig side or empty if none is selected or active.
     */
    private Optional<BlockPos> currentDigSide = Optional.empty();

    /**
     * Indicates if the current dig side should be automatically repeated
     * when it is completed.
     *
     * This will be automatically set to false when the current dig side is considered to be
     * empty.
     */
    private boolean automaticallyResending = false;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingArcheologist(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return null;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return null;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return ARCHEOLOGIST;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Adaptability;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Agility;
    }

    @Override
    public String getSchematicName()
    {
        return ARCHEOLOGIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        if (compound.contains(NbtTagConstants.TAG_DIG_SIDE))
        {
            this.currentDigSide = Optional.of(BlockPosUtil.read(compound, NbtTagConstants.TAG_DIG_SIDE));
        }
        else
        {
            this.currentDigSide = Optional.empty();
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();

        this.currentDigSide.ifPresent(pos -> BlockPosUtil.write(nbt, NbtTagConstants.TAG_DIG_SIDE, pos));

        return nbt;
    }

    @Override
    public void searchWorkOrder()
    {

    }

    public Optional<BlockPos> getCurrentDigSide()
    {
        return currentDigSide;
    }

    public void setCurrentDigSide(final BlockPos currentDigSide)
    {
        this.currentDigSide = Optional.of(currentDigSide);
    }

    public void clearCurrentDigSide()
    {
        this.currentDigSide = Optional.empty();
    }

    public boolean isAutomaticallyResending()
    {
        return automaticallyResending;
    }

    public void setAutomaticallyResending(final boolean automaticallyResending)
    {
        this.automaticallyResending = automaticallyResending;
    }

    public void onUnsuccessfulDig()
    {
        this.clearCurrentDigSide();
        this.setAutomaticallyResending(false);
    }

    public static class View extends AbstractBuildingWorker.View
    {

        /**
         * The current dig side or empty if none is selected or active.
         */
        private Optional<BlockPos> currentDigSide = Optional.empty();

        /**
         * Indicates if the current dig side should be automatically repeated
         * when it is completed.
         *
         * This will be automatically set to false when the current dig side is considered to be
         * empty.
         */
        private boolean automaticallyResending = false;

        /**
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        public Optional<BlockPos> getCurrentDigSide()
        {
            return currentDigSide;
        }

        public void setCurrentDigSide(final BlockPos currentDigSide)
        {
            this.currentDigSide = Optional.of(currentDigSide);
        }

        public void clearCurrentDigSide()
        {
            this.currentDigSide = Optional.empty();
        }

        public boolean isAutomaticallyResending()
        {
            return automaticallyResending;
        }

        public void setAutomaticallyResending(final boolean automaticallyResending)
        {
            this.automaticallyResending = automaticallyResending;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);

            if (buf.readBoolean())
            {
                this.currentDigSide = Optional.of(buf.readBlockPos());
            }
            else
            {
                this.currentDigSide = Optional.empty();
            }

            this.automaticallyResending = buf.readBoolean();
        }
    }
}
