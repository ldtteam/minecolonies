package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.Group;
import com.ldtteam.blockout.views.SwitchView;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

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
     * The current interactions.
     */
    private final List<IInteractionResponseHandler> interactions;

    /**
     * The current interaction in the list.
     */
    private int currentInteraction = 0;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowInteraction(final ICitizenDataView citizen)
    {
        super(Constants.MOD_ID + INTERACTION_RESOURCE_SUFFIX);
        this.citizen = citizen;
        this.interactions = citizen.getOrderedInteractions();
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        if (!interactions.isEmpty())
        {
            final IInteractionResponseHandler handler = interactions.get(currentInteraction);
            final Group group = findPaneOfTypeByID("responseOptions", Group.class);
            int y = 0;
            findPaneOfTypeByID(CHAT_LABEL_ID, Label.class).setLabelText(citizen.getName() + ": " + handler.getInquiry().getFormattedText());
            for (final ITextComponent component : handler.getPossibleResponses())
            {
                final ButtonImage button = new ButtonImage();
                button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
                button.setLabel(component.getFormattedText());
                button.setSize(86,17);
                button.setTextColor(100);
                button.setPosition(0, y+= button.getHeight());
                group.addChild(button);
            }
        }

        /*createHealthBar(citizen, findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        createSaturationBar(citizen, this);
        createHappinessBar(citizen, this);
        createXpBar(citizen, this);
        createSkillContent(citizen, this);
        updateHappiness(citizen, this);*/
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
