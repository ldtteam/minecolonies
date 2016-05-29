package com.minecolonies.colony.workorders;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

/**
 * Represents one building order to complete.
 * Has his onw schematic for the building.
 */
public class WorkOrderBuild extends WorkOrder
{
    private static final String TAG_BUILDING          = "building";
    private static final String TAG_UPGRADE_LEVEL     = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME      = "upgrade";
    private static final String TAG_IS_CLEARED        = "cleared";
    private static final String TAG_SCHEMATIC_NAME    = "schematicName";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";

    BlockPos buildingLocation;
    int      buildingRotation;
    String   schematicName;
    private   int      upgradeLevel;
    private   String   upgradeName;
    boolean  cleared;

    /**
     * unused constructor for reflection
     */
    public WorkOrderBuild()
    {
        super();
    }

    /**
     * create a new WorkOrder
     *
     * @param building the building to build
     * @param level    the level it should have
     */
    public WorkOrderBuild(Building building, int level)
    {
        super();
        this.buildingLocation = building.getID();
        this.upgradeLevel = level;
        this.upgradeName = building.getSchematicName() + level;
        this.schematicName = building.getStyle() + '/' + this.getUpgradeName();
        this.buildingRotation = building.getRotation();
        this.cleared = level > 1;
    }

    /**
     * Returns the name after upgrade
     *
     * @return Name after upgrade
     */
    private String getUpgradeName()
    {
        return upgradeName;
    }

    /**
     * Returns the level up level of the building
     *
     * @return Level after upgrade
     */
    public int getUpgradeLevel()
    {
        return upgradeLevel;
    }

    /**
     * Save the Work Order to an NBTTagCompound
     *
     * @param compound NBT tag compound
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, buildingLocation);
        if(!(this instanceof WorkOrderBuildDecoration))
        {
            compound.setInteger(TAG_UPGRADE_LEVEL, upgradeLevel);
            compound.setString(TAG_UPGRADE_NAME, upgradeName);
        }
        compound.setBoolean(TAG_IS_CLEARED, cleared);
        compound.setString(TAG_SCHEMATIC_NAME, schematicName);
        compound.setInteger(TAG_BUILDING_ROTATION, buildingRotation);
    }

    /**
     * Read the WorkOrder data from the NBTTagCompound
     *
     * @param compound NBT Tag compound
     */
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        buildingLocation = BlockPosUtil.readFromNBT(compound, TAG_BUILDING);
        if(!(this instanceof WorkOrderBuildDecoration))
        {
            upgradeLevel = compound.getInteger(TAG_UPGRADE_LEVEL);
            upgradeName = compound.getString(TAG_UPGRADE_NAME);
        }
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        schematicName = compound.getString(TAG_SCHEMATIC_NAME);
        buildingRotation = compound.getInteger(TAG_BUILDING_ROTATION);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * @param colony The colony that owns the Work Order
     * @return True if the building for this workorder still exists
     */
    @Override
    public boolean isValid(Colony colony)
    {
        return colony.getBuilding(buildingLocation) != null;
    }

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     * <p>
     * finds the first suitable builder for this job
     *
     * @param colony The colony that owns the Work Order
     */
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
            //  - OR the WorkOrder is for the TownHall
            if (citizen.getWorkBuilding().getBuildingLevel() > 0 ||
                citizen.getWorkBuilding().getID().equals(buildingLocation) ||
                (colony.hasTownHall() && colony.getTownHall().getID().equals(buildingLocation)))
            {
                job.setWorkOrder(this);
                this.setClaimedBy(citizen);
                return;
            }
        }
    }

    /**
     * Returns the ID of the building (aka ChunkCoordinates)
     *
     * @return ID of the building
     */
    public BlockPos getBuildingLocation()
    {
        return buildingLocation;
    }

    /**
     * Get the name the schematic for this work order.
     *
     * @return the internal string for this schematic
     */
    public String getSchematicName()
    {
        return this.schematicName;
    }

    /**
     * Gets how many times this schematic should be rotated.
     *
     * @return building rotation
     */
    public int getRotation()
    {
        return buildingRotation;
    }

    public void setCleared(boolean cleared)
    {
        this.cleared = cleared;
    }

    public boolean isCleared()
    {
        return cleared;
    }
}
