package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Button;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.network.messages.server.colony.ColonyDeleteOwnMessage;
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
