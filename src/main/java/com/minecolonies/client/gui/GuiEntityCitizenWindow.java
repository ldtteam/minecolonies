package com.minecolonies.client.gui;

import com.blockout.*;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

public class GuiEntityCitizenWindow extends GuiMineColoniesWindow implements Button.Handler
{
    private String INVENTORY_BUTTON_ID = "inventory";

    private CitizenData.View citizen;

    public GuiEntityCitizenWindow(CitizenData.View citizen)
    {
        super();
        this.citizen = citizen;

        Label title = new Label();
        title.setSize(-1, 11);
        title.setPosition(0, 9);
        title.setTextAlignment(Alignment.TopMiddle);
        title.setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"));
        title.setColor(0x0);
        title.putInside(this);

        String [] attributes = new String[5];
        attributes[0] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength);
        attributes[1] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina);
        attributes[2] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom);
        attributes[3] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence);
        attributes[4] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma);

        int y = 31;
        for (String attr : attributes)
        {
            Label label = new Label();

            label.setSize(100, 11);
            label.setPosition(30, y);
            label.setColor(0x0);
            label.setLabel(attr);
            label.putInside(this);

            y += label.getHeight();
        }

        for (int i = 0; i < 2; ++i)
        {
            TextField text = new TextFieldVanilla();
            text.setSize(-1, 20);
            text.setPosition(0, y);
            text.setText("This is just a test");
            text.putInside(this);

            y += text.getHeight();
        }

        Button inventory = new ButtonVanilla();
        inventory.setID(INVENTORY_BUTTON_ID);
        inventory.setSize(116, 20);
        inventory.setPosition(0, 13);
        inventory.setAlignment(Alignment.BottomMiddle);
        inventory.setLabel(LanguageHandler.format("container.inventory"));
        inventory.setHandler(this);
        inventory.putInside(this);
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID() == INVENTORY_BUTTON_ID)
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
