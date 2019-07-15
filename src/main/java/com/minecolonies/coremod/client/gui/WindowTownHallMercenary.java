package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Image;
import com.minecolonies.blockout.controls.Text;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.HireMercenaryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
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
    private final ColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallMercenary(final ColonyView c)
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
            MineColonies.getNetwork().sendToServer(new HireMercenaryMessage(colony));
            Minecraft.getMinecraft().player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 1.0f, 1.0f);
        }

        this.close();
    }
}
