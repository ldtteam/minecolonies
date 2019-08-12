package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.api.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.HireMercenaryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_DONE;

/**
 * Gui for hiring mercenaries
 */
public class WindowTownHallMercenary extends Window implements ButtonHandler
{
    /**
     * The xml file for this gui
     */
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/townhall/windowtownhallmercenary.xml";

    /**
     * The client side colony data
     */
    private final IColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallMercenary(final IColonyView c)
    {
        super(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX);
        this.colony = c;
        findPaneOfTypeByID("text", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.townHall.mercenaryStory"));

        int amountOfMercenaries = colony.getCitizenCount();
        amountOfMercenaries = amountOfMercenaries / 10;
        amountOfMercenaries += 3;

        int startX = 160;
        final int startY = 40;

        for (int i = 0; i < amountOfMercenaries; i++)
        {
            final Image newImage = new Image();
            newImage.setImage("minecolonies:textures/entity_icon/citizenmale3.png");
            newImage.setSize(10, 10);
            newImage.setPosition(startX, startY);
            this.addChild(newImage);

            startX += 15;
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            colony.usedMercenaries();
            Network.getNetwork().sendToServer(new HireMercenaryMessage(colony));
            Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }

        this.close();
    }
}
