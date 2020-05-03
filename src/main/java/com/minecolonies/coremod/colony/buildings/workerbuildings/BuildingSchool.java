package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.client.gui.WindowHutSchool;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobPupil;
import com.minecolonies.coremod.colony.jobs.JobTeacher;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Creates a new building for the school.
 */
public class BuildingSchool extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String SCHOOL = "school";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBT value to store the carpet pos.
     */
    private static final String TAG_CARPET  = "carpet";

    /**
     * If the school has a teacher.
     */
    private boolean hasTeacher = false;

    /**
     * List of carpets to sit on.
     */
    @NotNull
    private final List<BlockPos> carpet = new ArrayList<>();

    /**
     * Random obj for random calc.
     */
    private final Random random = new Random();

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingSchool(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHOOL;
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
        return "com.minecolonies.coremod.job.pupil";
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Knowledge;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Mana;
    }

    @Override
    public int getMaxInhabitants()
    {
        return 1 + 2 * getBuildingLevel();
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (citizen.isChild() || citizen.getJob() instanceof JobPupil)
        {
            if (getAssignedCitizen().size() + 1 >= getMaxInhabitants() && !hasTeacher)
            {
                return false;
            }
            return super.assignCitizen(citizen);
        }
        else if (hasTeacher)
        {
            return false;
        }

        if (super.assignCitizen(citizen))
        {
            markDirty();
            return hasTeacher = true;
        }
        return false;
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (citizen.getJob() instanceof JobTeacher)
        {
            hasTeacher = false;
            markDirty();
        }
        super.removeCitizen(citizen);
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        if (citizen.isChild())
        {
            return new JobPupil(citizen);
        }
        return new JobTeacher(citizen);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof CarpetBlock)
        {
            carpet.add(pos);
        }
    }


    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT carpetTagList = compound.getList(TAG_CARPET, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < carpetTagList.size(); ++i)
        {
            final CompoundNBT bedCompound = carpetTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(bedCompound, TAG_POS);
            if (!carpet.contains(pos))
            {
                carpet.add(pos);
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        if (!carpet.isEmpty())
        {
            @NotNull final ListNBT carpetTagList = new ListNBT();
            for (@NotNull final BlockPos pos : carpet)
            {
                final CompoundNBT carpetCompound = new CompoundNBT();
                BlockPosUtil.write(carpetCompound, NbtTagConstants.TAG_POS, pos);
                carpetTagList.add(carpetCompound);
            }
            compound.put(TAG_CARPET, carpetTagList);
        }

        return compound;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.school;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(hasTeacher);
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // School auto hiring
        if (!isFull() && ((getBuildingLevel() > 0 && isBuilt()))
              && (getHiringMode() == HiringMode.DEFAULT && !this.getColony().isManualHiring() || getHiringMode() == HiringMode.AUTO))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.getWorkBuilding() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData: getAssignedCitizen())
        {
            if (citizenData.getJob() instanceof JobPupil && !citizenData.isChild())
            {
                removeCitizen(citizenData);
            }
        }
    }

    /**
     * Get a random place to sit from the school.
     * @return the place to sit.
     */
    @Nullable
    public BlockPos getRandomPlaceToSit()
    {
        if (carpet.isEmpty())
        {
            return null;
        }
        final BlockPos returnPos = carpet.get(random.nextInt(carpet.size()));
        if (colony.getWorld().getBlockState(returnPos).getBlock() instanceof CarpetBlock)
        {
            return returnPos;
        }
        carpet.remove(returnPos);
        return null;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("School", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Check if this building has a teacher.
         */
        public boolean hasTeacher;

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
            return new WindowHutSchool(this, SCHOOL);
        }

        @Override
        public boolean canAssign(final ICitizenDataView citizenDataView)
        {
            if (citizenDataView.isChild())
            {
                return super.canAssign(citizenDataView);
            }
            return !hasTeacher;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            this.hasTeacher = buf.readBoolean();
        }
    }
}
