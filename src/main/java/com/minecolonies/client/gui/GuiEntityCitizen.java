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

    public GuiEntityCitizen(EntityCitizen entityCitizen, CitizenData.View citizen, EntityPlayer player, World world)
    {
        super(player, world, (int) entityCitizen.posX, (int) entityCitizen.posY, (int) entityCitizen.posZ, null);
        this.citizen = citizen;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        String strengthStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength);
        String staminaStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina);
        String wisdomStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom);
        String intelligenceStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence);
        String charismaStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma);

        int x = getSameCenterX(strengthStr, staminaStr, wisdomStr, intelligenceStr, charismaStr);
        int y = topY + labelSpan;

        addCenteredLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"), y);
        addLabel(strengthStr, x, y += labelSpan * 2);
        addLabel(staminaStr, x, y += labelSpan);
        addLabel(wisdomStr, x, y += labelSpan);
        addLabel(intelligenceStr, x, y += labelSpan);
        addLabel(charismaStr, x, y += labelSpan);
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
