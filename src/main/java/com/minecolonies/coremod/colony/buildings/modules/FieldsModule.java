package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingModule
{
    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * A map of fields, along with their unix timestamp of when they can next be checked again.
     */
    private final Map<IField, Instant> checkedFields = new HashMap<>();

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
    }

    /**
     * Getter to obtain the maximum field count.
     *
     * @return an integer stating the maximum field count.
     */
    protected abstract int getMaxFieldCount();

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
     * Retrieves the field to work on for the citizen, as long as the current field has work, it will keep returning that field.
     * Else it will retrieve a random field to work on for the citizen.
     * This method will also automatically claim any fields that are not in use if the building is on automatic assignment mode.
     *
     * @return a field to work on.
     */
    @Nullable
    public IField getFieldToWorkOn()
    {
        if (currentField != null)
        {
            return currentField;
        }

        Instant now = Instant.now();
        for (IField field : getOwnedFields().stream().collect(CollectorUtils.toShuffledList()))
        {
            if (!checkedFields.containsKey(field) || now.isAfter(checkedFields.get(field)))
            {
                checkedFields.remove(field);
                currentField = field;
                return field;
            }
        }
        return null;
    }

    /**
     * Returns list of owned fields.
     *
     * @return a list of field objects.
     */
    @NotNull
    public final List<IField> getOwnedFields()
    {
        return getFields().stream().filter(f -> building.getID().equals(f.getBuildingId())).toList();
    }

    /**
     * Returns list of fields.
     *
     * @return a list of field objects.
     */
    @NotNull
    public abstract List<IField> getFields();

    /**
     * Attempt to automatically claim free fields, if possible and if any fields are available.
     */
    public void claimFields()
    {
        if (!shouldAssignManually)
        {
            for (IField field : getFreeFields())
            {
                assignField(field);
            }
        }
    }

    /**
     * Returns list of free fields.
     *
     * @return a list of field objects.
     */
    public final List<IField> getFreeFields()
    {
        return getFields().stream().filter(field -> !field.isTaken()).toList();
    }

    /**
     * Method called to assign a field to the building.
     *
     * @param field the field to add.
     */
    public void assignField(final IField field)
    {
        if (canAssignField(field))
        {
            field.setBuilding(building.getID());
        }
    }

    /**
     * Check to see if a new field can be assigned to the worker.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    public final boolean canAssignField(IField field)
    {
        return getOwnedFields().size() < getMaxFieldCount() && canAssignFieldOverride(field);
    }

    /**
     * Additional checks to see if this field can be assigned to the building.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    protected abstract boolean canAssignFieldOverride(IField field);

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
        return getOwnedFields().isEmpty();
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
     * @param field the field to be freed.
     */
    public void freeField(final IField field)
    {
        field.resetOwningBuilding();
        markDirty();

        if (Objects.equals(currentField, field))
        {
            resetCurrentField();
        }
    }

    /**
     * Resets the current field if the worker indicates this field should no longer be worked on.
     */
    public void resetCurrentField()
    {
        if (currentField != null)
        {
            checkedFields.put(currentField, Instant.now().plus(getFieldCheckTimeoutSeconds(), ChronoUnit.SECONDS));
        }
        currentField = null;
    }

    /**
     * Get the timeout for fields to be allowed to be checked again.
     */
    protected abstract int getFieldCheckTimeoutSeconds();
}