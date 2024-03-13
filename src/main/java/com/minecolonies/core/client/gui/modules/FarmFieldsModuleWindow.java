package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockPosUtil.DirectionResult;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.core.colony.fields.FarmField;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.*;

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
     * ID of the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGN = "assign";

    /**
     * ID of the assignmentMode button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGNMENT_MODE = "assignmentMode";

    /**
     * ID of the field count label inside the GUI.
     */
    private static final String TAG_FIELD_COUNT = "fieldCount";

    /**
     * ID of the icon inside the GUI.
     */
    private static final String TAG_ICON = "icon";

    /**
     * Texture of the assign button when it's on.
     */
    private static final String TEXTURE_ASSIGN_ON_NORMAL = "minecolonies:textures/gui/builderhut/builder_button_mini_check.png";

    /**
     * Texture of the assign button when it's on and disabled.
     */
    private static final String TEXTURE_ASSIGN_ON_DISABLED = "minecolonies:textures/gui/builderhut/builder_button_mini_disabled_check.png";

    /**
     * Texture of the assign button when it's off.
     */
    private static final String TEXTURE_ASSIGN_OFF_NORMAL = "minecolonies:textures/gui/builderhut/builder_button_mini.png";

    /**
     * Texture of the assign button when it's off and disabled.
     */
    private static final String TEXTURE_ASSIGN_OFF_DISABLED = "minecolonies:textures/gui/builderhut/builder_button_mini_disabled.png";

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
          .setText(Component.translatableEscape(moduleView.assignFieldManually() ? COM_MINECOLONIES_COREMOD_GUI_HIRING_ON : COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
        findPaneOfTypeByID(TAG_FIELD_COUNT, Text.class)
          .setText(Component.translatableEscape(FIELD_LIST_LABEL_FIELD_COUNT, moduleView.getOwnedFields().size(), moduleView.getMaxFieldCount()));
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
                final IField field = moduleView.getFields().get(index);
                if (field instanceof FarmField farmField && !farmField.getSeed().isEmpty())
                {
                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(farmField.getSeed());
                }

                final String distance = Integer.toString(field.getSqDistance(buildingView));
                final DirectionResult direction = BlockPosUtil.calcDirection(buildingView.getPosition(), field.getPosition());

                final Component directionText = switch (direction) {
                    case UP, DOWN -> direction.getLongText();
                    default -> Component.translatableEscape(FIELD_LIST_LABEL_DISTANCE, Component.literal(distance + "m"), direction.getShortText());
                };

                rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(directionText);

                final ButtonImage assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, ButtonImage.class);
                assignButton.setEnabled(moduleView.assignFieldManually());
                assignButton.show();
                assignButton.setHoverPane(null);

                if (field.isTaken())
                {
                    setAssignButtonTexture(assignButton, true);
                }
                else
                {
                    // Field may be claimed
                    setAssignButtonTexture(assignButton, false);

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
        });

        updateUI();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateUI();
    }

    /**
     * Updates the assign button texture.
     *
     * @param button the button instance.
     * @param isOn   whether the button is on or off.
     */
    private void setAssignButtonTexture(final ButtonImage button, boolean isOn)
    {
        if (isOn)
        {
            button.setImage(new ResourceLocation(TEXTURE_ASSIGN_ON_NORMAL));
            button.setImageDisabled(new ResourceLocation(TEXTURE_ASSIGN_ON_DISABLED));
        }
        else
        {
            button.setImage(new ResourceLocation(TEXTURE_ASSIGN_OFF_NORMAL));
            button.setImageDisabled(new ResourceLocation(TEXTURE_ASSIGN_OFF_DISABLED));
        }
    }
}