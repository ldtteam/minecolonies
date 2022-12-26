package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldModuleView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * BOWindow for the farmer hut.
 */
public class FieldsModuleWindow extends AbstractModuleWindow
{
    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FIELDS_RESOURCE_SUFFIX = ":gui/layouthuts/layoutfields.xml";

    /**
     * ID of the fields list inside the GUI.
     */
    private static final String LIST_FIELDS = "fields";

    /**
     * ID of the worker label inside the GUI.
     */
    private static final String TAG_WORKER = "worker";

    /**
     * ID of the distance label inside the GUI.
     */
    private static final String TAG_DISTANCE = "dist";

    /**
     * ID of the direction label inside the GUI.
     */
    private static final String TAG_DIRECTION = "dir";

    /**
     * ID of the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGN = "assignFarm";

    /**
     * ID of the assignmentMode button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGNMENT_MODE = "assignmentMode";

    /**
     * ID of the field count label inside the GUI.
     */
    private static final String TAG_FIELD_COUNT = "fieldCount";

    /**
     * ID of the plant count label inside the GUI.
     */
    private static final String TAG_PLANT_COUNT = "plantCount";

    /**
     * String which displays the release of a field.
     */
    private static final String RED_X = "X";

    /**
     * String which displays adding a field.
     */
    private static final String APPROVE = "✓";

    /**
     * ID of the icon inside the GUI.
     */
    private static final String TAG_ICON = "icon";

    /**
     * The farmer module view.
     */
    private final FieldModuleView moduleView;

    /**
     * ScrollList with the fields.
     */
    private ScrollingList fieldList;

    /**
     * Constructor for the window of the farmer.
     *
     * @param moduleView {@link FieldModuleView}.
     */
    public FieldsModuleWindow(final IBuildingView building, final FieldModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_FIELDS_RESOURCE_SUFFIX);
        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
        this.moduleView = moduleView;
    }

    /**
     * Fired when the assignment mode has been toggled.
     *
     * @param button clicked button.
     */
    private void assignmentModeClicked(@NotNull final Button button)
    {
        moduleView.setAssignFieldManually(!moduleView.assignFieldManually());
        updateUI();
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = fieldList.getListElementIndexByPane(button);
        final IFieldView field = moduleView.getFields().get(row);
        if (field.isTaken())
        {
            moduleView.freeField(field);
        }
        else
        {
            moduleView.assignField(field);
        }
        updateUI();
    }

    /**
     * Contains the logic to refresh the UI whenever something changes.
     */
    private void updateUI()
    {
        findPaneOfTypeByID(TAG_BUTTON_ASSIGNMENT_MODE, Button.class)
          .setText(new TranslatableComponent(moduleView.assignFieldManually() ? COM_MINECOLONIES_COREMOD_GUI_HIRING_ON : COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
        findPaneOfTypeByID(TAG_FIELD_COUNT, Text.class)
          .setText(new TranslatableComponent(LABEL_FIELD_LIST_FIELD_COUNT, moduleView.getAmountOfOwnedFields(), moduleView.getMaxFieldCount()));
        findPaneOfTypeByID(TAG_PLANT_COUNT, Text.class)
          .setText(new TranslatableComponent(LABEL_FIELD_LIST_PLANT_COUNT, moduleView.getWorkedPlants().size(), moduleView.getMaxConcurrentPlants()));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        fieldList = findPaneOfTypeByID(LIST_FIELDS, ScrollingList.class);
        fieldList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return moduleView.getFields().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IFieldView field = moduleView.getFields().get(index);
                final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(field.getPosition(), buildingView.getPosition())));
                final Component direction = BlockPosUtil.calcDirection(buildingView.getPosition(), field.getPosition());

                final boolean canAddField = moduleView.canAddField();

                final ICitizen owner = field.getOwnerId() != null ? buildingView.getColony().getCitizen(field.getOwnerId()) : null;
                final Component ownerText = owner == null
                                              ? new TextComponent("<")
                                                  .append(new TranslatableComponent(COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_FARMER_HUT_UNUSED))
                                                  .append(">")
                                              : new TextComponent(owner.getName());

                rowPane.findPaneOfTypeByID(TAG_WORKER, Text.class).setText(ownerText);
                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(new TextComponent(distance + "m"));
                rowPane.findPaneOfTypeByID(TAG_DIRECTION, Text.class).setText(direction);

                final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);
                assignButton.setEnabled(moduleView.assignFieldManually());
                assignButton.show();

                if (owner != null && !buildingView.getAllAssignedCitizens().contains(owner.getId()))
                {
                    assignButton.hide();
                }
                else
                {
                    if (field.isTaken())
                    {
                        assignButton.setText(new TextComponent(RED_X).withStyle(ChatFormatting.RED));
                    }
                    else
                    {
                        assignButton.setText(new TextComponent(APPROVE).withStyle(ChatFormatting.GREEN));

                        if (!canAddField)
                        {
                            assignButton.disable();
                        }
                    }
                }

                if (field.getPlant() != null)
                {
                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(new ItemStack(field.getPlant()));
                }
            }
        });

        updateUI();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateUI();
    }
}
