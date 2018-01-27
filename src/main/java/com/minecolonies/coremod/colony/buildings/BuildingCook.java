package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuildingWorker
{
    /**
     * The cook string.
     */
    private static final String COOK_DESC = "Cook";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Tag to store the furnace position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the furnace list.
     */
    private static final String TAG_FURNACES = "furnaces";

    /**
     * Tag to store status of ovens to NBT.
     */
    private static final String TAG_COOKING = "cooking";

    /**
     * List of registered furnaces.
     */
    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Is true when the Cook put something in the oven.
     */
    private boolean isOvenFull = true;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(ItemStackUtils.ISFOOD, Integer.MAX_VALUE);
        keepX.put(ItemStackUtils.ISCOOKABLE, Integer.MAX_VALUE);
        keepX.put(TileEntityFurnace::isItemFuel, Integer.MAX_VALUE);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COOK_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobCook(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COOK_DESC;
    }

    /**
     * Return a list of furnaces assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getFurnaces()
    {
        return new ArrayList<>(furnaces);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : furnaces)
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            furnaceCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            furnacesTagList.appendTag(furnaceCompound);
        }
        compound.setTag(TAG_FURNACES, furnacesTagList);
        compound.setBoolean(TAG_COOKING, isOvenFull);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList furnaceTagList = compound.getTagList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            furnaces.add(NBTUtil.getPosFromTag(furnaceTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
        isOvenFull = compound.getBoolean(TAG_COOKING);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof BlockFurnace && !furnaces.contains(pos))
        {
            furnaces.add(pos);
        }
        markDirty();
    }

    /**
     * Check if something is in the oven of th ecook.
     *
     * @return true if so.
     */
    public boolean isSomethingInOven()
    {
        return isOvenFull;
    }

    /**
     * Set that something is in the oven.
     *
     * @param set true or false
     */
    public void setIsSomethingInOven(final boolean set)
    {
        isOvenFull = set;
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiate the cook view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, COOK_DESC);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.CHARISMA;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.INTELLIGENCE;
        }
    }
}
