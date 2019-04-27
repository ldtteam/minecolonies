package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Building class for the Barracks.
 */
public class BuildingBarracks extends AbstractBuilding
{
    /**
     * Name of our building's Schematics.
     */
    private static final String SCHEMATIC_NAME = "Barracks";

    /**
     * Max hut level of the Barracks.
     */
    private static final int BARRACKS_HUT_MAX_LEVEL = 5;

    /**
     * The tag to store the tower list to NBT.
     */
    private static final String TAG_TOWERS = "towers";

    /**
     * The list of barracksTowers.
     */
    private final List<BlockPos> towers = new ArrayList<>();

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    public BuildingBarracks(@NotNull final Colony colony, final BlockPos pos)
    {
        super(colony, pos);
    }

    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return BARRACKS_HUT_MAX_LEVEL;
    }

    @Override
    public void onDestroyed()
    {
        final World world = getColony().getWorld();

        if (world != null)
        {
            for (final BlockPos tower : towers)
            {
                world.setBlockState(tower, Blocks.AIR.getDefaultState());
            }
        }
        super.onDestroyed();
    }

    @Override
    public void registerBlockPosition(@NotNull final IBlockState block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == ModBlocks.blockBarracksTowerSubstitution)
        {
            if (world.getBlockState(pos).getBlock() != ModBlocks.blockHutBarracksTower)
            {
                world.setBlockState(pos, ModBlocks.blockHutBarracksTower.getDefaultState().withProperty(BlockHorizontal.FACING, block.getValue(BlockHorizontal.FACING)));
                getColony().getBuildingManager().addNewBuilding((TileEntityColonyBuilding) world.getTileEntity(pos), world);
            }
            final AbstractBuilding building = getColony().getBuildingManager().getBuilding(pos);
            if (building instanceof BuildingBarracksTower)
            {
                building.setStyle(this.getStyle());
                ((BuildingBarracksTower) building).addBarracks(getLocation());
                if (!towers.contains(pos))
                {
                    towers.add(pos);
                }
            }
        }
    }

    @Override
    public int getClaimRadius()
    {
        int sum = getBuildingLevel();
        for (final BlockPos pos : towers)
        {
            final AbstractBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                sum += building.getBuildingLevel();
            }
        }
        return Math.max(1, sum / getMaxBuildingLevel());
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        towers.clear();
        towers.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_TOWERS, Constants.NBT.TAG_COMPOUND))
                        .map(resultCompound -> BlockPosUtil.readFromNBT(resultCompound, TAG_POS))
                        .collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        final NBTTagList towerTagList = towers.stream().map(pos -> BlockPosUtil.writeToNBT(new NBTTagCompound(), TAG_POS, pos)).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_TOWERS, towerTagList);
    }

    /**
     * Barracks building View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the barracks view.
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
            return new WindowBarracksBuilding(this);
        }
    }
}
