package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingEventsModule, IBuildingModule
{
    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * The list of the fields the citizen manages.
     */
    private final Set<IField> fields = new HashSet<>();

    /**
     * The field the citizen is currently working on.
     */
    @Nullable
    private IField currentField;

    /**
     * Fields should be assigned manually to the citizen.
     */
    private boolean shouldAssignManually = false;

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);

        updateFields();
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(shouldAssignManually);
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
     * Updates the internal set of fields backed by {@link FieldsModule#getFields(IColony)}
     */
    private void updateFields()
    {
        fields.clear();
        fields.addAll(getFields(building.getColony()).stream()
                        .filter(f -> building.getID().equals(f.getBuildingId()))
                        .toList());
    }

    /**
     * Getter to obtain the fields this module should process.
     *
     * @param colony the current colony.
     * @return a collection of fields.
     */
    protected abstract @NotNull Set<? extends IField> getFields(IColony colony);

    /**
     * Get the class type which is expected for the fields to have.
     *
     * @return the class type.
     */
    public abstract Class<?> getExpectedFieldType();

    /**
     * Getter of the current field.
     *
     * @return a field object.
     */
    @Nullable
    public IField getCurrentField()
    {
        return currentField;
    }

    /**
     * Resets the current field if the worker indicates this field should no longer be worked on.
     */
    public void resetCurrentField()
    {
        currentField = null;
    }

    /**
     * Retrieves the field to work on for the citizen, as long as the current field has work, it will keep returning that field.
     * Else it will retrieve a random field to work on for the citizen.
     * This method will also automatically claim any fields that are not in use if the building is on automatic assignment mode.
     *
     * @return a field to work on.
     */
    @Nullable
    public IField getFieldToWorkOn()
    {
        if (currentField != null && currentField.needsWork())
        {
            return currentField;
        }

        final List<IField> randomizedFields = this.fields.stream()
                                                .filter(field -> !field.equals(currentField))
                                                .collect(CollectorUtils.toShuffledList());
        for (final IField field : randomizedFields)
        {
            if (field.needsWork())
            {
                currentField = field;
                return field;
            }
        }
        return null;
    }

    /**
     * Attempt to automatically claim free fields, if possible and if any fields are available.
     */
    public void claimFields()
    {
        if (getFields().size() < building.getBuildingLevel() && !shouldAssignManually)
        {
            IField freeField = getFreeField(building.getColony());
            if (freeField != null)
            {
                assignField(freeField);
            }
        }
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
        if (!canAddField(field))
        {
            return;
        }

        field.setBuilding(building.getID());
        fields.add(field);
        markDirty();
    }

    /**
     * Check if a field can be added to the building.
     *
     * @param field the field which is being added.
     * @return true if the field can be added.
     */
    public boolean canAddField(IField field)
    {
        return checkFieldConditions(fields.size(), fields.stream().map(IField::getPlant).collect(Collectors.toSet()).size(), getMaxFieldCount(), getMaxConcurrentPlants());
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
    public static boolean checkFieldConditions(int amountOfFields, int amountOfPlants, int maxFieldCount, int maxConcurrentPlants)
    {
        return checkFieldCount(amountOfFields, maxFieldCount) && checkPlantCount(amountOfPlants, maxConcurrentPlants);
    }

    /**
     * Checks if the amount of fields is lower than the maximum allowed fields.
     *
     * @param amountOfFields the amount of fields.
     * @param maxFieldCount  the maximum amount of fields.
     * @return true if so.
     */
    public static boolean checkFieldCount(int amountOfFields, int maxFieldCount)
    {
        return amountOfFields < maxFieldCount;
    }

    /**
     * Checks if the amount of fields is lower than the maximum allowed fields.
     *
     * @param amountOfPlants      the amount of plants.
     * @param maxConcurrentPlants the maximum amount of concurrent plants.
     * @return true if so.
     */
    public static boolean checkPlantCount(int amountOfPlants, int maxConcurrentPlants)
    {
        return amountOfPlants < maxConcurrentPlants;
    }

    /**
     * Method called to assign a field to the building.
     *
     * @param matcher the field matcher.
     */
    public final void assignField(final FieldRecord matcher)
    {
        getFieldByMatcher(matcher).ifPresent(this::assignField);
    }

    /**
     * Query a field by checking for the appropriate field matcher.
     *
     * @param matcher the field matcher.
     * @return an optional containing a field if one was found.
     */
    private Optional<? extends IField> getFieldByMatcher(final FieldRecord matcher)
    {
        return getFields(building.getColony()).stream().filter(f -> f.matches(matcher)).findFirst();
    }

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public final boolean assignManually()
    {
        return shouldAssignManually;
    }

    /**
     * Checks if the building has any fields.
     *
     * @return true if he has none.
     */
    public final boolean hasNoFields()
    {
        return fields.isEmpty();
    }

    /**
     * Switches the assign manually of the building.
     *
     * @param assignManually true if assignment should be manual.
     */
    public final void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Method called to free a field.
     *
     * @param matcher the field matcher.
     */
    public final void freeField(final FieldRecord matcher)
    {
        getFieldByMatcher(matcher).ifPresent(this::freeField);
    }

    /**
     * Method called to free a field.
     *
     * @param field the field to be freed.
     */
    public void freeField(final IField field)
    {
        fields.remove(field);
        field.resetOwningBuilding();
        markDirty();

        if (currentField != null && currentField.equals(field))
        {
            currentField = null;
        }
    }
}