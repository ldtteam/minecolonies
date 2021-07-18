package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowBannerPicker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the town hall.
 */
public class WindowSettingsPage extends AbstractWindowTownHall
{
    /**
     * Drop down list for style.
     */
    private DropDownList colorDropDownList;

    /**
     * Constructor for the town hall window.
     *
     * @param townHall {@link BuildingTownHall.View}.
     */
    public WindowSettingsPage(final BuildingTownHall.View townHall)
    {
        super(townHall, "layoutsettings.xml");
        initColorPicker();

        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);
        registerButton(BUTTON_TOGGLE_HOUSING, this::toggleHousing);
        registerButton(BUTTON_TOGGLE_MOVE_IN, this::toggleMoveIn);
        registerButton(BUTTON_TOGGLE_PRINT_PROGRESS, this::togglePrintProgress);
        registerButton("bannerPicker", this::openBannerPicker);

        colorDropDownList.setSelectedIndex(townHall.getColony().getTeamColonyColor().ordinal());
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initColorPicker()
    {
        registerButton(BUTTON_PREVIOUS_COLOR_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_COLOR_ID, this::nextStyle);
        findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class).setEnabled(enabled);
        colorDropDownList = findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class);

        colorDropDownList.setHandler(this::onDropDownListChanged);

        final List<TextFormatting> textColors = Arrays.stream(TextFormatting.values()).filter(TextFormatting::isColor).collect(Collectors.toList());

        colorDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return textColors.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < textColors.size())
                {
                    return textColors.get(index).getName();
                }
                return "";
            }
        });
    }

    /**
     * Called when the dropdownList changed.
     *
     * @param dropDownList the list.
     */
    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        Network.getNetwork().sendToServer(new TeamColonyColorChangeMessage(dropDownList.getSelectedIndex(), building));
    }

    /**
     * Change to the next style.
     */
    private void nextStyle()
    {
        colorDropDownList.selectNext();
    }

    /**
     * Change to the previous style.
     */
    private void previousStyle()
    {
        colorDropDownList.selectPrevious();
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        if (building.getColony().isManualHiring())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (!building.getColony().isPrintingProgress())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_PRINT_PROGRESS, Button.class).setText(LanguageHandler.format(OFF_STRING));
        }

        if (building.getColony().isManualHousing())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_HOUSING, Button.class).setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (building.getColony().canMoveIn())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_MOVE_IN, Button.class).setText(LanguageHandler.format(ON_STRING));
        }
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHiring(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleJobMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHousing(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getTextAsString().equals(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF)))
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleHousingMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles citizens moving in. Off means citizens stop moving in.
     *
     * @param button the pressed button.
     */
    private void toggleMoveIn(@NotNull final Button button)
    {
        final boolean toggle;
        if (button.getTextAsString().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setText(LanguageHandler.format(ON_STRING));
            toggle = true;
        }
        else
        {
            button.setText(LanguageHandler.format(OFF_STRING));
            toggle = false;
        }
        Network.getNetwork().sendToServer(new ToggleMoveInMessage(this.building.getColony(), toggle));
    }

    /**
     * Toggles printing progress.
     *
     * @param button the button to toggle.
     */
    private void togglePrintProgress(@NotNull final Button button)
    {
        if (button.getTextAsString().equals(LanguageHandler.format(OFF_STRING)))
        {
            button.setText(LanguageHandler.format(ON_STRING));
        }
        else
        {
            button.setText(LanguageHandler.format(OFF_STRING));
        }
        Network.getNetwork().sendToServer(new ToggleHelpMessage(this.building.getColony()));
    }

    /**
     * Opens the banner picker window. Window does not use BlockOut, so is started manually.
     * @param button the trigger button
     */
    private void openBannerPicker(@NotNull final Button button)
    {
        Screen window = new WindowBannerPicker(building.getColony(), this);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(window));
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_SETTINGS;
    }
}
