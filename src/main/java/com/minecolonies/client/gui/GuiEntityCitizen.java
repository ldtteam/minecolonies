package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.packets.OpenInventoryPacket;
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
        super(player, world, (int) entityCitizen.posX, (int) entityCitizen.posY, (int) entityCitizen.posZ);
        this.citizen = entityCitizen;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        String strength = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength);
        String stamina = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina);
        String wisdom = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom);
        String intelligence = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence);
        String charisma = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma);

        int x = getSameCenterX(strength, stamina, wisdom, intelligence, charisma);
        int y = topY + labelSpan;

        addCenteredLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"), y);
        addLabel(strength, x, y += labelSpan * 2);
        addLabel(stamina, x, y += labelSpan);
        addLabel(wisdom, x, y += labelSpan);
        addLabel(intelligence, x, y += labelSpan);
        addLabel(charisma, x, y += labelSpan);
        addBottomButton(BUTTON_INVENTORY, LanguageHandler.format("container.inventory"), buttonMiddleX, buttonWidth, buttonHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case BUTTON_INVENTORY:
                MineColonies.packetPipeline.sendToServer(new OpenInventoryPacket(citizen.getInventory(), citizen.getCustomNameTag()));
                break;
        }
    }
}
