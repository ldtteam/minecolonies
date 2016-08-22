package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Field;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Job class of the farmer, handles his fields.
 */
public class JobFarmer extends AbstractJob
{
    /**
     * NBTTag to store the fields.
     */
    private static final String TAG_FIELDS = "fields";

    /**
     * NBTTag to store the currentField.
     */
    private static final String TAG_CURRENT_FIELD = "currentField";

    /**
     * The list of the fields the farmer manages.
     */
    private ArrayList<Field> farmerFields = new ArrayList<>();

    /**
     * The field the farmer is currently working on.
     */
    private Field currentField;

    public JobFarmer(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Farmer"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList fieldTagList = new NBTTagList();
        for (Field f : farmerFields)
        {
            NBTTagCompound fieldCompound = new NBTTagCompound();
            f.writeToNBT(fieldCompound);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_FIELDS, fieldTagList);
        compound.setInteger(TAG_CURRENT_FIELD,farmerFields.indexOf(currentField));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList fieldTagList = compound.getTagList(TAG_FIELDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            NBTTagCompound fieldCompound = fieldTagList.getCompoundTagAt(i);

            Field f = Field.createFromNBT(getColony(), fieldCompound);
            if (f != null)
            {
                farmerFields.add(f);
            }
        }
        currentField = farmerFields.get(compound.getInteger(TAG_CURRENT_FIELD));
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkFarmer(this);
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
     * Assigns a field list to the field list.
     * @param farmerFields the list to set.
     */
    public void setFarmerFields(final ArrayList<Field> farmerFields)
    {
        this.farmerFields = farmerFields;
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

    public Field getFieldToWorkOn()
    {
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
}
