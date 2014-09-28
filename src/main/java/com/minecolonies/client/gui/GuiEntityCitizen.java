package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiEntityCitizen extends GuiBase
{
    private final int BUTTON_INVENTORY = 0;
    private EntityCitizen citizen;

    public GuiEntityCitizen(EntityCitizen entityCitizen, EntityPlayer player, World world)
    {
        super(player, world, (int) entityCitizen.posX, (int) entityCitizen.posY, (int) entityCitizen.posZ, null);
        this.citizen = entityCitizen;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        int strength = 0;
        int stamina = 0;
        int wisdom = 0;
        int intelligence = 0;
        int charisma = 0;

        if (citizen.getCitizenData() != null)
        {
            strength = citizen.getCitizenData().strength;
            stamina = citizen.getCitizenData().stamina;
            wisdom = citizen.getCitizenData().wisdom;
            intelligence = citizen.getCitizenData().intelligence;
            charisma = citizen.getCitizenData().charisma;
        }

        String strengthStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", strength);
        String staminaStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", stamina);
        String wisdomStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", wisdom);
        String intelligenceStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", intelligence);
        String charismaStr = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", charisma);

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
