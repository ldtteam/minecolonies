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
    private static final  String                INVENTORY_BUTTON_ID             = "inventory";
    private static final  String                CITIZEN_RESOURCE_SUFFIX         = ":gui/windowCitizen.xml";
    private static final  String                STRENGTH                        = "strength";
    private static final  String                ENDURANCE                       = "endurance";
    private static final  String                CHARISMA                        = "charisma";
    private static final  String                INTELLIGENCE                    = "intelligence";
    private static final  String                DILIGENCE                       = "diligence";

    /**
     * Xp-bar constants
     */
    private static final int                    XP_HEIGHT                       = 5;
    private static final int                    LEFT_BORDER_X                   = 10;
    private static final int                    LEFT_BORDER_Y                   = 10;

    private static final int                    XP_BAR_ICON_COLUMN              = 0;
    private static final int                    XP_BAR_ICON_COLUMN_END          = 172;
    private static final int                    XP_BAR_ICON_COLUMN_END_WIDTH    = 10;
    private static final int                    XP_BAR_ICON_END_OFFSET          = 90;


    //Width is halved to fit into the gui.
    private static final int                    XP_BAR_WIDTH                    = 182/2;
    private static final int                    XP_BAR_EMPTY_ROW                = 64;
    private static final int                    XP_BAR_FULL_ROW                 = 64;
    private static final int                    XP_LABEL_X                      = 60;
    private static final int                    XP_LABEL_Y                      = 30;

    /**
     * Health bar constants
     */
    private static final int                    EMPTY_HEART_ICON_ROW_POS        = 16;
    private static final int                    FULL_HEART_ICON_ROW_POS         = 53;
    private static final int                    HALF_HEART_ICON_ROW_POS         = 62;
    private static final int                    HEART_ICON_COLUMN               = 0;
    private static final int                    HEART_ICON_HEIGHT_WIDTH         = 9;
    private static final int                    HEART_ICON_POS_X                = 10;
    private static final int                    HEART_ICON_OFFSET_X             = 10;
    private static final int                    HEART_ICON_POS_Y                = 10;


    /**
     * Contains the id's of the elements in the windowCitizen.xml
     */
    private static final  String                 WINDOW_ID_NAME                 = "name";
    private static final  String                 WINDOW_ID_XP                   = "xpLabel";
    private static final  String                 WINDOW_ID_XPBAR                = "xpBar";
    private static final  String                 WINDOW_ID_HEALTHBAR            = "healthBar";

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
            //Calculates how much percent of the next level has been completed
            int experienceRatio = citizen.getLevel()!=0 ? (int)((double)(citizen.getExperience()+1)/((citizen.getLevel()*citizen.getLevel())*100)*100) : (citizen.getExperience()/100)*100;

            findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setLabel(""+citizen.getLevel());
            findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setPosition(XP_LABEL_X,XP_LABEL_Y);

            Image xpBar = new Image();
            xpBar.setImage(Gui.icons,XP_BAR_ICON_COLUMN,XP_BAR_EMPTY_ROW,XP_BAR_WIDTH,XP_HEIGHT);
            xpBar.setPosition(LEFT_BORDER_X,LEFT_BORDER_Y);

            Image xpBar2 = new Image();
            xpBar2.setImage(Gui.icons,XP_BAR_ICON_COLUMN_END,XP_BAR_EMPTY_ROW,XP_BAR_ICON_COLUMN_END_WIDTH,XP_HEIGHT);
            xpBar2.setPosition(XP_BAR_ICON_END_OFFSET+LEFT_BORDER_X,LEFT_BORDER_Y);

            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar);
            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar2);

            if (experienceRatio > 0)
            {
                Image xpBarFull = new Image();
                xpBarFull.setImage(Gui.icons,XP_BAR_ICON_COLUMN,XP_BAR_FULL_ROW,experienceRatio,XP_HEIGHT);
                xpBarFull.setPosition(LEFT_BORDER_X,LEFT_BORDER_Y);
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
            heart.setImage(Gui.icons, EMPTY_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(i*HEART_ICON_POS_X+HEART_ICON_OFFSET_X,HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Current health (Red hearts)
        int heartPos;
        for(heartPos=0;heartPos<((int)citizen.health/2);heartPos++)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, FULL_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(heartPos*HEART_ICON_POS_X+HEART_ICON_OFFSET_X,HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Half hearts
        if(citizen.health/2%1!=0)
        {
            Image heart = new Image();
            heart.setImage(Gui.icons, HALF_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(heartPos*HEART_ICON_POS_X+HEART_ICON_OFFSET_X,HEART_ICON_POS_Y);
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
        findPaneOfTypeByID(ENDURANCE, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.endurance", citizen.endurance));
        findPaneOfTypeByID(CHARISMA, Label.class).setLabel(
                LanguageHandler.format("com.minecolonies.gui.citizen.skills.charisma", citizen.charisma));
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
