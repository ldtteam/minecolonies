package com.minecolonies.colony.workorders;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class WorkOrderBuild extends WorkOrder
{
    protected ChunkCoordinates buildingId;
    protected String           upgradeName;

    private final static String TAG_BUILDING     = "building";
    private final static String TAG_UPGRADE_NAME = "upgrade";

    public WorkOrderBuild()
    {
        super();
    }

    public WorkOrderBuild(ChunkCoordinates buildingId, String upgradeName)
    {
        super();
        this.buildingId = buildingId;
        this.upgradeName = upgradeName;
    }

    public ChunkCoordinates getBuildingId()
    {
        return buildingId;
    }

    public String getUpgradeName()
    {
        return upgradeName;
    }

    public static WorkOrderBuild create(Building building, int level)
    {
        String upgradeName = building.getSchematicName() + level;
        return new WorkOrderBuild(building.getID(), upgradeName);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        ChunkCoordUtils.writeToNBT(compound, TAG_BUILDING, buildingId);
        compound.setString(TAG_UPGRADE_NAME, upgradeName);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        buildingId = ChunkCoordUtils.readFromNBT(compound, TAG_BUILDING);
        upgradeName = compound.getString(TAG_UPGRADE_NAME);
    }
}
