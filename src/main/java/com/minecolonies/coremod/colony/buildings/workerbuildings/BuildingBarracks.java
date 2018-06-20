package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.BlockBarracksTowerSubstitution;
import com.minecolonies.coremod.blocks.huts.BlockHutBarracks;
import com.minecolonies.coremod.blocks.huts.BlockHutBarracksTower;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
     * Tower position offset.
     */
    private static final int TOWER_OFFSET = 8;

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
        return 5;
    }

    @Override
    public void onDestroyed()
    {
        final World world = getColony().getWorld();

        if (world != null)
        {
            for (int i = 1; i <= getBuildingLevel() && i < BARRACKS_HUT_MAX_LEVEL; i++)
            {
                final Tuple<BlockPos, EnumFacing> tuple = getPositionAndFacingForLevel(i);

                if (world.getBlockState(tuple.getFirst()).getBlock() instanceof BlockHutBarracksTower)
                {
                    world.setBlockState(tuple.getFirst(), Blocks.AIR.getDefaultState());
                }
            }
        }

        super.onDestroyed();
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
                }
                final AbstractBuilding building = getColony().getBuildingManager().getBuilding(tuple.getFirst());
                if (building instanceof BuildingBarracksTower)
                {
                    building.setStyle(this.getStyle());
                    ((BuildingBarracksTower) building).addBarracks(getLocation());
                }
            }

            //TODO: Implement the following properly. Once we've got textures and etc for a Substitution block.
            /*for (final Tuple<BlockPos, EnumFacing> tower : getBarracksTowers())
            {

                if (world.getBlockState(tower.getFirst()).getBlock() instanceof BlockBarracksTowerSubstitution)
                {
                    world.setBlockState(tower.getFirst(), ModBlocks.blockHutBarracksTower.getDefaultState().withProperty(BlockHutBarracksTower.FACING, tower.getSecond()));

                    final TileEntity barracksTowerEntity = world.getTileEntity(tower.getFirst());

                    if (barracksTowerEntity != null)
                    {
                        getColony().getBuildingManager().addNewBuilding((TileEntityColonyBuilding) barracksTowerEntity, world);
                    }
                }

                final AbstractBuilding building = getColony().getBuildingManager().getBuilding(tower.getFirst());
                if (building instanceof BuildingBarracksTowerNew)
                {
                    building.setStyle(this.getStyle());
                    ((BuildingBarracksTowerNew) building).addBarracks(getLocation());
                }
            }*/
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
     * Return list of all the Barrack's Towers.
     *
     * @return a tuple with position and facing.
     */
    private List<Tuple<BlockPos, EnumFacing>> getBarracksTowers()
    {
        final StructureName sn =
          new StructureName(
            Structures.SCHEMATICS_PREFIX,
            getStyle(),
            getSchematicName() + getBuildingLevel());

        final String structureName = sn.toString();
        final StructureWrapper wrapper = new StructureWrapper(getColony().getWorld(), structureName);

        BlockPos barracksPos = null;
        final List<Template.BlockInfo> barracksTowers = new ArrayList<>();

        for (final Template.BlockInfo block : wrapper.getStructure().getStructure().getTemplate().blocks)
        {
            if (block.blockState.getBlock() instanceof BlockHutBarracks)
            {
                barracksPos = block.pos;
            }

            if (block.blockState.getBlock() instanceof BlockBarracksTowerSubstitution)
            {
                barracksTowers.add(block);
            }
        }

        final List<Tuple<BlockPos, EnumFacing>> towers = new ArrayList<>();

        if (barracksPos != null)
        {
            for (final Template.BlockInfo block : barracksTowers)
            {
                final int xDif = barracksPos.getX() - block.pos.getX();
                final int yDif = barracksPos.getY() - block.pos.getY();
                final int zDif = barracksPos.getZ() - block.pos.getZ();

                final int towerX = getLocation().getX() + xDif;
                final int towerY = getLocation().getY() - yDif;
                final int towerZ = getLocation().getZ() + zDif;

                final BlockPos towerPos = new BlockPos(towerX, towerY, towerZ);
                final EnumFacing towerFacing = block.blockState.getValue(BlockBarracksTowerSubstitution.FACING);

                towers.add(new Tuple<>(towerPos, towerFacing));
            }
        }

        return towers;
    }

    /**
     * BuildingDeliveryman View.
     */
    public static class View extends AbstractBuildingView
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
