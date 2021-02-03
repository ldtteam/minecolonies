package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.resources.I18n;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_INFO_PREFIX;
import static com.minecolonies.api.util.constant.WindowConstants.*;

// TODO: rework this to use switch element (fill it in constructor) and default page handle
public class WindowInfo extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowinfo.xml";

    /**
     * The view of the current building.
     */
    private final IBuildingView building;

    /**
     * The current info page.
     */
    private int infoIndex;

    private final Text   name;
    private final Text   info;
    private final Button infoNextPage;
    private final Button infoPrevPage;

    private final String translationPrefix;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param building The building the info window is for.
     */
    public WindowInfo(final IBuildingView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);

        this.building = building;
        this.translationPrefix = COM_MINECOLONIES_INFO_PREFIX + building.getSchematicName() + ".";

        registerButton(BUTTON_EXIT, this::exitClicked);
        registerButton(BUTTON_INFO_NEXT_PAGE, this::nextPage);
        registerButton(BUTTON_INFO_PREV_PAGE, this::prevPage);

        this.infoNextPage = findPaneOfTypeByID(BUTTON_INFO_NEXT_PAGE, Button.class);
        this.infoPrevPage = findPaneOfTypeByID(BUTTON_INFO_PREV_PAGE, Button.class);
        this.name = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);
        this.info = findPaneOfTypeByID(LABEL_INFO, Text.class);

        this.infoIndex = 0;
        refreshPage();
    }

    private void exitClicked()
    {
        building.openGui(false);
    }

    private void refreshPage()
    {
        if (I18n.hasKey(this.translationPrefix + (this.infoIndex + 1)) && !I18n.format(this.translationPrefix + (this.infoIndex + 1)).equals(""))
        {
            this.infoNextPage.enable();
        }
        else
        {
            this.infoNextPage.disable();
        }

        if (I18n.hasKey(this.translationPrefix + (this.infoIndex - 1)) && !I18n.format(this.translationPrefix + (this.infoIndex - 1)).equals(""))
        {
            this.infoPrevPage.enable();
        }
        else
        {
            this.infoPrevPage.disable();
        }

        pageNum.setText(Integer.toString(this.infoIndex));
        this.name.setText(LanguageHandler.format(this.translationPrefix + this.infoIndex + ".name"));

        //We replace escaped newlines to real ones because we want them to be usable and I18n escapes all newlines sadly.
        this.info.setText(LanguageHandler.format(this.translationPrefix + this.infoIndex).replace("\\n", "\n"));
    }

    private void nextPage()
    {
        this.infoIndex++;

        refreshPage();
    }

    private void prevPage()
    {
        this.infoIndex--;

        refreshPage();
    }
}