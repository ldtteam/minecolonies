package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecoloniesSeat;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.EntityCitizen;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CHAIRS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

import java.util.ArrayList;
import java.util.List;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuildingFurnaceUser
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
     * List of registered chairs.
     */
    private final List<BlockPos> chairs = new ArrayList<>();

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(ItemStackUtils.ISFOOD, STACKSIZE);
        keepX.put(ItemStackUtils.ISCOOKABLE, STACKSIZE);
        keepX.put(TileEntityFurnace::isItemFuel, STACKSIZE);
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

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_CHAIRS))
        {
            final NBTTagList furnaceTagList = compound.getTagList(TAG_CHAIRS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < furnaceTagList.tagCount(); ++i)
            {
                chairs.add(NBTUtil.getPosFromTag(furnaceTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : chairs)
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            furnaceCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            furnacesTagList.appendTag(furnaceCompound);
        }
        compound.setTag(TAG_CHAIRS, furnacesTagList);
    }

    /**
     * Return a list of chairs assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getChairs(final EntityCitizen citizen)
    {
        if (Configurations.gameplay.restaurantSittingRequired && chairs.size() == 0)
        {
            recalculateChairs(citizen);
        }
        return new ArrayList<>(chairs);
    }
    
    
    /**
     * Function will search the schematic area of the building and
     * look for a chairs and create a list of all chairs located.
     *
     * @param citizen citizen that is searching for chairs.
     */
    public void recalculateChairs(final EntityCitizen citizen)
    {
        final World world = CompatibilityUtils.getWorld(citizen);
        AxisAlignedBB range = this.getTargetableArea(world);
        
        for (int y = (int)range.minY; y < range.maxY; y++)
        {
            for (int x = (int)range.minX; x < range.maxX; x++)
            {
                for (int z = (int)range.minZ; z < range.maxZ; z++)
                {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final Block block = world.getBlockState(pos).getBlock();
                    if (block instanceof IBlockMinecoloniesSeat)
                    {
                        chairs.add(pos);
                    }
                }
            }
        }
        
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof IBlockMinecoloniesSeat && !chairs.contains(pos))
        {
            chairs.add(pos);
        }
        markDirty();
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
