package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.views.DropDownList;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowBannerPicker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.event.TextureReloadListener.TEXTURE_PACKS;

/**
 * BOWindow for the town hall.
 */
public class WindowSettingsPage extends AbstractWindowTownHall
{
    /**
     * Is the special feature unlocked.
     */
    private static AtomicBoolean isFeatureUnlocked = new AtomicBoolean(false);

    /**
     * Drop down list for style.
     */
    private DropDownList colorDropDownList;

    /**
     * Drop down list for style.
     */
    private DropDownList textureDropDownList;

    /**
     * The initial texture index.
     */
    private int initialTextureIndex;

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
        textureDropDownList.setSelectedIndex(TEXTURE_PACKS.indexOf(townHall.getColony().getTextureStyleId()));
        this.initialTextureIndex = textureDropDownList.getSelectedIndex();

        checkFeatureUnlock();
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

        final List<ChatFormatting> textColors = Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(Collectors.toList());

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

        textureDropDownList = findPaneOfTypeByID(DROPDOWN_TEXT_ID, DropDownList.class);
        textureDropDownList.setHandler(this::toggleTexture);
        textureDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return TEXTURE_PACKS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return TEXTURE_PACKS.get(index);
            }
        });
    }

    /**
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleTexture(final DropDownList dropDownList)
    {
        if (dropDownList.getSelectedIndex() != initialTextureIndex)
        {
            Network.getNetwork().sendToServer(new ColonyTextureStyleMessage(building.getColony(), TEXTURE_PACKS.get(dropDownList.getSelectedIndex())));
        }
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
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (!building.getColony().isPrintingProgress())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_PRINT_PROGRESS, Button.class).setText(Component.translatable(OFF_STRING));
        }

        if (building.getColony().isManualHousing())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_HOUSING, Button.class).setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
        }

        if (building.getColony().canMoveIn())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_MOVE_IN, Button.class).setText(Component.translatable(ON_STRING));
        }
    }

    /**
     * Toggles the allocation of a certain job. Manual or automatic.
     *
     * @param button the pressed button.
     */
    private void toggleHiring(@NotNull final Button button)
    {
        String key = button.getText().getContents() instanceof TranslatableContents ? ((TranslatableContents) button.getText().getContents()).getKey() : button.getTextAsString();

        final boolean toggle;
        if (key.equals(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF))
        {
            button.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
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
        String key = button.getText().getContents() instanceof TranslatableContents ? ((TranslatableContents) button.getText().getContents()).getKey() : button.getTextAsString();

        final boolean toggle;
        if (key.equals(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF))
        {
            button.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
            toggle = true;
        }
        else
        {
            button.setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_OFF));
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
        String key = button.getText().getContents() instanceof TranslatableContents ? ((TranslatableContents) button.getText().getContents()).getKey() : button.getTextAsString();

        final boolean toggle;
        if (key.equals(OFF_STRING))
        {
            button.setText(Component.translatable(ON_STRING));
            toggle = true;
        }
        else
        {
            button.setText(Component.translatable(OFF_STRING));
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
        String key = button.getText().getContents() instanceof TranslatableContents ? ((TranslatableContents) button.getText().getContents()).getKey() : button.getTextAsString();

        if (key.equals(OFF_STRING))
        {
            button.setText(Component.translatable(ON_STRING));
        }
        else
        {
            button.setText(Component.translatable(OFF_STRING));
        }
        Network.getNetwork().sendToServer(new ToggleHelpMessage(this.building.getColony()));
    }

    /**
     * Opens the banner picker window. BOWindow does not use blockui, so is started manually.
     *
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

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        final Pane pane = findPaneByID(DROPDOWN_TEXT_ID);
        if (isFeatureUnlocked.get())
        {
            pane.enable();
        }
        else
        {
            pane.disable();
            AbstractTextBuilder.TooltipBuilder hoverText = PaneBuilders.tooltipBuilder().hoverPane(pane);
            hoverText.append(Component.translatable("com.minecolonies.core.townhall.patreon")).paragraphBreak();
            hoverText.build();
        }
    }

    /**
     * Check if the feature is unlocked through the patreon API.
     */
    public void checkFeatureUnlock()
    {
        final String player = Minecraft.getInstance().player.getStringUUID();
        new Thread(() -> {
            try
            {
                final SSLSocketFactory sslsocketfactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                final URL url = new URL("https://auth.minecolonies.com/api/minecraft/" + player + "/features");
                final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                conn.setSSLSocketFactory(sslsocketfactory);

                final InputStream responseBody = conn.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody));

                String inputLine;
                final StringBuilder response = new StringBuilder();

                while ((inputLine = reader.readLine()) != null)
                {
                    response.append(inputLine);
                }
                reader.close();
                isFeatureUnlocked.set(Boolean.parseBoolean(response.toString()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }
}
