package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiEntityCitizen extends GuiBase
{
    private final int BUTTON_INVENTORY = 0;
    private CitizenData.View citizen;

    public GuiEntityCitizen(CitizenData.View citizen)
    {
        super();
        this.citizen = citizen;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        String[] attributes = new String[5];
        attributes[0] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength);
        attributes[1] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina);
        attributes[2] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom);
        attributes[3] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence);
        attributes[4] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma);

        int x = getSameCenterX(attributes);
        int y = topY + labelSpan;

        addCenteredLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"), y);
        y += labelSpan;
        for (String attr : attributes)
        {
            addLabel(attr, x, y += labelSpan);
        }
        addBottomButton(BUTTON_INVENTORY, LanguageHandler.format("container.inventory"), buttonMiddleX, buttonWidth, buttonHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case BUTTON_INVENTORY:
                MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
                break;
        }
    }
}
