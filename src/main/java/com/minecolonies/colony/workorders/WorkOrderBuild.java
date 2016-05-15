package com.minecolonies.colony.workorders;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import com.minecolonies.util.Schematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorkOrderBuild extends WorkOrder
{
    private static final String TAG_BUILDING          = "building";
    private static final String TAG_UPGRADE_LEVEL     = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME      = "upgrade";
    private static final String TAG_IS_CLEARED        = "cleared";
    private static final String TAG_SCHEMATIC_NAME    = "schematicName";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";
    protected BlockPos  buildingId;
    protected Schematic schematic;
    private   int       buildingRotation;
    private   String    schematicName;
    private   int       upgradeLevel;
    private   String    upgradeName;
    private   boolean   cleared;

    public WorkOrderBuild()
    {
        super();
    }

    public WorkOrderBuild(Building building, int level)
    {
        super();
        this.buildingId = building.getID();
        this.upgradeLevel = level;
        this.upgradeName = building.getSchematicName() + level;
        this.schematicName = building.getStyle() + '/' + this.getUpgradeName();
        this.buildingRotation = building.getRotation();
        if (level > 0)
        {
            this.cleared = true;
        }
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
     * Does this workorder have a loaded Schematic?
     * <p>
     * if a schematic is not null there exists a location for it
     *
     * @return true if there is a loaded schematic for this workorder
     */
    public boolean hasSchematic()
    {
        return schematic != null;
    }

    /**
     * Tells if this workorder is cleared already.
     * If not, we have to clear the site of water etc.
     *
     * @return true if cleared already
     */
    public boolean isCleared()
    {
        return cleared;
    }

    /**
     * declare this workorder as cleared
     */
    public void setCleared()
    {
        this.cleared = true;
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
     * @param compound NBT tag compount
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, buildingId);
        compound.setInteger(TAG_UPGRADE_LEVEL, upgradeLevel);
        compound.setString(TAG_UPGRADE_NAME, upgradeName);
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
        buildingId = BlockPosUtil.readFromNBT(compound, TAG_BUILDING);
        upgradeLevel = compound.getInteger(TAG_UPGRADE_LEVEL);
        upgradeName = compound.getString(TAG_UPGRADE_NAME);
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
        return colony.getBuilding(buildingId) != null;
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
                citizen.getWorkBuilding().getID().equals(buildingId) ||
                (colony.hasTownHall() && colony.getTownHall().getID().equals(buildingId)))
            {
                job.setWorkOrder(this);
                this.setClaimedBy(citizen);
                return;
            }
        }
    }

    /**
     * return the current schematic for this buildjob
     *
     * @return null if none set
     */
    public Schematic getSchematic()
    {
        return schematic;
    }

    /**
     * set a new schematic for this build job
     * todo: make obsolete
     *
     * @param schematic the new schematic to use
     */
    public void setSchematic(final Schematic schematic)
    {
        this.schematic = schematic;
    }

    /**
     * Load the schematic for this building.
     *
     * @param world the world we want to place it
     */
    public void loadSchematic(World world)
    {
        //failsafe for faulty schematic files
        try
        {
            this.schematic = new Schematic(world, this.schematicName);
        }
        catch (IllegalStateException e)
        {
            Log.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", this.schematicName), e);
            this.schematic = null;
            return;
        }

        //put the building into place
        this.schematic.rotate(this.buildingRotation);
        this.schematic.setPosition(this.getBuildingId());
        //start this building by initializing the current work pointer
        this.schematic.incrementBlock();
    }

    /**
     * Returns the ID of the building (aka ChunkCoordinates)
     *
     * @return ID of the building
     */
    public BlockPos getBuildingId()
    {
        return buildingId;
    }

    /**
     * Get the name the schematic for this workorder.
     *
     * @return the internal string for this schematic
     */
    public String getSchematicName()
    {
        return this.schematicName;
    }
}
