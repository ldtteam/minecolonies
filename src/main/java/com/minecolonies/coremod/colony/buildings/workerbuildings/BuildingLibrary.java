package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobStudent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBookshelf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BOOKCASES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Creates a new building for the Library.
 */
public class BuildingLibrary extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String STUDENT = "Student";

    /**
     * Description of the block used to set this block.
     */
    private static final String LIBRARY_HUT_NAME = "Library";

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> bookCases = new ArrayList<>();

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Random obj for random calc.
     */
    private final Random random = new Random();

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingLibrary(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return LIBRARY_HUT_NAME;
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
        return STUDENT;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobStudent(citizen);
    }

    @Override
    public boolean hasAssignedCitizen()
    {
        return getAssignedCitizen().size() >= getBuildingLevel() * 2;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList bookcaseTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : bookCases)
        {
            @NotNull final NBTTagCompound bookCompound = new NBTTagCompound();
            bookCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            bookcaseTagList.appendTag(bookCompound);
        }
        compound.setTag(TAG_BOOKCASES, bookcaseTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList furnaceTagList = compound.getTagList(TAG_BOOKCASES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            bookCases.add(NBTUtil.getPosFromTag(furnaceTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if(block instanceof BlockBookshelf)
        {
            bookCases.add(pos);
        }
    }

    /**
     * Returns a random bookshelf from the list.
     * @return the position of it.
     */
    public BlockPos getRandomBookShelf()
    {
        if (bookCases.isEmpty())
        {
            return getLocation();
        }
        final BlockPos returnPos = bookCases.get(random.nextInt(bookCases.size()));
        if ((colony.getWorld().getBlockState(returnPos).getBlock() instanceof BlockBookshelf))
        {
            return returnPos;
        }
        bookCases.remove(returnPos);
        return getLocation();
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
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
            return new WindowHutWorkerPlaceholder<AbstractBuildingWorker.View>(this, LIBRARY_HUT_NAME);
        }

        /**
         * Check if it has enough workers.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return getWorkerId().size() >= getBuildingLevel() * 2;
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
    }
}
