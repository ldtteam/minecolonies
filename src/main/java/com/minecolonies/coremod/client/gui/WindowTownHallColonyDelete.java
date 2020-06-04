package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.ColonyDeleteOwnMessage;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.WindowConstants.TOWNHALL_COLONY_DELETE_GUI;

/**
 * Townhallgui for deleting the owned colony
 */
public class WindowTownHallColonyDelete extends AbstractWindowSkeleton
{
    private static final String BUTTON_CONFIRM = "confirm";

    public WindowTownHallColonyDelete()
    {
        super(MOD_ID + TOWNHALL_COLONY_DELETE_GUI);

        findPaneOfTypeByID("text", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.delete.sure"));
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_CONFIRM))
        {
            // Delete colony
            Network.getNetwork().sendToServer(new ColonyDeleteOwnMessage());
        }

        close();
    }
}
