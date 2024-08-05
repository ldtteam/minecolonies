package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignmentModeMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.BUILDING_TAB_FIELDS;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT;

/**
 * Client side version of the abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldsModuleView extends AbstractBuildingModuleView
{
    /**
     * Checks if fields should be assigned manually.
     */
    private boolean shouldAssignFieldManually;

    /**
     * The maximum amount of fields the building can support.
     */
    private int maxFieldCount;

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        shouldAssignFieldManually = buf.readBoolean();
        maxFieldCount = buf.readInt();
    }

    @Override
    public String getIcon()
    {
        return "field";
    }

    @Override
    public String getDesc()
    {
        return BUILDING_TAB_FIELDS;
    }

    /**
     * Should the citizen be assigned manually to the fields.
     *
     * @return true if yes.
     */
    public boolean assignFieldManually()
    {
        return shouldAssignFieldManually;
    }

    /**
     * Sets the assignedFieldManually in the view.
     *
     * @param assignFieldManually variable to set.
     */
    public void setAssignFieldManually(final boolean assignFieldManually)
    {
        this.shouldAssignFieldManually = assignFieldManually;
        new AssignmentModeMessage(buildingView, assignFieldManually, getProducer().getRuntimeID()).sendToServer();
    }

    /**
     * Assign a given field to the current worker.
     *
     * @param field the field to assign.
     */
    public void assignField(final IField field)
    {
        if (buildingView != null && canAssignField(field))
        {
            new AssignFieldMessage(buildingView, field, true, getProducer().getRuntimeID()).sendToServer();

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.setBuilding(buildingView.getID());
            }
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
        return getOwnedFields().size() < maxFieldCount && canAssignFieldOverride(field);
    }

    /**
     * Getter of all owned fields.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IField> getOwnedFields()
    {
        return getFields().stream()
                 .filter(field -> buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(new FieldsComparator(buildingView))
                 .toList();
    }

    /**
     * Additional checks to see if this field can be assigned to the building.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    protected abstract boolean canAssignFieldOverride(IField field);

    /**
     * Getter of all fields that are either free, or taken by the current building.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IField> getFields()
    {
        return getFieldsInColony().stream()
                 .filter(field -> !field.isTaken() || buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(new FieldsComparator(buildingView))
                 .toList();
    }

    /**
     * Obtains the list of fields from the colony.
     *
     * @return the list of field instances.
     */
    protected abstract List<IField> getFieldsInColony();

    /**
     * Free a field from the current worker.
     *
     * @param field the field to free.
     */
    public void freeField(final IField field)
    {
        if (buildingView != null)
        {
            new AssignFieldMessage(buildingView, field, false, getProducer().getRuntimeID()).sendToServer();

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.resetOwningBuilding();
            }
        }
    }

    /**
     * Get a warning text component for the specific field whenever this field cannot be assigned for any reason.
     *
     * @param field the field in question.
     * @return a text component that should be shown if there is a problem for the specific field, else null.
     */
    @Nullable
    public MutableComponent getFieldWarningTooltip(IField field)
    {
        if (getOwnedFields().size() >= maxFieldCount)
        {
            return Component.translatableEscape(FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT);
        }
        return null;
    }

    /**
     * Get the maximum allowed field count.
     *
     * @return the max field count.
     */
    public int getMaxFieldCount()
    {
        return maxFieldCount;
    }

    /**
     * Comparator class for sorting fields in a predictable order in the window.
     */
    static class FieldsComparator implements Comparator<IField>
    {
        /**
         * The building this comparator is running on.
         */
        private final IBuildingView assignedBuilding;

        /**
         * Default constructor.
         *
         * @param assignedBuilding the building this comparator is running on.
         */
        public FieldsComparator(IBuildingView assignedBuilding)
        {
            this.assignedBuilding = assignedBuilding;
        }

        @Override
        public int compare(final IField field1, final IField field2)
        {
            if (field1.isTaken() && field2.isTaken())
            {
                return field1.getSqDistance(assignedBuilding) - field2.getSqDistance(assignedBuilding);
            }
            else if (field1.isTaken())
            {
                return -1;
            }
            else if (field2.isTaken())
            {
                return 1;
            }

            return field1.getSqDistance(assignedBuilding) - field2.getSqDistance(assignedBuilding);
        }
    }
}