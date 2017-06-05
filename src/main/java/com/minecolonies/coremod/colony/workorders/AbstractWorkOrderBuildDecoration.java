package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.structures.Structures;
import net.minecraft.util.math.BlockPos;

/**
 * A work order that the build can take to build decorations.
 */
public class AbstractWorkOrderBuildDecoration extends AbstractWorkOrderBuild
{
    /**
     * Unused constructor for reflection.
     */
    public AbstractWorkOrderBuildDecoration()
    {
        super();
    }

    /**
     * Create a new work order telling the building to build a decoration.
     *
     * @param structureName The name of the decoration.
     * @param workOrderName The user friendly name of the decoration.
     * @param rotation      The number of times the decoration was rotated.
     * @param location      The location where the decoration should be built.
     * @param mirror        Is the decoration mirrored?
     */
    public AbstractWorkOrderBuildDecoration(final String structureName, final String workOrderName, final int rotation, final BlockPos location, final boolean mirror)
    {
        super();
        //normalise structure name
        final Structures.StructureName sn = new Structures.StructureName(structureName);
        this.structureName = sn.toString();
        this.workOrderName = workOrderName;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
        this.isMirrored = mirror;
    }

    @Override
    public boolean isDecoration()
    {
        return true;
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return true;
    }

    @Override
    protected String getValue()
    {
        return workOrderName;
    }
}
