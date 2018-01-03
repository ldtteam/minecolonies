package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.BlockHutBarracksTower;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Building for the Barracks.
 */
public class BuildingBarracks extends AbstractBuilding
{
    /**
     * General Barracks description key.
     */
    private static final String BARRACKS = "Barracks";

    /**
     * Max hut level of the Barracks.
     */
    private static final int BARRACKS_HUT_MAX_LEVEL = 5;

    /**
     * Tower position offset.
     */
    private static final int TOWER_OFFSET = 8;

    /**
     * Constructor for the Barracks building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBarracks(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Barracks schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BARRACKS;
    }

    @Override
    public void onDestroyed()
    {
        final World world = getColony().getWorld();
        if (world != null)
        {
            for (int i = 1; i <= this.getBuildingLevel(); i++)
            {
                final Tuple<BlockPos, EnumFacing> tuple = getPositionAndFacingForLevel(i);
                world.setBlockState(tuple.getFirst(), Blocks.AIR.getDefaultState());
            }
        }
        super.onDestroyed();
    }

    /**
     * Gets the max level of the Barracks's hut.
     *
     * @return The max level of the Barracks's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BARRACKS_HUT_MAX_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        final World world = getColony().getWorld();
        if (world != null)
        {
            for (int i = 1; i <= newLevel && i < BARRACKS_HUT_MAX_LEVEL; i++)
            {
                final Tuple<BlockPos, EnumFacing> tuple = getPositionAndFacingForLevel(i);

                if (!(world.getBlockState(tuple.getFirst()).getBlock() instanceof BlockHutBarracksTower))
                {
                    world.setBlockState(tuple.getFirst(), ModBlocks.blockHutBarracksTower.getDefaultState().withProperty(BlockHutBarracksTower.FACING, tuple.getSecond()));
                    getColony().getBuildingManager().addNewBuilding((TileEntityColonyBuilding) world.getTileEntity(tuple.getFirst()), world);

                    final AbstractBuilding building = getColony().getBuildingManager().getBuilding(tuple.getFirst());
                    if (building instanceof BuildingBarracksTower)
                    {
                        building.setStyle(this.getStyle());
                        ((BuildingBarracksTower) building).addBarracks(getLocation());
                    }
                }
            }
        }
        super.onUpgradeComplete(newLevel);
    }

    /**
     * Calculate position and facing of the tower to add.
     *
     * @param level the level of the barracks.
     * @return a tuple with position and facing.
     */
    private final Tuple<BlockPos, EnumFacing> getPositionAndFacingForLevel(final int level)
    {

        BlockPos position = getLocation();
        int tempLevel = level;

        if (isMirrored())
        {
            tempLevel += Constants.ROTATE_ONCE;
        }

        switch (getRotation())
        {
            case Constants.ROTATE_ONCE:
                tempLevel += Constants.ROTATE_THREE_TIMES;
                break;
            case Constants.ROTATE_TWICE:
                tempLevel += Constants.ROTATE_ONCE;
                break;
            case Constants.ROTATE_THREE_TIMES:
                tempLevel += Constants.ROTATE_TWICE;
                break;
            default:
                //do nothing
        }

        if (tempLevel > Constants.MAX_ROTATIONS)
        {
            tempLevel -= Constants.MAX_ROTATIONS;
        }

        EnumFacing facing = EnumFacing.NORTH;

        final int offset = getStyle().toLowerCase(Locale.ENGLISH).contains("birch") ? (TOWER_OFFSET + 1) : TOWER_OFFSET;

        switch (tempLevel)
        {
            case Constants.ROTATE_ONCE:
                position = position.offset(EnumFacing.SOUTH, offset).offset(EnumFacing.WEST, offset);
                break;
            case Constants.ROTATE_TWICE:
                position = position.offset(EnumFacing.NORTH, offset).offset(EnumFacing.EAST, offset);
                facing = EnumFacing.SOUTH;
                break;
            case Constants.ROTATE_THREE_TIMES:
                position = position.offset(EnumFacing.SOUTH, offset).offset(EnumFacing.EAST, offset);
                facing = EnumFacing.WEST;
                break;
            case Constants.MAX_ROTATIONS:
                position = position.offset(EnumFacing.NORTH, offset).offset(EnumFacing.WEST, offset);
                facing = EnumFacing.EAST;
                break;
            default:
                //do nothing
        }

        return new Tuple<>(position, facing);
    }

    /**
     * BuildingDeliveryman View.
     */
    public static class View extends AbstractBuildingHut.View
    {
        /**
         * Instantiate the deliveryman view.
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
