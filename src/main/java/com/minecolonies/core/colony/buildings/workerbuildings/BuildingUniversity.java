package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BOOKCASES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_RESEARCHERS_MORE_KNOWLEDGE;
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
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag furnaceTagList = compound.getList(TAG_BOOKCASES, Tag.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            bookCases.add(NbtUtils.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final ListTag bookcaseTagList = new ListTag();
        for (@NotNull final BlockPos entry : bookCases)
        {
            @NotNull final CompoundTag bookCompound = new CompoundTag();
            bookCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            bookcaseTagList.add(bookCompound);
        }
        compound.put(TAG_BOOKCASES, bookcaseTagList);

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.defaultBlockState().is(Tags.Blocks.BOOKSHELVES))
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
        final BlockPos returnPos = bookCases.get(MathUtils.RANDOM.nextInt(bookCases.size()));
        if (colony.getWorld().getBlockState(returnPos).is(Tags.Blocks.BOOKSHELVES))
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
        final WorkerBuildingModule module = getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.researcher.get());

        int i = 1;
        for (final ILocalResearch research : inProgress)
        {
            if (i > module.getAssignedCitizen().size())
            {
                return;
            }

            if (colony.getResearchManager()
                  .getResearchTree()
                  .getResearch(research.getBranch(), research.getId())
                  .research(colony.getResearchManager().getResearchEffects(), colony.getResearchManager().getResearchTree()))
            {
                onSuccess(research);
            }
            colony.getResearchManager().markDirty();
            i++;
        }
    }

    /**
     * Called on successfully concluding a research.
     *
     * @param research the concluded research.
     */
    public void onSuccess(final ILocalResearch research)
    {
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.applyResearchEffects();
        }

        final MutableComponent message = Component.translatable(RESEARCH_CONCLUDED + ThreadLocalRandom.current().nextInt(3),
          MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()));

        MessageUtils.format(message).sendTo(colony).forManagers();
        colony.getResearchManager().checkAutoStartResearch();
        this.markDirty();
    }

    @Override
    public void processOfflineTime(final long time)
    {
        if (getBuildingLevel() >= OFFLINE_PROCESSING_LEVEL_CAP && time > 0)
        {
            MessageUtils.format(MESSAGE_RESEARCHERS_MORE_KNOWLEDGE).sendTo(colony).forAllPlayers();
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
