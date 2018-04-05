package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.BlockBarracksTowerSubstitution;
import com.minecolonies.coremod.blocks.BlockHutBarracks;
import com.minecolonies.coremod.blocks.BlockHutBarracksTower;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBarracksBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Building class for the Barracks.
 */
public class BuildingBarracksNew extends AbstractBuilding
{
    /**
     * Name of our building's Schematics.
     */
    private static final String SCHEMATIC_NAME = "Barracks";

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected BuildingBarracksNew(@NotNull final Colony colony, final BlockPos pos)
    {
        super(colony, pos);
    }

    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

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
            for (final Tuple<BlockPos, EnumFacing> tower : getBarracksTowers())
            {
                world.setBlockState(tower.getFirst(), Blocks.AIR.getDefaultState());
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
            for (final Tuple<BlockPos, EnumFacing> tower : getBarracksTowers())
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
            }
        }
        super.onUpgradeComplete(newLevel);
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

        for (Template.BlockInfo block : wrapper.getStructure().getStructure().getTemplate().blocks)
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
