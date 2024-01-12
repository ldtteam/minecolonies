package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.structurize.client.gui.WindowSwitchPack;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowBannerPicker;
import com.minecolonies.coremod.client.gui.map.WindowColonyMap;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.colony.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.ON_STRING;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.event.TextureReloadListener.TEXTURE_PACKS;

import net.minecraft.ChatFormatting;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BOWindow for the town hall.
 */
public class WindowMainPage extends AbstractWindowTownHall
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
     * Drop down list for name style.
     */
    private DropDownList nameStyleDropDownList;

    /**
     * The initial texture index.
     */
    private int initialTextureIndex;

    /**
     * The initial texture index.
     */
    private int initialNamePackIndex;



    /**
     * Label for the colony name.
     */
    private final Text title;


    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowMainPage(final BuildingTownHall.View building)
    {
        super(building, "layoutactions.xml");
        initDropDowns();

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);

        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_MERCENARY, this::mercenaryClicked);
        registerButton(BUTTON_TOWNHALLMAP, this::mapButtonClicked);
        registerButton(BUTTON_PATREON, this::patreonClicked);
        registerButton(BUTTON_TOGGLE_JOB, this::toggleHiring);
        registerButton(BUTTON_TOGGLE_HOUSING, this::toggleHousing);
        registerButton(BUTTON_TOGGLE_MOVE_IN, this::toggleMoveIn);

        registerButton(BUTTON_COLONY_SWITCH_STYLE, this::switchPack);

        findPaneOfTypeByID(BUTTON_COLONY_SWITCH_STYLE, ButtonImage.class).setText(Component.literal(building.getColony().getStructurePack()));
        registerButton(BUTTON_BANNER_PICKER, this::openBannerPicker);
        registerButton(BUTTON_RESET_TEXTURE, this::resetTextureStyle);

        this.colorDropDownList.setSelectedIndex(building.getColony().getTeamColonyColor().ordinal());
        this.textureDropDownList.setSelectedIndex(TEXTURE_PACKS.indexOf(building.getColony().getTextureStyleId()));
        this.initialTextureIndex = textureDropDownList.getSelectedIndex();

        this.nameStyleDropDownList.setSelectedIndex(building.getColony().getNameFileIds().indexOf(building.getColony().getNameStyle()));
        this.initialNamePackIndex = nameStyleDropDownList.getSelectedIndex();

        checkFeatureUnlock();

        if (building.getColony().isManualHiring())
        {
            findPaneOfTypeByID(BUTTON_TOGGLE_JOB, Button.class).setText(Component.translatable(COM_MINECOLONIES_COREMOD_GUI_HIRING_ON));
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
        String key = button.getText().getContents() instanceof TranslatableContents contents ? contents.getKey() : button.getTextAsString();

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
        String key = button.getText().getContents() instanceof TranslatableContents contents ? contents.getKey() : button.getTextAsString();

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
        String key = button.getText().getContents() instanceof TranslatableContents contents ? contents.getKey() : button.getTextAsString();

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
     * Switch the structure style pack.
     */
    private void switchPack()
    {
        new WindowSwitchPack(() -> {
            building.getColony().setStructurePack(StructurePacks.selectedPack.getName());
            Network.getNetwork().sendToServer(new ColonyStructureStyleMessage(building.getColony(), StructurePacks.selectedPack.getName()));
            return new WindowMainPage((BuildingTownHall.View) this.building);
        }).open();
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initDropDowns()
    {
        findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class).setEnabled(enabled);

        colorDropDownList = findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class);
        colorDropDownList.setHandler(this::onDropDownListChanged);

        final List<ChatFormatting> textColors = Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).toList();

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
                    final String colorName = textColors.get(index).getName().replace("_", " ");
                    return colorName.substring(0, 1).toUpperCase(Locale.US) + colorName.substring(1);
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

        nameStyleDropDownList = findPaneOfTypeByID(DROPDOWN_NAME_ID, DropDownList.class);
        nameStyleDropDownList.setHandler(this::toggleNameFile);
        nameStyleDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getColony().getNameFileIds().size();
            }

            @Override
            public String getLabel(final int index)
            {
                return building.getColony().getNameFileIds().get(index);
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
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleNameFile(final DropDownList dropDownList)
    {
        if (dropDownList.getSelectedIndex() != initialNamePackIndex)
        {
            Network.getNetwork().sendToServer(new ColonyNameStyleMessage(building.getColony(), building.getColony().getNameFileIds().get(dropDownList.getSelectedIndex())));
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
     * Opens the banner picker window. BOWindow does not use blockui, so is started manually.
     *
     * @param button the trigger button
     */
    private void openBannerPicker(@NotNull final Button button)
    {
        Screen window = new WindowBannerPicker(building.getColony(), this, isFeatureUnlocked);
        Minecraft.getInstance().setScreen(window);
    }

    /**
     * Reset the texture style.
     */
    private void resetTextureStyle()
    {
        Network.getNetwork().sendToServer(new ColonyTextureStyleMessage(building.getColony(), TEXTURE_PACKS.get(0)));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        final Pane textPane = findPaneByID(DROPDOWN_TEXT_ID);
        final Pane namePane = findPaneByID(DROPDOWN_NAME_ID);
        final Pane resetButton = findPaneByID(BUTTON_RESET_TEXTURE);

        if (isFeatureUnlocked.get())
        {
            textPane.enable();
            namePane.enable();
            textPane.show();
            resetButton.hide();
        }
        else
        {
            textPane.disable();
            namePane.disable();

            if (!building.getColony().getTextureStyleId().equals("default"))
            {
                resetButton.show();
                textPane.hide();
            }
            else
            {
                textPane.show();
            }

            PaneBuilders.tooltipBuilder().hoverPane(textPane).append(Component.translatable("com.minecolonies.core.townhall.patreon.textures"))
              .paragraphBreak()
              .appendNL(Component.empty())
              .appendNL(Component.translatable("com.minecolonies.core.townhall.patreon"))
              .paragraphBreak()
              .build();

            PaneBuilders.tooltipBuilder().hoverPane(namePane)
              .append(Component.translatable("com.minecolonies.core.townhall.patreon.names")).paragraphBreak()
              .appendNL(Component.empty())
              .appendNL(Component.translatable("com.minecolonies.core.townhall.patreon")).paragraphBreak()
              .build();
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

    /**
     * On Patreon button clicked. Open website link to patreon.
     */
    private void patreonClicked()
    {
        Minecraft.getInstance().setScreen(new ConfirmLinkScreen((check) -> {
            if (check) {
                Util.getPlatform().openUri("https://www.patreon.com/Minecolonies");
            }

            Minecraft.getInstance().setScreen(this.screen);
        }, "https://www.patreon.com/Minecolonies", true));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        title.setText(Component.literal(building.getColony().getName()));

        if (building.getColony().getMercenaryUseTime() != 0
              && building.getColony().getWorld().getGameTime() - building.getColony().getMercenaryUseTime() < TICKS_FOURTY_MIN)
        {
            findPaneOfTypeByID(BUTTON_MERCENARY, Button.class).disable();
        }
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        @NotNull final WindowTownHallNameEntry window = new WindowTownHallNameEntry(building.getColony());
        window.open();
    }

    /**
     * Action performed when mercenary button is clicked.
     */
    private void mercenaryClicked()
    {
        @NotNull final WindowTownHallMercenary window = new WindowTownHallMercenary(building.getColony());
        window.open();
    }

    /**
     * Opens the map on button clicked
     */
    private void mapButtonClicked()
    {
        @NotNull final WindowColonyMap window = new WindowColonyMap(building);
        window.open();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ACTIONS;
    }
}
