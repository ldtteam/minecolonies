package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.Colony;
import net.minecraft.util.math.BlockPos;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderBuildDecoration extends WorkOrderBuild
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildDecoration()
    {
        super();
    }

    /**
     * Create a new work order telling the building to build a decoration.
     *
     * @param decoration The name of the decoration.
     * @param style      The style of the decoration.
     * @param rotation   The number of times the decoration was rotated.
     * @param location   The location where the decoration should be built.
     * @param mirror     Is the decoration mirrored?
     */
    public WorkOrderBuildDecoration(final String decoration, final String style, final int rotation, final BlockPos location, final boolean mirror)
    {
        super();
        this.structureName = style + '/' + decoration;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
        this.isMirrored = mirror;
    }

    @Override
    public boolean isValid(final Colony colony)
    {
        return true;
    }

    @Override
    protected String getValue()
    {
        return structureName;
    }
}
