package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.FieldsModuleWindow;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignmentModeMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.*;

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

    /**
     * The maximum amount of different plants the building can support.
     */
    private int maxConcurrentPlants;

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        shouldAssignFieldManually = buf.readBoolean();
        maxFieldCount = buf.readInt();
        maxConcurrentPlants = buf.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new FieldsModuleWindow(buildingView, this);
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
        Network.getNetwork().sendToServer(new AssignmentModeMessage(buildingView, assignFieldManually));
        this.shouldAssignFieldManually = assignFieldManually;
    }

    /**
     * Get the maximum allowed concurrent plants.
     *
     * @return the max concurrent plants.
     */
    public int getMaxConcurrentPlants()
    {
        return maxConcurrentPlants;
    }

    /**
     * Assign a given field to the current worker.
     *
     * @param field the field to assign.
     */
    public void assignField(final IFieldView field)
    {
        if (buildingView != null && FieldsModule.checkFieldConditions(getOwnedFields().size(), getWorkedPlants().size(), maxFieldCount, maxConcurrentPlants)
              && canAssignField(field))
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, true, field.getMatcher()));

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
    public List<IFieldView> getOwnedFields()
    {
        return getColony().getFields(getExpectedFieldType()).stream()
                 .filter(field -> buildingView.getID().equals(field.getBuildingId()))
                 .distinct()
                 .sorted(new FieldsComparator(buildingView))
                 .toList();
    }

    /**
     * Getter of the worked plants set.
     *
     * @return an unmodifiable set.
     */
    @NotNull
    public Set<Item> getWorkedPlants()
    {
        return getOwnedFields().stream()
                 .map(IFieldView::getPlant)
                 .collect(Collectors.toSet());
    }

    /**
     * Check to see if a new field can be assigned to the worker.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    public abstract boolean canAssignField(IFieldView field);

    /**
     * Get the class type which is expected for the fields to have.
     *
     * @return the class type.
     */
    public abstract FieldType getExpectedFieldType();

    /**
     * Getter of all fields that are either free, or taken by the current building.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IFieldView> getFields()
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
    public void freeField(final IFieldView field)
    {
        if (buildingView != null)
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, false, field.getMatcher()));

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
    public MutableComponent getFieldWarningTooltip(IFieldView field)
    {
        if (!FieldsModule.checkFieldCount(getOwnedFields().size(), maxFieldCount))
        {
            return Component.translatable(FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT);
        }
        else if (!FieldsModule.checkPlantCount(getWorkedPlants().size(), maxConcurrentPlants))
        {
            return Component.translatable(FIELD_LIST_WARN_EXCEEDS_PLANT_COUNT);
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
    static class FieldsComparator implements Comparator<IFieldView>
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
        public int compare(final IFieldView field1, final IFieldView field2)
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