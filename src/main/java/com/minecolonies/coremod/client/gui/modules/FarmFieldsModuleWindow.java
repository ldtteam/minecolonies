package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.coremod.colony.fields.FarmField;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRING_ON;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_LABEL_FIELD_COUNT;

/**
 * BOWindow for the fields tab in huts.
 */
public class FarmFieldsModuleWindow extends AbstractModuleWindow
{
    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_FIELDS_RESOURCE_SUFFIX = ":gui/layouthuts/layoutfarmfields.xml";

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
    public FarmFieldsModuleWindow(final IBuildingView building, final FieldsModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_FIELDS_RESOURCE_SUFFIX);
        this.moduleView = moduleView;

        registerButton(TAG_BUTTON_ASSIGNMENT_MODE, this::assignmentModeClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
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
        final IField field = moduleView.getFields().get(row);
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
          .setText(Component.translatable(moduleView.assignFieldManually() ? COM_MINECOLONIES_COREMOD_GUI_HIRING_ON : COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
        findPaneOfTypeByID(TAG_FIELD_COUNT, Text.class)
          .setText(Component.translatable(FIELD_LIST_LABEL_FIELD_COUNT, moduleView.getOwnedFields().size(), moduleView.getMaxFieldCount()));
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
                return getFields().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final FarmField field = getFields().get(index);
                final String distance = Integer.toString(field.getDistance(buildingView));
                final Component direction = BlockPosUtil.calcDirection(buildingView.getPosition(), field.getPosition());

                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(Component.translatable(distance + "m"));
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
                        assignButton.setText(Component.translatable(RED_X).withStyle(ChatFormatting.RED));
                    }
                    else
                    {
                        assignButton.setText(Component.translatable(APPROVE).withStyle(ChatFormatting.GREEN));

                        if (!moduleView.canAssignField(field))
                        {
                            assignButton.disable();

                            MutableComponent warningTooltip = moduleView.getFieldWarningTooltip(field);
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

                if (field.getSeed() != null)
                {
                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(field.getSeed());
                }
            }
        });

        updateUI();
    }

    /**
     * Fetches the list of fields, specifically filtered for the farm field.
     *
     * @return the filtered list of fields.
     */
    private List<FarmField> getFields()
    {
        return this.moduleView.getFields().stream()
                 .filter(f -> f instanceof FarmField farmField && !farmField.getSeed().isEmpty())
                 .map(m -> (FarmField) m)
                 .toList();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateUI();
    }
}