package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Abstract class for all buildings which require a filterable list of allowed/blocked items.
 */
public class FarmerFieldModule extends AbstractBuildingModule implements IPersistentModule, IBuildingEventsModule, IBuildingModule
{
    /**
     * NBTTag to store the fields.
     */
    private static final String TAG_FIELDS = "fields";

    /**
     * NBTTag to store the field BlockPos.
     */
    private static final String TAG_FIELDS_BLOCKPOS = "fieldsPos";

    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * Flag used to be notified about block updates.
     */
    private static final int BLOCK_UPDATE_FLAG = 3;

    /**
     * The last field tag.
     */
    private static final String LAST_FIELD_TAG = "lastField";

    /**
     * The field the farmer is currently working on.
     */
    @Nullable
    private BlockPos currentField;

    /**
     * The field the farmer worked on the last morning (first).
     */
    @Nullable
    private BlockPos lastField;

    /**
     * The list of the fields the farmer manages.
     */
    private final Set<BlockPos> farmerFields = new HashSet<>();

    /**
     * Fields should be assigned manually to the farmer.
     */
    private boolean shouldAssignManually = false;


    /**
     * Construct a new grouped itemlist module with the unique list identifier.
     */
    public FarmerFieldModule()
    {
        super();
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final ListTag fieldTagList = compound.getList(TAG_FIELDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.size(); ++i)
        {
            final CompoundTag fieldCompound = fieldTagList.getCompound(i);
            final BlockPos fieldLocation = BlockPosUtil.read(fieldCompound, TAG_FIELDS_BLOCKPOS);
            farmerFields.add(fieldLocation);
        }
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);

        if (compound.contains(LAST_FIELD_TAG))
        {
            lastField = BlockPosUtil.read(compound, LAST_FIELD_TAG);
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag fieldTagList = new ListTag();
        for (@NotNull final BlockPos f : farmerFields)
        {
            @NotNull final CompoundTag fieldCompound = new CompoundTag();
            BlockPosUtil.write(fieldCompound, TAG_FIELDS_BLOCKPOS, f);
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_FIELDS, fieldTagList);
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        if (lastField != null)
        {
            BlockPosUtil.write(compound, LAST_FIELD_TAG, lastField);
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(shouldAssignManually);

        int size = 0;

        final List<BlockPos> fields = new ArrayList<>(building.getColony().getBuildingManager().getFields());
        final List<BlockPos> cleanList = new ArrayList<>();

        final WorkerBuildingModule module = building.getFirstModuleOccurance(WorkerBuildingModule.class);
        for (@NotNull final BlockPos field : fields)
        {
            if (WorldUtil.isBlockLoaded(building.getColony().getWorld(), field))
            {
                final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
                if (scareCrow instanceof ScarecrowTileEntity)
                {
                    if (((ScarecrowTileEntity) scareCrow).isTaken())
                    {
                        if (module.getAssignedCitizen().isEmpty() || ((ScarecrowTileEntity) scareCrow).getOwnerId() == module.getFirstCitizen().getId())
                        {
                            cleanList.add(field);
                            size++;
                        }
                    }
                    else
                    {
                        size++;
                        cleanList.add(field);
                    }
                }
            }
        }

        buf.writeInt(size);
        for (@NotNull final BlockPos field : cleanList)
        {
            buf.writeBlockPos(field);
        }

        buf.writeInt(farmerFields.size());
    }

    /**
     * Getter of the current field.
     *
     * @return a field object.
     */
    @Nullable
    public BlockPos getCurrentField()
    {
        return currentField;
    }

    /**
     * Sets the field the farmer is currently working on.
     *
     * @param currentField the field to work on.
     */
    public void setCurrentField(@Nullable final BlockPos currentField)
    {
        this.currentField = currentField;
    }

    /**
     * Retrieves a random field to work on for the farmer.
     *
     * @param world the world it is in.
     * @return a field to work on.
     */
    @Nullable
    public BlockPos getFieldToWorkOn(final Level world)
    {
        final List<BlockPos> fields = new ArrayList<>(farmerFields);
        Collections.shuffle(fields);

        if (!fields.isEmpty())
        {
            if (fields.get(0).equals(lastField))
            {
                Collections.shuffle(fields);
            }
            lastField = fields.get(0);
        }
        for (@NotNull final BlockPos field : fields)
        {
            final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
            if (scareCrow instanceof ScarecrowTileEntity && ((ScarecrowTileEntity) scareCrow).needsWork())
            {
                currentField = field;
                return field;
            }
        }
        return null;
    }


    /**
     * Synchronize field list with colony.
     *
     * @param world the world the building is in.
     */
    public void syncWithColony(@NotNull final Level world)
    {
        if (!farmerFields.isEmpty())
        {
            @NotNull final ArrayList<BlockPos> tempFields = new ArrayList<>(farmerFields);
            final WorkerBuildingModule module = building.getFirstModuleOccurance(WorkerBuildingModule.class);
            for (@NotNull final BlockPos field : tempFields)
            {
                final BlockEntity scarecrow = world.getBlockEntity(field);
                if (scarecrow instanceof ScarecrowTileEntity)
                {
                    building.getColony().getWorld()
                      .sendBlockUpdated(scarecrow.getBlockPos(),
                        building.getColony().getWorld().getBlockState(scarecrow.getBlockPos()),
                        building.getColony().getWorld().getBlockState(scarecrow.getBlockPos()),
                        BLOCK_UPDATE_FLAG);
                    ((ScarecrowTileEntity) scarecrow).setTaken(true);
                    ((ScarecrowTileEntity) scarecrow).setOwner(module.getFirstCitizen() != null? module.getFirstCitizen().getId() : 0);
                    ((ScarecrowTileEntity) scarecrow).setColony(building.getColony());
                }
                else
                {
                    farmerFields.remove(field);
                    if (currentField != null && currentField.equals(field))
                    {
                        currentField = null;
                    }
                }
            }
        }
    }

    /**
     * Returns list of fields of the farmer.
     *
     * @return a list of field objects.
     */
    @NotNull
    public List<BlockPos> getFarmerFields()
    {
        return new ArrayList<>(farmerFields);
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
    public void addFarmerFields(final BlockPos field)
    {
        final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
        if (scareCrow instanceof ScarecrowTileEntity)
        {
            farmerFields.add(field);
            this.markDirty();
        }
    }

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public boolean assignManually()
    {
        return shouldAssignManually;
    }

    /**
     * Switches the assignManually of the farmer.
     *
     * @param assignManually true if assignment should be manual.
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Resets the fields to need work again.
     */
    public void resetFields()
    {
        for (@NotNull final BlockPos field : farmerFields)
        {
            final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
            if (scareCrow instanceof ScarecrowTileEntity)
            {
                ((ScarecrowTileEntity) scareCrow).setNeedsWork(true);
            }
        }
    }

    /**
     * Method called to free a field.
     *
     * @param position id of the field.
     */
    public void freeField(final BlockPos position)
    {
        final BlockEntity scarecrow = building.getColony().getWorld().getBlockEntity(position);
        if (scarecrow instanceof ScarecrowTileEntity)
        {
            farmerFields.remove(position);
            ((ScarecrowTileEntity) scarecrow).setTaken(false);
            ((ScarecrowTileEntity) scarecrow).setOwner(0);
            building.getColony().getWorld()
              .sendBlockUpdated(scarecrow.getBlockPos(),
                building.getColony().getWorld().getBlockState(scarecrow.getBlockPos()),
                building.getColony().getWorld().getBlockState(scarecrow.getBlockPos()),
                BLOCK_UPDATE_FLAG);
        }
    }

    /**
     * Method called to assign a field to the farmer.
     *
     * @param position id of the field.
     */
    public void assignField(final BlockPos position)
    {
        final BlockEntity scarecrow = building.getColony().getWorld().getBlockEntity(position);
        if (scarecrow instanceof ScarecrowTileEntity)
        {
            ((ScarecrowTileEntity) scarecrow).setTaken(true);
            final WorkerBuildingModule module = building.getFirstModuleOccurance(WorkerBuildingModule.class);
            if (module.getFirstCitizen() != null)
            {
                ((ScarecrowTileEntity) scarecrow).setOwner(module.getFirstCitizen().getId());
            }
            farmerFields.add(position);
            markDirty();
        }
    }

    @Override
    public void onWakeUp()
    {
        resetFields();
    }

    @Override
    public void onDestroyed()
    {
        for (@NotNull final BlockPos field : farmerFields)
        {
            final BlockEntity scareCrow = building.getColony().getWorld().getBlockEntity(field);
            if (scareCrow instanceof ScarecrowTileEntity)
            {
                ((ScarecrowTileEntity) scareCrow).setTaken(false);
                ((ScarecrowTileEntity) scareCrow).setOwner(0);

                building.getColony().getWorld()
                  .sendBlockUpdated(scareCrow.getBlockPos(),
                    building.getColony().getWorld().getBlockState(scareCrow.getBlockPos()),
                    building.getColony().getWorld().getBlockState(scareCrow.getBlockPos()),
                    BLOCK_UPDATE_FLAG);
            }
        }
    }
}
