package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BuildingUtils
{
    /**
     * Base height considered for the buildings.
     */
    private static final int BASE_HEIGHT = 10;

    /**
     * Private constructor to hide public one.
     */
    private BuildingUtils()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Calculate the Size of the building given a world and a building.
     * @param world the world.
     * @param building the building.
     * @return the AxisAlignedBB box.
     */
    public static AxisAlignedBB getTargetAbleArea(final World world, final AbstractBuilding building)
    {
        final int x1;
        final int z1;
        final int x3;
        final int z3;
        final int y1 = building.getLocation().getY() - 2;
        final int y3;

        if(building.getHeight() == 0)
        {
            final StructureName sn =
                    new StructureName(Structures.SCHEMATICS_PREFIX,
                            building.getStyle(),
                            building.getSchematicName() + building.getBuildingLevel());

            final String structureName = sn.toString();

            final StructureWrapper wrapper = new StructureWrapper(world, structureName);
            wrapper.rotate(building.getRotation(), world, building.getLocation(), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);

            final BlockPos pos = building.getLocation();
            wrapper.setPosition(pos);

            x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
            z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
            x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
            z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
            y3 = building.getLocation().getY() + BASE_HEIGHT;
        }
        else
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = building.getCorners();
            x1 = corners.getFirst().getFirst();
            x3 = corners.getFirst().getSecond();
            z1 = corners.getSecond().getFirst();
            z3 = corners.getSecond().getSecond();
            y3 = building.getLocation().getY() + building.getHeight();
        }

        return new AxisAlignedBB(x1, y1, z1, x3, y3, z3);
    }
}
