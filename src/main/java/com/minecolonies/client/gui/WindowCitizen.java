package com.minecolonies.client.gui;

import com.blockout.Alignment;
import com.blockout.PaneParams;
import com.blockout.View;
import com.blockout.controls.*;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.Gui;

public class WindowCitizen extends Window implements Button.Handler
{
    private static  String              INVENTORY_BUTTON_ID     = "inventory";
    private static  String              CITIZIN_RESOURCE_SUFFIX = ":gui/windowCitizen.xml";
    private static  String              STRENGTH                = "strength";
    private static  String              STAMINA                 = "stamina";
    private static  String              SPEED                  = "speed";
    private static  String              INTELLIGENCE            = "intelligence";
    private static  String              DILIGENCE                = "diligence";

    private         CitizenData.View    citizen;

    public WindowCitizen(CitizenData.View citizen)
    {
        super(Constants.MOD_ID + CITIZIN_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID("name", Label.class).setLabel(citizen.getName());

        findPaneOfTypeByID("healthBar", View.class).setAlignment(Alignment.MiddleRight);

        for(int i=0;i<citizen.maxHealth/2;i++)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 16, 0, 9, 9);
            heart.setPosition(i*10+10,10);
            findPaneOfTypeByID("healthBar", View.class).addChild(heart);
        }

        int heartPos;
        for(heartPos=0;heartPos<((int)citizen.health/2);heartPos++)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 53, 0, 9, 9);
            heart.setPosition(heartPos*10+11,10);
            findPaneOfTypeByID("healthBar", View.class).addChild(heart);
        }

        if(citizen.health/2%1!=0)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 53+9, 0, 9, 9);
            heart.setPosition(heartPos*10+11,10);
            findPaneOfTypeByID("healthBar", View.class).addChild(heart);
        }

        int xpBarCap = ((citizen.getLevel() >= 30 ? 62 + (citizen.getLevel() - 30) * 7 : (citizen.getLevel() >= 15 ? 17 + (citizen.getLevel() - 15) * 3 : 17)));


        if (xpBarCap > 0)
        {
            //todo next line and smaller

            int experienceRatio = citizen.getLevel()!=0 ? (int)((double)(citizen.getExperience()+1)/((citizen.getLevel()*citizen.getLevel())*200)*100) : 100;

            findPaneOfTypeByID("xp", Label.class).setLabel(""+citizen.getLevel());
            findPaneOfTypeByID("xp", Label.class).setPosition(10,40);

            Image xpBar = new Image();
            xpBar.setImage(Gui.icons,0,64,182,5);
            xpBar.setPosition(10,10);

            findPaneOfTypeByID("xpBar", View.class).addChild(xpBar);

            if (experienceRatio > 0)
            {
                Image xpBarFull = new Image();
                xpBarFull.setImage(Gui.icons,0,69,experienceRatio*2,5);
                xpBarFull.setPosition(10,10);
                findPaneOfTypeByID("xpBar", View.class).addChild(xpBarFull);
            }
        }

        
        findPaneOfTypeByID(STRENGTH, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
        findPaneOfTypeByID(STAMINA, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies"
                                       + ".gui.citizen.skills.stamina", citizen.stamina));
        findPaneOfTypeByID(SPEED, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.speed", citizen.speed));
        findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
        findPaneOfTypeByID(DILIGENCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.diligence", citizen.diligence));
    }

    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
