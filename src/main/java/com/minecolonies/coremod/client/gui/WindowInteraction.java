package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import net.minecraft.entity.player.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowCitizen.*;

/**
 * Window for the citizen.
 */
public class WindowInteraction extends AbstractWindowSkeleton
{
    /**
     * The citizenData.View object.
     */
    private final ICitizenDataView citizen;

    /**
     * Inventory of the player.
     */
    private final PlayerInventory inventory = this.mc.player.inventory;

    /**
     * Is the player in creative or not.
     */
    private final boolean isCreative = this.mc.player.isCreative();

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowInteraction(final ICitizenDataView citizen)
    {
        super(Constants.MOD_ID + INTERACTION_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(WINDOW_ID_NAME, Label.class).setLabelText(citizen.getName());

        createHealthBar(citizen, findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        createSaturationBar(citizen, this);
        createHappinessBar(citizen, this);
        createXpBar(citizen, this);
        createSkillContent(citizen, this);
        updateHappiness(citizen, this);

        setPage("");
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_REQUESTS:
                findPaneOfTypeByID(VIEW_HEAD, SwitchView.class).nextView();
                buttonPrevPage.off();
                buttonNextPage.off();
                pageNum.off();
                break;
            case BUTTON_BACK:
                findPaneOfTypeByID(VIEW_HEAD, SwitchView.class).previousView();
                setPage("");
                break;
            case INVENTORY_BUTTON_ID:
                Network.getNetwork().sendToServer(new OpenInventoryMessage(citizen.getName(), citizen.getEntityId()));
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }
}
