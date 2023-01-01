package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.FieldStructureType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.FieldsModuleWindow;
import com.minecolonies.coremod.colony.buildings.modules.FieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FieldRegistry;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignmentModeMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.*;

/**
 * Client side version of the abstract class to list all fields (assigned) to a building.
 */
public abstract class FieldModuleView extends AbstractBuildingModuleView
{
    /**
     * Checks if fields should be assigned manually.
     */
    private boolean shouldAssignFieldManually;

    /**
     * Contains a view object of all the fields in the colony.
     */
    @NotNull
    private List<IFieldView> fields = new ArrayList<>();

    /**
     * Set of plants being cultivated by this building.
     */
    private Set<Item> workedPlants = new HashSet<>();

    /**
     * The amount of fields the building owns.
     */
    private int amountOfOwnedFields;

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
        fields = new ArrayList<>();

        shouldAssignFieldManually = buf.readBoolean();
        int amountOfFields = buf.readInt();
        for (int i = 1; i <= amountOfFields; i++)
        {
            final FieldStructureType type = buf.readEnum(FieldStructureType.class);
            final IFieldView fieldView = FieldRegistry.getFieldViewClassForType(type, getColony());
            fieldView.deserialize(buf);
            fields.add(fieldView);
        }
        amountOfOwnedFields = buf.readInt();
        maxFieldCount = buf.readInt();
        maxConcurrentPlants = buf.readInt();

        calculateWorkedPlants();
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
     * Determines the set of unique plants being worked on every field.
     */
    private void calculateWorkedPlants()
    {
        workedPlants = fields.stream()
                         .map(field -> {
                             if (field != null && buildingView.getAllAssignedCitizens().contains(field.getOwnerId()))
                             {
                                 return field.getPlant();
                             }
                             return null;
                         })
                         .filter(Objects::nonNull)
                         .collect(Collectors.toSet());
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
     * Getter of the fields list.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<IFieldView> getFields()
    {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Getter of the worked plants set.
     *
     * @return an unmodifiable set.
     */
    @NotNull
    public Set<Item> getWorkedPlants()
    {
        return Collections.unmodifiableSet(workedPlants);
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
        if (buildingView != null && canAddField(field))
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, true, field.getPosition()));

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                if (!buildingModuleView.getAssignedCitizens().isEmpty())
                {
                    field.setOwner(buildingModuleView.getAssignedCitizens().get(0));
                }
                amountOfOwnedFields++;
                calculateWorkedPlants();
            }
        }
    }

    /**
     * Check to see if a new field can be assigned to the worker.
     *
     * @param field the field which is being added.
     * @return true if so.
     */
    public boolean canAddField(IFieldView field)
    {
        return FieldModule.checkFieldConditions(amountOfOwnedFields, workedPlants.size(), maxFieldCount, maxConcurrentPlants);
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
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, false, field.getPosition()));

            final WorkerBuildingModuleView buildingModuleView = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, view -> true);
            if (buildingModuleView != null)
            {
                field.resetOwner();
                amountOfOwnedFields--;
                calculateWorkedPlants();
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
    public BaseComponent getFieldWarningTooltip(IFieldView field)
    {
        if (!FieldModule.checkFieldCount(amountOfOwnedFields, maxFieldCount))
        {
            return new TranslatableComponent(FIELD_LIST_WARN_EXCEEDS_FIELD_COUNT);
        }
        else if (!FieldModule.checkPlantCount(workedPlants.size(), maxConcurrentPlants))
        {
            return new TranslatableComponent(FIELD_LIST_WARN_EXCEEDS_PLANT_COUNT);
        }
        return null;
    }

    /**
     * Getter for the amount of owned fields.
     *
     * @return the amount of owned fields.
     */
    public int getAmountOfOwnedFields()
    {
        return amountOfOwnedFields;
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
}
