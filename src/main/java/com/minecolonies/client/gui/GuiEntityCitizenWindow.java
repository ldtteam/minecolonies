package com.minecolonies.client.gui;

import com.blockout.Alignment;
import com.blockout.Label;
import com.blockout.Button;
import com.blockout.ButtonVanilla;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

public class GuiEntityCitizenWindow extends GuiMineColoniesWindow implements Button.Handler
{
    private Label   title;
    private Label[] attributes;
    private Button  inventory;

    private CitizenData.View citizen;

    public GuiEntityCitizenWindow(CitizenData.View citizen)
    {
        super();
        this.citizen = citizen;
    }

    public void createGui()
    {
        title = new Label();
        title.setSize(-1, 11);
        title.setPosition(0, 9);
        title.setTextAlignment(Alignment.TopMiddle);
        title.setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"));
        title.setColor(0x0, 0x0);
        //title.setShadowedText(false);
        title.putInside(this);

        attributes = new Label[5];

        int y = 31;
        for (int i = 0; i < attributes.length; ++i)
        {
            Label attribute = new Label();
            attributes[i] = attribute;

            attribute.setColor(0x0, 0x0);
            attribute.setSize(100, 11);
            attribute.setPosition(30, y);
            attribute.putInside(this);

            y += attribute.getHeight();
        }

        attributes[0].setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
        attributes[1].setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
        attributes[2].setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
        attributes[3].setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
        attributes[4].setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));

        inventory = new ButtonVanilla();
        inventory.setSize(116, 20);
        inventory.setAlignment(Alignment.BottomMiddle);
        inventory.setPosition(0, 13);
        inventory.setLabel(LanguageHandler.format("container.inventory"));
        inventory.setHandler(this);
        inventory.putInside(this);
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button == inventory)
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
