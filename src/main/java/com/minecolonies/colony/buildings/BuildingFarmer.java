package com.minecolonies.colony.buildings;

import com.minecolonies.MineColonies;
import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutFarmer;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.ai.citizen.farmer.Field;
import com.minecolonies.entity.ai.citizen.farmer.FieldView;
import com.minecolonies.network.messages.AssignFieldMessage;
import com.minecolonies.network.messages.AssignmentModeMessage;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.util.LanguageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private static final String FARMER = "Farmer";

    /**
     * NBTTag to store the fields.
     */
    private static final String TAG_FIELDS = "fields";

    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * The list of the fields the farmer manages.
     */
    private final ArrayList<Field> farmerFields = new ArrayList<>();

    /**
     * The field the farmer is currently working on.
     */
    @Nullable
    private Field currentField;

    /**
     * Fields should be assigned manually to the farmer?
     */
    private boolean assignManually = false;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * Returns list of fields of the farmer.
     *
     * @return a list of field objects.
     */
    @NotNull
    public List<Field> getFarmerFields()
    {
        return Collections.unmodifiableList(farmerFields);
    }

    /**
     * Checks if the farmer has any fields.
     *
     * @return true if he has none.
     */
    public boolean hasNoFields()
    {
        return farmerFields.isEmpty();
    }

    /**
     * Assigns a field list to the field list.
     *
     * @param field the field to add.
     */
    public void addFarmerFields(Field field)
    {
        farmerFields.add(field);
    }

    /**
     * Getter of the current field.
     *
     * @return a field object.
     */
    @Nullable
    public Field getCurrentField()
    {
        return currentField;
    }

    /**
     * Sets the field the farmer is currently working on.
     *
     * @param currentField the field to work on.
     */
    public void setCurrentField(final Field currentField)
    {
        this.currentField = currentField;
    }

    /**
     * Retrieves a random field to work on for the farmer.
     *
     * @return a field to work on.
     */
    @Nullable
    public Field getFieldToWorkOn()
    {
        Collections.shuffle(farmerFields);
        for (@NotNull final Field field : farmerFields)
        {
            if (field.needsWork())
            {
                currentField = field;
                return field;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FARMER;
    }

    @NotNull
    @Override
    public AbstractJob createJob(@NotNull CitizenData citizen)
    {
        if (!farmerFields.isEmpty())
        {
            for (@NotNull final Field field : farmerFields)
            {
                final Field colonyField = getColony().getField(field.getID());
                if (colonyField != null)
                {
                    colonyField.setOwner(citizen.getName());
                }
                field.setOwner(citizen.getName());
            }
        }
        return new JobFarmer(citizen);
    }

    //we have to update our field from the colony!
    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
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
        assignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList fieldTagList = new NBTTagList();
        for (@NotNull final Field f : farmerFields)
        {
            @NotNull final NBTTagCompound fieldCompound = new NBTTagCompound();
            f.writeToNBT(fieldCompound);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_FIELDS, fieldTagList);
        compound.setBoolean(TAG_ASSIGN_MANUALLY, assignManually);
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for (@NotNull final Field field : farmerFields)
        {
            final Field tempField = getColony().getField(field.getID());

            if (tempField != null)
            {
                tempField.setTaken(false);
                tempField.setOwner("");
                @NotNull final ScarecrowTileEntity scarecrowTileEntity = (ScarecrowTileEntity) getColony().getWorld().getTileEntity(field.getID());
                getColony().getWorld()
                  .notifyBlockUpdate(scarecrowTileEntity.getPos(),
                    getColony().getWorld().getBlockState(scarecrowTileEntity.getPos()),
                    getColony().getWorld().getBlockState(scarecrowTileEntity.getPos()),
                    3);
                scarecrowTileEntity.setName(LanguageHandler.format("com.minecolonies.gui.scarecrow.user", LanguageHandler.format("com.minecolonies.gui.scarecrow.user.noone")));
            }
        }
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(assignManually);

        int size = 0;

        for (@NotNull Field field : getColony().getFields().values())
        {
            if (field.isTaken())
            {
                if (getWorker() == null || field.getOwner().equals(getWorker().getName()))
                {
                    size++;
                }
            }
            else
            {
                size++;
            }
        }

        buf.writeInt(size);

        for (@NotNull final Field field : getColony().getFields().values())
        {
            if (field.isTaken())
            {
                if (getWorker() == null || field.getOwner().equals(getWorker().getName()))
                {
                    @NotNull final FieldView fieldView = new FieldView(field);
                    fieldView.serializeViewNetworkData(buf);
                }
            }
            else
            {
                @NotNull final FieldView fieldView = new FieldView(field);
                fieldView.serializeViewNetworkData(buf);
            }
        }

        if (getWorker() == null)
        {
            ByteBufUtils.writeUTF8String(buf, "");
        }
        else
        {
            ByteBufUtils.writeUTF8String(buf, getWorker().getName());
        }
    }

    /**
     * Synchronize field list with colony.
     *
     * @param world the world the building is in.
     */
    public void syncWithColony(@NotNull World world)
    {
        if (!farmerFields.isEmpty())
        {
            @NotNull final ArrayList<Field> tempFields = new ArrayList<>(farmerFields);

            for (@NotNull final Field field : tempFields)
            {
                @NotNull final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) world.getTileEntity(field.getID());
                if (scarecrow == null)
                {
                    farmerFields.remove(field);
                    if (currentField != null && currentField.getID() == field.getID())
                    {
                        currentField = null;
                    }
                }
                else
                {
                    scarecrow.setName(LanguageHandler.format("com.minecolonies.gui.scarecrow.user", getWorker().getName()));
                    getColony().getWorld()
                      .notifyBlockUpdate(scarecrow.getPos(), getColony().getWorld().getBlockState(scarecrow.getPos()), getColony().getWorld().getBlockState(scarecrow.getPos()), 3);
                    field.setInventoryField(scarecrow.getInventoryField());
                    if (currentField != null && currentField.getID() == field.getID())
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
        for (@NotNull final Field field : farmerFields)
        {
            field.setNeedsWork(true);
            field.calculateSize(getColony().getWorld(), field.getLocation().down());
        }
    }

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public boolean assignManually()
    {
        return assignManually;
    }

    /**
     * Method called to free a field.
     *
     * @param position id of the field.
     */
    public void freeField(BlockPos position)
    {
        //Get the field with matching id, if none found return null.
        final Field tempField = farmerFields.stream().filter(field -> field.getID().equals(position)).findFirst().orElse(null);

        if (tempField != null)
        {
            farmerFields.remove(tempField);
            Field field = getColony().getField(position);
            field.setTaken(false);
            field.setOwner("");
            @NotNull final ScarecrowTileEntity scarecrowTileEntity = (ScarecrowTileEntity) getColony().getWorld().getTileEntity(field.getID());
            getColony().getWorld()
              .notifyBlockUpdate(scarecrowTileEntity.getPos(),
                getColony().getWorld().getBlockState(scarecrowTileEntity.getPos()),
                getColony().getWorld().getBlockState(scarecrowTileEntity.getPos()),
                3);
            scarecrowTileEntity.setName(LanguageHandler.format("com.minecolonies.gui.scarecrow.user", LanguageHandler.format("com.minecolonies.gui.scarecrow.user.noone")));
        }
    }

    /**
     * Method called to assign a field to the farmer.
     *
     * @param position id of the field.
     */
    public void assignField(BlockPos position)
    {
        final Field field = getColony().getField(position);
        field.setTaken(true);
        field.setOwner(getWorker().getName());
        farmerFields.add(field);
    }

    /**
     * Switches the assignManually of the farmer.
     *
     * @param assignManually true if assignment should be manual.
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.assignManually = assignManually;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            getColony().triggerAchievement(ModAchievements.achievementBuildingFarmer);
        }
        if (newLevel >= getMaxBuildingLevel())
        {
            getColony().triggerAchievement(ModAchievements.achievementUpgradeFarmerMax);
        }
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Checks if fields should be assigned manually.
         */
        private boolean assignFieldManually;

        /**
         * Contains a view object of all the fields in the colony.
         */
        @NotNull
        private List<FieldView> fields = new ArrayList<>();

        /**
         * Name of the worker of the building.
         */
        private String workerName;

        /**
         * The amount of fields the farmer owns;
         */
        private int amountOfFields;

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

        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutFarmer(this);
        }

        @Override
        public void deserialize(@NotNull ByteBuf buf)
        {
            fields = new ArrayList<>();
            super.deserialize(buf);
            assignFieldManually = buf.readBoolean();
            final int size = buf.readInt();
            for (int i = 1; i <= size; i++)
            {
                @NotNull final FieldView fieldView = new FieldView();
                fieldView.deserialize(buf);
                fields.add(fieldView);
                if (fieldView.isTaken())
                {
                    amountOfFields++;
                }
            }
            workerName = ByteBufUtils.readUTF8String(buf);
        }

        /**
         * Should the farmer be assigned manually to the fields?
         *
         * @return true if yes.
         */
        public boolean assignFieldManually()
        {
            return assignFieldManually;
        }

        /**
         * Getter of the fields list.
         *
         * @return an unmodifiable List.
         */
        @NotNull
        public List<FieldView> getFields()
        {
            return Collections.unmodifiableList(fields);
        }

        /**
         * Getter of the worker name.
         *
         * @return the name of the worker.
         */
        public String getWorkerName()
        {
            return workerName;
        }

        /**
         * Getter for amount of fields.
         *
         * @return the amount of fields.
         */
        public int getAmountOfFields()
        {
            return amountOfFields;
        }

        /**
         * Sets the assignedFieldManually in the view.
         *
         * @param assignFieldManually variable to set.
         */
        public void setAssignFieldManually(final boolean assignFieldManually)
        {
            MineColonies.getNetwork().sendToServer(new AssignmentModeMessage(this, assignFieldManually));
            this.assignFieldManually = assignFieldManually;
        }

        public void changeFields(final BlockPos id, boolean addNewField, int row)
        {
            MineColonies.getNetwork().sendToServer(new AssignFieldMessage(this, addNewField, id));
            fields.get(row).setTaken(addNewField);

            if (addNewField)
            {
                fields.get(row).setOwner(workerName);
                amountOfFields++;
            }
            else
            {
                fields.get(row).setOwner("");
                amountOfFields--;
            }
        }
    }
}

