package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignmentModeMessage;
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
    public void deserialize(@NotNull final FriendlyByteBuf buf)
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
        Network.getNetwork().sendToServer(new AssignmentModeMessage(buildingView, assignFieldManually));
    }

    /**
     * Assign a given field to the current worker.
     *
     * @param field the field to assign.
     */
    public void assignField(final IField field)
    {
        if (buildingView != null && FieldsModule.checkFieldCount(getOwnedFields().size(), maxFieldCount) && canAssignField(field))
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, field, true));

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.setBuilding(buildingView.getID());
            }
        }
    }

    /**
     * Getter of all owned fields.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IField> getOwnedFields()
    {
        return getColony().getFields(getExpectedFieldType()).stream()
                 .filter(field -> buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(new FieldsComparator(buildingView))
                 .toList();
    }

    /**
     * Check to see if a new field can be assigned to the worker.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    public abstract boolean canAssignField(IField field);

    /**
     * Get the class type which is expected for the fields to have.
     *
     * @return the field type.
     */
    public abstract FieldRegistries.FieldEntry getExpectedFieldType();

    /**
     * Getter of all fields that are either free, or taken by the current building.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IField> getFields()
    {
        return getColony().getFields(getExpectedFieldType()).stream()
                 .filter(field -> !field.isTaken() || buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(new FieldsComparator(buildingView))
                 .toList();
    }

    /**
     * Free a field from the current worker.
     *
     * @param field the field to free.
     */
    public void freeField(final IField field)
    {
        if (buildingView != null)
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, field, false));

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
        if (!FieldsModule.checkFieldCount(getOwnedFields().size(), maxFieldCount))
        {
            return Component.translatable(FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT);
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
                return field1.getDistance(assignedBuilding) - field2.getDistance(assignedBuilding);
            }
            else if (field1.isTaken())
            {
                return -1;
            }
            else if (field2.isTaken())
            {
                return 1;
            }

            return field1.getDistance(assignedBuilding) - field2.getDistance(assignedBuilding);
        }
    }
}