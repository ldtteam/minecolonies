package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
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
public class BuildingUniversity extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String UNIVERSITY = "university";

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

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);

        final List<ILocalResearch> inProgress = colony.getResearchManager().getResearchTree().getResearchInProgress();
        final WorkerBuildingModule module = getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.researcher);

        int i = 1;
        for (final ILocalResearch research : inProgress)
        {
            if (i > module.getAssignedCitizen().size())
            {
                return;
            }

            for (final ICitizenData data : getAllAssignedCitizen())
            {
                data.getCitizenSkillHandler().addXpToSkill(module.getSecondarySkill(), 25.0, data);
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
            player.sendMessage(message, player.getUUID());
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
            for (final ICitizenData citizenData : getAllAssignedCitizen())
            {
                if (citizenData.getJob() != null)
                {
                    citizenData.getJob().processOfflineTime(time);
                }
            }
        }
    }
}
