package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.workorders.WorkOrder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.UUID;

public class JobBuilder extends Job
{
    protected UUID             workOrderId;
    protected Schematic        schematic;
    protected String           schematicName;
    protected ChunkCoordinates schematicPos;
    protected ChunkCoordinates schematicProgress;

    private final static String TAG_WORK_ORDER= "workorder";
    private final static String TAG_SCHEMATIC = "schematic";
    private final static String TAG_NAME      = "name";
    private final static String TAG_POSITION  = "position";
    private final static String TAG_PROGRESS  = "progress";

    public JobBuilder(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "Builder"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BUILDER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (workOrderId != null)
        {
            compound.setString(TAG_WORK_ORDER, workOrderId.toString());

            if (hasSchematic())
            {
                NBTTagCompound schematicTag = new NBTTagCompound();
                schematicTag.setString(TAG_NAME, schematic.getName());
                ChunkCoordUtils.writeToNBT(schematicTag, TAG_POSITION, schematic.getPosition());
                ChunkCoordUtils.writeToNBT(schematicTag, TAG_PROGRESS, schematic.getLocalPosition());
                compound.setTag(TAG_SCHEMATIC, schematicTag);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = UUID.fromString(compound.getString(TAG_WORK_ORDER));

            if(compound.hasKey(TAG_SCHEMATIC))
            {
                NBTTagCompound schematicTag = compound.getCompoundTag(TAG_SCHEMATIC);
                schematicName = schematicTag.getString(TAG_NAME);
                schematicPos = ChunkCoordUtils.readFromNBT(schematicTag, TAG_POSITION);
                schematicProgress = ChunkCoordUtils.readFromNBT(schematicTag, TAG_PROGRESS);
            }
        }
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        if (schematicName != null)
        {
            schematic = Schematic.loadSchematic(getCitizen().getColony().getWorld(), schematicName);
            if (schematic != null)
            {
                schematic.setPosition(schematicPos);
                schematic.setLocalPosition(schematicProgress);
            }

            schematicName = null;
            schematicPos = null;
            schematicProgress = null;
        }

        tasks.addTask(3, new EntityAIWorkBuilder(this));
    }

    public void setWorkOrder(WorkOrder order)
    {
        workOrderId = (order != null) ? order.getID() : null;
    }

    public UUID getWorkOrderId()
    {
        return workOrderId;
    }

    public boolean hasWorkOrder()
    {
        return workOrderId != null;
    }

    public boolean hasSchematic()
    {
        return schematic != null && schematic.hasSchematic();
    }

    public Schematic getSchematic()
    {
        return schematic;
    }

    public void setSchematic(Schematic schematic)
    {
        this.schematic = schematic;
    }

    public int getWorkInterval()
    {
        return 1;//Constants.BUILDERWORKINTERFALL - this.getLevel();//TODO
    }

    public void complete()
    {
        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setSchematic(null);
    }
}
