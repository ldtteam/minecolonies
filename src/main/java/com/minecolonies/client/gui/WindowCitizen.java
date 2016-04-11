package com.minecolonies.client.gui;

import com.blockout.Alignment;
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
    /**
     * The static labels to find text in the language files in order to load it in the gui.
     */
    private final static String                 INVENTORY_BUTTON_ID     = "inventory";
    private final static String                 CITIZEN_RESOURCE_SUFFIX = ":gui/windowCitizen.xml";
    private final static String                 STRENGTH                = "strength";
    private final static String                 STAMINA                 = "stamina";
    private final static String                 SPEED                   = "speed";
    private final static String                 INTELLIGENCE            = "intelligence";
    private final static String                 DILIGENCE               = "diligence";

    /**
     * Contains the id's of the elements in the windowCitizen.xml
     */
    private final static String                 WINDOW_ID_NAME = "name";
    private final static String                 WINDOW_ID_XP = "xpLabel";
    private final static String                 WINDOW_ID_XPBAR = "xpBar";
    private final static String                 WINDOW_ID_HEALTHBAR = "healthBar";

    private              CitizenData.View       citizen;

    /**
     * Constructor to initiate the citizen windows
     * @param citizen citizen to bind the window to
     */
    public WindowCitizen(CitizenData.View citizen)
    {
        super(Constants.MOD_ID + CITIZEN_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    /**
     * Creates the xp bar for each citizen.
     * Calculates an xpBarCap which is the maximum of xp to fit into the bar.
     * Then creates an xp bar and fills it up with the available xp
     */
    private void createXpBar()
    {
        int xpBarCap = ((citizen.getLevel() >= 30 ? 62 + (citizen.getLevel() - 30) * 7 : (citizen.getLevel() >= 15 ? 17 + (citizen.getLevel() - 15) * 3 : 17)));

        if (xpBarCap > 0)
        {
            int experienceRatio = citizen.getLevel()!=0 ? (int)((double)(citizen.getExperience()+1)/((citizen.getLevel()*citizen.getLevel())*100)*100) : (citizen.getExperience()/100)*100;

            findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setLabel(""+citizen.getLevel());
            findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setPosition(60,30);

            Image xpBar = new Image();
            xpBar.setImage(Gui.icons,0,64,182/2,5);
            xpBar.setPosition(10,10);

            Image xpBar2 = new Image();
            xpBar2.setImage(Gui.icons,172,64,10,5);
            xpBar2.setPosition(100,10);

            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar);
            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar2);

            if (experienceRatio > 0)
            {
                Image xpBarFull = new Image();
                xpBarFull.setImage(Gui.icons,0,69,experienceRatio,5);
                xpBarFull.setPosition(10,10);
                findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBarFull);
            }
        }
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth
     */
    private void createHealthBar()
    {
        findPaneOfTypeByID("healthBar", View.class).setAlignment(Alignment.MiddleRight);

        //MaxHealth (Black hearts)
        for(int i=0;i<citizen.maxHealth/2;i++)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 16, 0, 9, 9);
            heart.setPosition(i*10+10,10);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Current health (Red hearts)
        int heartPos;
        for(heartPos=0;heartPos<((int)citizen.health/2);heartPos++)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 53, 0, 9, 9);
            heart.setPosition(heartPos*10+11,10);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Half hearts
        if(citizen.health/2%1!=0)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, 53+9, 0, 9, 9);
            heart.setPosition(heartPos*10+11,10);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(WINDOW_ID_NAME, Label.class).setLabel(citizen.getName());

        createHealthBar();
        createXpBar();
        createSkillContent();
    }

    /**
     * Fills the citizen gui with it's skill values.
     */
    private void createSkillContent()
    {
        findPaneOfTypeByID(STRENGTH, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.strength", citizen.strength));
        findPaneOfTypeByID(STAMINA, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.stamina", citizen.stamina));
        findPaneOfTypeByID(SPEED, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.speed", citizen.speed));
        findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.intelligence", citizen.intelligence));
        findPaneOfTypeByID(DILIGENCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.diligence", citizen.diligence));
    }

    /**
     * Called when a button in the citizen has been clicked
     * @param button the clicked button
     */
    @Override
    public void onButtonClicked(Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
