package com.minecolonies.colony.workorders;

import com.minecolonies.colony.Colony;
import net.minecraft.util.math.BlockPos;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderBuildDecoration extends WorkOrderBuild
{
    /**
     * unused constructor for reflection
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
     */
    public WorkOrderBuildDecoration(String decoration, String style, int rotation, BlockPos location)
    {
        this.schematicName = style + '/' + decoration;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
    }

    @Override
    protected String getValue()
    {
        return schematicName;
    }

    @Override
    public boolean isValid(Colony colony)
    {
        return true;
    }
}
