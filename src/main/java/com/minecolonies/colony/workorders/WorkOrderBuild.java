package com.minecolonies.colony.workorders;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class WorkOrderBuild extends WorkOrder
{
    protected ChunkCoordinates buildingId;
    protected String           upgradeName;

    private static final String TAG_BUILDING     = "building";
    private static final String TAG_UPGRADE_NAME = "upgrade";

    public WorkOrderBuild()
    {
        super();
    }

    public WorkOrderBuild(Building building, int level)
    {
        super();
        this.buildingId = building.getID();
        this.upgradeName = building.getSchematicName() + level;
    }

    public ChunkCoordinates getBuildingId()
    {
        return buildingId;
    }

    public String getUpgradeName()
    {
        return upgradeName;
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

    @Override
    public void attemptToFulfill(Colony colony)
    {
        for (CitizenData citizen : colony.getCitizens().values())
        {
            JobBuilder job = citizen.getJob(JobBuilder.class);
            if (job == null || job.hasWorkOrder())
            {
                continue;
            }

            //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
            //  - The Builder's Work Building is built
            //  - OR the WorkOrder is for the Builder's Work Building
            //  - OR the WorkOrder is for the Townhall
            if (citizen.getWorkBuilding().getBuildingLevel() > 0 ||
                    citizen.getWorkBuilding().getID().equals(buildingId) ||
                    (colony.hasTownhall() && colony.getTownhall().getID().equals(buildingId)))
            {
                job.setWorkOrder(this);
                setClaimedBy(citizen);
                return;
            }
        }
    }
}
