package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.workerbuildings.IField;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldModule extends AbstractBuildingModule implements IPersistentModule, IBuildingEventsModule, IBuildingModule
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
     * The list of the fields the citizen manages.
     */
    private final List<IField> fields = new ArrayList<>();

    /**
     * The list of the plants the citizen manages.
     */
    private final Set<Item> plants = new HashSet<>();

    /**
     * The field the citizen is currently working on.
     */
    @Nullable
    private IField currentIField;

    /**
     * The field the citizen worked on the last morning (first).
     */
    @Nullable
    private IField lastField;

    /**
     * Fields should be assigned manually to the citizen.
     */
    private boolean shouldAssignManually = false;

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final ListTag fieldTagList = compound.getList(TAG_FIELDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.size(); ++i)
        {
            final CompoundTag fieldCompound = fieldTagList.getCompound(i);
            final BlockPos fieldLocation = BlockPosUtil.read(fieldCompound, TAG_FIELDS_BLOCKPOS);
            getFieldFromPosition(fieldLocation).ifPresent(fields::add);
        }
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);

        if (compound.getAllKeys().contains(LAST_FIELD_TAG))
        {
            lastField = getFieldFromPosition(BlockPosUtil.read(compound, LAST_FIELD_TAG)).orElse(null);
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        final ListTag fieldTagList = new ListTag();
        for (final IField f : fields)
        {
            final CompoundTag fieldCompound = new CompoundTag();
            BlockPosUtil.write(fieldCompound, TAG_FIELDS_BLOCKPOS, f.getPosition());
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_FIELDS, fieldTagList);
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        if (lastField != null)
        {
            BlockPosUtil.write(compound, LAST_FIELD_TAG, lastField.getPosition());
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(shouldAssignManually);

        int size = 0;

        final List<IField> allFields = new ArrayList<>(getFields(building.getColony()));
        final List<IField> cleanedFields = new ArrayList<>();
        final List<IField> ownedIFields = new ArrayList<>();

        final WorkerBuildingModule module = building.getFirstModuleOccurance(WorkerBuildingModule.class);
        for (final IField field : allFields)
        {
            if (field.isTaken() && module.getFirstCitizen() != null)
            {
                if (module.getAssignedCitizen().isEmpty() || ((Integer) module.getFirstCitizen().getId()).equals(field.getOwnerId()))
                {
                    cleanedFields.add(field);
                    ownedIFields.add(field);
                    size++;
                }
            }
            else
            {
                size++;
                cleanedFields.add(field);
            }
        }

        buf.writeInt(size);
        for (final IField field : cleanedFields)
        {
            buf.writeEnum(field.getType());
            field.serializeToView(buf);
        }

        buf.writeInt(ownedIFields.size());
        buf.writeInt(getMaxFieldCount());
        buf.writeInt(getMaxConcurrentPlants());
    }

    /**
     * Getter to obtain the maximum field count.
     *
     * @return an integer stating the maximum field count.
     */
    protected abstract int getMaxFieldCount();

    /**
     * Getter to obtain the maximum amount of concurrent plants.
     *
     * @return an integer stating the maximum concurrent plant count.
     */
    protected abstract int getMaxConcurrentPlants();

    /**
     * Util method that obtains a field instance from its respective block position
     *
     * @param position the position.
     * @return the field instance or null.
     */
    private Optional<IField> getFieldFromPosition(BlockPos position)
    {
        return getFields(building.getColony()).stream().filter(f -> f.getPosition().equals(position)).findFirst();
    }

    /**
     * Getter to obtain the fields this module should process.
     *
     * @param colony the current colony.
     * @return a collection of fields.
     */
    protected abstract Collection<IField> getFields(IColony colony);

    /**
     * Getter of the current field.
     *
     * @return a field object.
     */
    @Nullable
    public IField getCurrentField()
    {
        return currentIField;
    }

    /**
     * Retrieves a random field to work on for the citizen.
     * This method will also automatically claim any fields that are not in use if the building is on automatic assignment mode.
     *
     * @return a field to work on.
     */
    @Nullable
    public IField getFieldToWorkOn()
    {
        if (getFields().size() < building.getBuildingLevel() && !shouldAssignManually)
        {
            IField freeField = getFreeField(building.getColony());
            if (freeField != null)
            {
                assignField(freeField);
            }
        }

        final List<IField> ownedIFields = new ArrayList<>(this.fields);
        Collections.shuffle(ownedIFields);

        if (!ownedIFields.isEmpty())
        {
            if (ownedIFields.get(0).equals(lastField))
            {
                Collections.shuffle(ownedIFields);
            }
            lastField = ownedIFields.get(0);
        }
        for (final IField field : ownedIFields)
        {
            if (field.needsWork())
            {
                currentIField = field;
                return field;
            }
        }
        return null;
    }

    /**
     * Returns list of fields of the farmer.
     *
     * @return a list of field objects.
     */
    @NotNull
    public List<IField> getFields()
    {
        return new ArrayList<>(fields);
    }

    /**
     * Getter to obtain any free field in the colony.
     *
     * @param colony the current colony.
     * @return any free field or null.
     */
    @Nullable
    protected abstract IField getFreeField(IColony colony);

    /**
     * Method called to assign a field to the building.
     *
     * @param field the field to add.
     */
    public void assignField(final IField field)
    {
        if (!canAddField(fields.size(), plants.size(), getMaxFieldCount(), getMaxConcurrentPlants()))
        {
            return;
        }

        final WorkerBuildingModule module = building.getFirstModuleOccurance(WorkerBuildingModule.class);
        if (module.getFirstCitizen() != null)
        {
            field.setOwner(module.getFirstCitizen().getId());
        }
        fields.add(field);
        plants.add(field.getPlant());
        markDirty();
    }

    /**
     * Utility method to see if a field can be still be assigned.
     *
     * @param amountOfFields      the amount of fields the module currently contains.
     * @param amountOfPlants      the amount of unique plants that the citizen works on.
     * @param maxFieldCount       the maximum amount of fields the building supports.
     * @param maxConcurrentPlants the maximum amount of concurrent plants the building supports.
     * @return true if all conditions pass.
     */
    public static boolean canAddField(int amountOfFields, int amountOfPlants, int maxFieldCount, int maxConcurrentPlants)
    {
        final boolean exceedsTotalFields = amountOfFields >= maxFieldCount;
        final boolean exceedsTotalConcurrentPlants = amountOfPlants >= maxConcurrentPlants;
        return !exceedsTotalFields && !exceedsTotalConcurrentPlants;
    }

    /**
     * Method called to assign a field to the building.
     *
     * @param position position of the field.
     */
    public void assignField(final BlockPos position)
    {
        getFieldFromPosition(position).ifPresent(this::assignField);
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
     * Checks if the building has any fields.
     *
     * @return true if he has none.
     */
    public boolean hasNoFields()
    {
        return fields.isEmpty();
    }

    /**
     * Switches the assign manually of the building.
     *
     * @param assignManually true if assignment should be manual.
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Method called to free a field.
     *
     * @param position the position a field that needs to be freed.
     */
    public void freeField(final BlockPos position)
    {
        getFieldFromPosition(position).ifPresent(this::freeField);
    }

    /**
     * Method called to free a field.
     *
     * @param field the field to be freed.
     */
    public void freeField(final IField field)
    {
        fields.remove(field);
        field.resetOwner();
        building.getColony().getWorld()
          .sendBlockUpdated(field.getPosition(),
            building.getColony().getWorld().getBlockState(field.getPosition()),
            building.getColony().getWorld().getBlockState(field.getPosition()),
            BLOCK_UPDATE_FLAG);

        if (currentIField != null && currentIField.equals(field))
        {
            currentIField = null;
        }
    }
}
