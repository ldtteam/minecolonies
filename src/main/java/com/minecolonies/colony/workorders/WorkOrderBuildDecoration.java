package com.minecolonies.colony.workorders;

import com.minecolonies.colony.Colony;
import net.minecraft.util.BlockPos;

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

    public WorkOrderBuildDecoration(String decoration, String style, int rotation, BlockPos location)
    {
        this.schematicName = style + '/' + decoration;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
    }

    @Override
    public boolean isValid(Colony colony)
    {
        return true;
    }
}
