package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldsModuleView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRING_ON;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_LABEL_FIELD_COUNT;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_LABEL_PLANT_COUNT;

/**
 * BOWindow for the fields tab in huts.
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
     * The field module view.
     */
    private final FieldsModuleView moduleView;

    /**
     * ScrollList with the fields.
     */
    private ScrollingList fieldList;

    /**
     * Constructor for the window.
     *
     * @param moduleView {@link FieldsModuleView}.
     */
    public FieldsModuleWindow(final IBuildingView building, final FieldsModuleView moduleView)
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
          .setText(new TranslatableComponent(FIELD_LIST_LABEL_FIELD_COUNT, moduleView.getOwnedFields().size(), moduleView.getMaxFieldCount()));
        findPaneOfTypeByID(TAG_PLANT_COUNT, Text.class)
          .setText(new TranslatableComponent(FIELD_LIST_LABEL_PLANT_COUNT, moduleView.getWorkedPlants().size(), moduleView.getMaxConcurrentPlants()));
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
                final String distance = Integer.toString(field.getDistance(buildingView));
                final Component direction = BlockPosUtil.calcDirection(buildingView.getPosition(), field.getPosition());

                final boolean canAddField = moduleView.canAssignField(field);

                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(new TextComponent(distance + "m"));
                rowPane.findPaneOfTypeByID(TAG_DIRECTION, Text.class).setText(direction);

                final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);
                assignButton.setEnabled(moduleView.assignFieldManually());
                assignButton.setHoverPane(null);
                assignButton.show();

                if (field.isTaken() && !buildingView.getID().equals(field.getBuildingId()))
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

                            BaseComponent warningTooltip = moduleView.getFieldWarningTooltip(field);
                            if (warningTooltip != null && moduleView.assignFieldManually())
                            {
                                PaneBuilders.tooltipBuilder()
                                  .append(warningTooltip.withStyle(ChatFormatting.RED))
                                  .hoverPane(assignButton)
                                  .build();
                            }
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