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

        String xml =
                "<window>\n" +
                "    <label id=\"title\"\n" +
                "            width=\"-1\" height=\"11\" x=\"0\" y=\"9\"\n" +
                "            textalign=\"TopMiddle\"\n" +
                "            color=\"0\"\n" +
                "            label=\"$(com.minecolonies.gui.citizen.skills)\" />\n" +
                "    <view\n" +
                "            width=\"100\" height=\"55\" x=\"30\" y=\"30\">\n" +
                "        <label id=\"strength\"\n" +
                "                width=\"100\" height=\"11\" y=\"0\"\n" +
                "                color=\"0\" />\n" +
                "        <label id=\"stamina\"\n" +
                "                width=\"100\" height=\"11\" y=\"11\"\n" +
                "                color=\"black\" />\n" +
                "        <label id=\"wisdom\"\n" +
                "                width=\"100\" height=\"11\" y=\"22\"\n" +
                "                color=\"#ff0000\" />\n" +
                "        <label id=\"intelligence\"\n" +
                "                width=\"100\" height=\"11\" y=\"33\"\n" +
                "                color=\"rgb(255,0,0)\" />\n" +
                "        <label id=\"charisma\"\n" +
                "                width=\"100\" height=\"11\" y=\"44\"\n" +
                "                color=\"rgba(0,255,0,0.5)\" />\n" +
                "    </view>\n" +
                "    <button id=\"inventory\"\n" +
                "            width=\"116\" height=\"20\" x=\"0\" y=\"13\"\n" +
                "            align=\"BottomMiddle\"\n" +
                "            label=\"$(container.inventory)\" />\n" +
                "</window>\n";

        Loader.createFromXML(xml, this);

        ((Label)findPaneByID("strength")).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
        ((Label)findPaneByID("stamina")).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
        ((Label)findPaneByID("wisdom")).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
        ((Label)findPaneByID("intelligence")).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
        ((Label)findPaneByID("charisma")).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));


//        Label title = new Label();
//        title.setSize(-1, 11);
//        title.setPosition(0, 9);
//        title.setTextAlignment(Alignment.TopMiddle);
//        title.setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills"));
//        title.setColor(0x0);
//        title.putInside(this);
//
//        View attributePane = new View();
//        attributePane.setSize(getWidth() - 60, 60);
//        attributePane.setPosition(0, 30);
//        attributePane.setAlignment(Alignment.TopMiddle);
//        attributePane.putInside(this);
//
//        String [] attributes = new String[5];
//        attributes[0] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength);
//        attributes[1] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina);
//        attributes[2] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom);
//        attributes[3] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence);
//        attributes[4] = LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma);
//
//        int y = 0;
//        for (String attr : attributes)
//        {
//            Label label = new Label();
//
//            label.setSize(100, 11);
//            label.setPosition(0, y);
//            label.setColor(0x0);
//            label.setLabel(attr);
//            label.putInside(attributePane);
//
//            y += label.getHeight();
//        }
//
//        y += attributePane.getX();
//
//        for (int i = 0; i < 2; ++i)
//        {
//            TextField text = new TextFieldVanilla();
//            text.setSize(-1, 20);
//            text.setPosition(0, y);
//            text.setText("This is just a test");
//            text.putInside(this);
//
//            y += text.getHeight();
//        }
//
//        ButtonVanilla inventory = new ButtonVanilla();
//        inventory.setID(INVENTORY_BUTTON_ID);
//        inventory.setSize(116, 20);
//        inventory.setPosition(0, 13);
//        inventory.setAlignment(Alignment.BottomMiddle);
//        inventory.setLabel(LanguageHandler.format("container.inventory"));
//        inventory.setHandler(this);
//        inventory.putInside(this);
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
