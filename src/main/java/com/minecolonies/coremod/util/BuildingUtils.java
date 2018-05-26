package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractSchematicProvider;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BuildingUtils
{
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
    public static AxisAlignedBB getTargetAbleArea(final World world, final AbstractSchematicProvider building)
    {
        final BlockPos location = building.getLocation();
        final int x1;
        final int z1;
        final int x3;
        final int z3;
        final int y1 = location.getY() - 2;
        final int y3;

        if(building.getHeight() == 0)
        {
            final StructureName sn =
                    new StructureName(Structures.SCHEMATICS_PREFIX,
                            building.getStyle(),
                            building.getSchematicName() + building.getBuildingLevel());

            final String structureName = sn.toString();

            final StructureWrapper wrapper = new StructureWrapper(world, structureName);
            wrapper.rotate(building.getRotation(), world, location, building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);

            final BlockPos pos = location;
            wrapper.setPosition(pos);

            x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
            z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
            x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
            z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
            y3 = location.getY() + wrapper.getHeight();

            building.setCorners(x1, x3, z1, z3);
            building.setHeight(wrapper.getHeight());
        }
        else
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = building.getCorners();
            x1 = corners.getFirst().getFirst();
            x3 = corners.getFirst().getSecond();
            z1 = corners.getSecond().getFirst();
            z3 = corners.getSecond().getSecond();
            y3 = location.getY() + building.getHeight();
        }

        return new AxisAlignedBB(x1, y1, z1, x3, y3, z3);
    }
}
