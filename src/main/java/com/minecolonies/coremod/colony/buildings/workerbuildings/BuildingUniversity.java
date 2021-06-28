package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.coremod.client.gui.huts.WindowHutUniversityModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobResearch;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BOOKCASES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.RESEARCH_CONCLUDED;

/**
 * Creates a new building for the university.
 */
public class BuildingUniversity extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String UNIVERSITY = "university";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL           = 5;

    /**
     * Offline processing level cap.
     */
    private static final int OFFLINE_PROCESSING_LEVEL_CAP = 3;

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> bookCases = new ArrayList<>();

    /**
     * Random obj for random calc.
     */
    private final Random random = new Random();

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingUniversity(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return UNIVERSITY;
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
        return "com.minecolonies.coremod.job.researcher";
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT furnaceTagList = compound.getList(TAG_BOOKCASES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            bookCases.add(NBTUtil.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT bookcaseTagList = new ListNBT();
        for (@NotNull final BlockPos entry : bookCases)
        {
            @NotNull final CompoundNBT bookCompound = new CompoundNBT();
            bookCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            bookcaseTagList.add(bookCompound);
        }
        compound.put(TAG_BOOKCASES, bookcaseTagList);

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        //todo we might in the future want to add our own oredict tag to this.
        if (block == Blocks.BOOKSHELF)
        {
            bookCases.add(pos);
        }
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Returns a random bookshelf from the list.
     *
     * @return the position of it.
     */
    public BlockPos getRandomBookShelf()
    {
        if (bookCases.isEmpty())
        {
            return getPosition();
        }
        final BlockPos returnPos = bookCases.get(random.nextInt(bookCases.size()));
        if (colony.getWorld().getBlockState(returnPos).getBlock() == Blocks.BOOKSHELF)
        {
            return returnPos;
        }
        bookCases.remove(returnPos);
        return getPosition();
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobResearch(citizen);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.university;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);

        final List<ILocalResearch> inProgress = colony.getResearchManager().getResearchTree().getResearchInProgress();

        int i = 1;
        for (final ILocalResearch research : inProgress)
        {
            if (i > getAssignedCitizen().size())
            {
                return;
            }

            for (final ICitizenData data : getAssignedCitizen())
            {
                data.getCitizenSkillHandler().addXpToSkill(getSecondarySkill(), 25.0, data);
            }

            if (colony.getResearchManager()
                  .getResearchTree()
                  .getResearch(research.getBranch(), research.getId())
                  .research(colony.getResearchManager().getResearchEffects(), colony.getResearchManager().getResearchTree()))
            {
                onSuccess(research);
            }
            i++;
        }
    }

    /**
     * Called on successfully concluding a research.
     * @param research the concluded research.
     */
    public void onSuccess(final ILocalResearch research)
    {
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.applyResearchEffects();
        }

        final TranslationTextComponent message = new TranslationTextComponent(RESEARCH_CONCLUDED + ThreadLocalRandom.current().nextInt(3),
         IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName());

        for(PlayerEntity player : colony.getMessagePlayerEntities())
        {
            player.sendMessage(message, player.getUniqueID());
        }
        colony.getResearchManager().checkAutoStartResearch();
        this.markDirty();
    }

    @Override
    public void processOfflineTime(final long time)
    {
        if (getBuildingLevel() >= OFFLINE_PROCESSING_LEVEL_CAP && time > 0)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.researcher.moreknowledge");
            for (final ICitizenData citizenData : getAssignedCitizen())
            {
                if (citizenData.getJob() != null)
                {
                    citizenData.getJob().processOfflineTime(time);
                }
            }
        }
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Instantiates the view of the building.
         *
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
            return new WindowHutUniversityModule(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
        }

        /**
         * Check if it has enough workers.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return getWorkerId().size() >= getBuildingLevel();
        }
    }
}
