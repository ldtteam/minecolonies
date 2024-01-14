package com.minecolonies.core.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.network.messages.server.ReactivateBuildingMessage;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to reactivate a building.
 */
public class WindowReactivateBuilding extends AbstractWindowSkeleton
{
    /*
     * Building the worker is trying to place.
     */
    @NotNull
    private final BlockPos pos;

    /**
     * Creates a new instance of this window.
     * @param pos the position of the building.
     */
    public WindowReactivateBuilding(@NotNull final BlockPos pos)
    {
        super(Constants.MOD_ID + REACTIVATE_BUILDING_SOURCE_SUFFIX);
        this.pos = pos;
        registerButton(BUTTON_REACTIVATE, this::reactivateClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
    }

    /**
     * Reactivate the building.
     */
    private void reactivateClicked()
    {
        Network.getNetwork().sendToServer(new ReactivateBuildingMessage(pos));
        close();
    }


    /**
     * Cancel reactivation.
     */
    private void cancelClicked()
    {
        close();
    }
}
