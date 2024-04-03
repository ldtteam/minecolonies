package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_NAME_TOO_LONG;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for a hut name entry.
 */
public class WindowHutNameEntry extends BOWindow implements ButtonHandler
{
    /**
     * The max length of the name.
     */
    private static final int MAX_NAME_LENGTH = 15;

    /**
     * Resource suffix of GUI xml file.
     */
    private static final String HUT_NAME_RESOURCE_SUFFIX = ":gui/windowhutnameentry.xml";

    /**
     * The building associated to the GUI.
     */
    private final IBuildingView building;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowHutNameEntry(final IBuildingView b)
    {
        super(new ResourceLocation(Constants.MOD_ID + HUT_NAME_RESOURCE_SUFFIX));
        this.building = b;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(Component.translatable(building.getCustomName().toLowerCase(Locale.US)).getString());
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();

            if (name.length() > MAX_NAME_LENGTH)
            {
                name = name.substring(0, MAX_NAME_LENGTH);
                MessageUtils.format(WARNING_NAME_TOO_LONG, name).sendTo(Minecraft.getInstance().player);
            }

            building.setCustomName(name);
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        if (building != null)
        {
            building.openGui(false);
        }
    }
}
