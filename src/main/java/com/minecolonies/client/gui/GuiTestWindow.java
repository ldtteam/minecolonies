package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;

public class GuiTestWindow extends Window implements Button.Handler
{
    private static String INVENTORY_BUTTON_ID = "inventory";

    private CitizenData.View citizen;

    public GuiTestWindow(CitizenData.View citizen)
    {
        super(Constants.MODID + ":" + "gui/windowTestGUI.xml");
        this.citizen = citizen;
    }

    public void onOpened()
    {
        try
        {
            findPaneOfTypeByID("strength", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
            findPaneOfTypeByID("stamina", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
            findPaneOfTypeByID("wisdom", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.wisdom", citizen.wisdom));
            //findPaneOfTypeByID("intelligence", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
            //findPaneOfTypeByID("charisma", Label.class).setLabel(LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));

            findPaneOfTypeByID("scrollgroup", ScrollingList.class).setDataProvider(
                new ScrollingList.DataProvider() {
                    @Override
                    public int getElementCount() { return 20; }

                    @Override
                    public void updateElement(int index, Pane pane)
                    {
                        pane.findPaneOfTypeByID("listlabel", Label.class).setLabel(String.format("#%d", index));
                        ScrollingList scrollList = pane.findPaneOfTypeByID("scrollgroup2", ScrollingList.class);
                        if (scrollList != null)
                        {
                            scrollList.setDataProvider(
                                new ScrollingList.DataProvider() {
                                    @Override
                                    public int getElementCount() { return 5; }

                                    @Override
                                    public void updateElement(int index2, Pane pane)
                                    {
                                        pane.findPaneOfTypeByID("listlabel2", Label.class).setLabel(String.format("%c", 'A' + index2));
                                    }
                                });
                        }
                    }
                });
        }
        catch (NullPointerException exc) {}
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.network.sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
