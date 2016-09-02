package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutFarmer;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.ai.citizen.farmer.Field;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.ai.citizen.farmer.FieldView;
import com.minecolonies.entity.ai.citizen.miner.Level;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuildingWorker
{
    /**
     * Descriptive string of the profession.
     */
    private static final    String  FARMER      = "Farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final    int     MAX_BUILDING_LEVEL = 3;

    /**
     * NBTTag to store the fields.
     */
    private static final String TAG_FIELDS = "fields";

    /**
     * NBTTag to store the currentField.
     */
    private static final String TAG_CURRENT_FIELD = "currentField";

    private static final String TAG_ASSIGN_MANUALLY = "assign";
    /**
     * The list of the fields the farmer manages.
     */
    private final ArrayList<Field> farmerFields = new ArrayList<>();

    /**
     * The field the farmer is currently working on.
     */
    private Field currentField;

    /**
     * Fields should be assigned manually to the farmer?
     */
    private boolean assignFieldManually = false;

    /**
     * Public constructor which instantiates the building.
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(Colony c, BlockPos l)
    {
        super(c, l);
    }


    /**
     * Returns list of fields of the farmer.
     * @return a list of field objects.
     */
    public List<Field> getFarmerFields()
    {
        return Collections.unmodifiableList(farmerFields);
    }

    /**
     * Checks if the farmer has any fields.
     * @return true if he has none.
     */
    public boolean hasNoFields()
    {
        return farmerFields.isEmpty();
    }

    /**
     * Assigns a field list to the field list.
     * @param field the field to add.
     */
    public void addFarmerFields(Field field)
    {
        farmerFields.add(field);
    }

    /**
     * Getter of the current field.
     * @return a field object.
     */
    public Field getCurrentField()
    {
        return currentField;
    }

    /**
     * Sets the field the farmer is currently working on.
     * @param currentField the field to work on.
     */
    public void setCurrentField(final Field currentField)
    {
        this.currentField = currentField;
    }

    /**
     * Retrieves a random field to work on for the farmer.
     * @return a field to work on.
     */
    public Field getFieldToWorkOn()
    {
        Collections.shuffle(farmerFields);
        for(Field field: farmerFields)
        {
            if(field.needsWork())
            {
                currentField = field;
                return field;
            }
        }
        return null;
    }

    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public String getJobName()
    {
        return FARMER;
    }

    /**
     * Synchronize field list with colony.
     * @param world the world the building is in.
     */
    public void synchWithColony(World world)
    {
        if(!farmerFields.isEmpty())
        {
            final ArrayList<Field> tempFields = new ArrayList<>(farmerFields);

            for(final Field field: tempFields)
            {
                final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) world.getTileEntity(field.getID());
                if(scarecrow == null)
                {
                    farmerFields.remove(field);
                    if (currentField != null && currentField.getID() == field.getID())
                    {
                        currentField = null;
                    }
                }
                else
                {
                    field.setInventoryField(scarecrow.getInventoryField());
                    if(currentField != null && currentField.getID() == field.getID())
                    {
                        currentField.setInventoryField(scarecrow.getInventoryField());
                    }
                }
            }
        }
    }

    /**
     * Resets the fields to need work again.
     */
    public void resetFields()
    {
        for(final Field field: farmerFields)
        {
            field.setNeedsWork(true);
            field.calculateSize(getColony().getWorld(), field.getLocation().down());
        }
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for(final Field field: farmerFields)
        {
            field.setTaken(false);
            field.setOwner("");
        }
    }

    /**
     * Getter for the assign manually.
     * @return true if he should.
     */
    public boolean assignManually()
    {
        return assignFieldManually;
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobFarmer(citizen);
    }

    //we have to update our field from the colony!
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList fieldTagList = compound.getTagList(TAG_FIELDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            final NBTTagCompound fieldCompound = fieldTagList.getCompoundTagAt(i);
            final Field f = Field.createFromNBT(getColony(), fieldCompound);
            if (f != null)
            {
                farmerFields.add(f);
            }
        }
        assignFieldManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        final NBTTagList fieldTagList = new NBTTagList();
        for (final Field f : farmerFields)
        {
            final NBTTagCompound fieldCompound = new NBTTagCompound();
            f.writeToNBT(fieldCompound);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_FIELDS, fieldTagList);
        compound.setBoolean(TAG_ASSIGN_MANUALLY, assignFieldManually);
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(ByteBuf buf)
    {
        for(Field field: getFarmerFields())
        {
            FieldView fieldView = new FieldView(field);
            fieldView.serializeViewNetworkData(buf);
        }
        BlockPosUtil.writeToByteBuf(buf, this.getID());
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        //todo add to window.
        /**
         * Checks if fields should be assigned manually.
         */
        public boolean assignFieldManually;

        /**
         * Contains a view object of all the fields in the colony.
         */
        private List<FieldView> fields = new ArrayList<>();

        private BlockPos buildingLocation;

        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutFarmer(this);
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);
            assignFieldManually = buf.readBoolean();
            while(buf.isReadable())
            {
                FieldView fieldView = new FieldView();
                fieldView.serializeViewNetworkData(buf);
                fields.add(fieldView);
            }
            buildingLocation = BlockPosUtil.readFromByteBuf(buf);
        }

        /**
         * Getter of the fields list.
         * @return an unmodifiable List.
         */
        public List<FieldView> getFields()
        {
            return Collections.unmodifiableList(fields);
        }

        /**
         * Getter of the building location.
         * @return the blockPos.
         */
        private BlockPos getBuildingLocation()
        {
            return buildingLocation;
        }
    }
}

