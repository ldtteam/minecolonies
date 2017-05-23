package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Alignment;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.views.View;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Image;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the citizen.
 */
public class WindowCitizen extends Window implements ButtonHandler
{
    /**
     * The label to find the inventory button.
     */
    private static final String INVENTORY_BUTTON_ID     = "inventory";
    /**
     * The label to find the gui of the citizen.
     */
    private static final String CITIZEN_RESOURCE_SUFFIX = ":gui/windowCitizen.xml";
    /**
     * The label to find strength in the gui.
     */
    private static final String STRENGTH                = "strength";
    /**
     * The label to find endurance in the gui.
     */
    private static final String ENDURANCE               = "endurance";
    /**
     * The label to find charisma in the gui.
     */
    private static final String CHARISMA                = "charisma";
    /**
     * The label to find intelligence in the gui.
     */
    private static final String INTELLIGENCE            = "intelligence";
    /**
     * The label to find dexterity in the gui.
     */
    private static final String DEXTERITY               = "dexterity";

    /**
     * Xp-bar height.
     */
    private static final int XP_HEIGHT                    = 5;
    /**
     * The x-distance to the left border of the gui of the xpBar.
     */
    private static final int LEFT_BORDER_X                = 10;
    /**
     * The y-distance to the top-left border of the gui of the xpBar.
     */
    private static final int LEFT_BORDER_Y                = 10;
    /**
     * The column in which the icon starts.
     */
    private static final int XP_BAR_ICON_COLUMN           = 0;
    /**
     * The column where the icon ends.
     */
    private static final int XP_BAR_ICON_COLUMN_END       = 172;
    /**
     * The width of the end piece of the xpBar.
     */
    private static final int XP_BAR_ICON_COLUMN_END_WIDTH = 10;
    /**
     * The offset where the end should be placed in the GUI.
     */
    private static final int XP_BAR_ICON_END_OFFSET       = 90;
    /**
     * The width of the xpBar (Original width is halved to fit in the gui).
     */
    private static final int XP_BAR_WIDTH                 = 182 / 2;
    /**
     * The row where the empty xpBar starts.
     */
    private static final int XP_BAR_EMPTY_ROW             = 64;
    /**
     * The row where the full xpBar starts.
     */
    private static final int XP_BAR_FULL_ROW              = 69;
    /**
     * X position of the xpLabel.
     */
    private static final int XP_LABEL_X                   = -20;
    /**
     * Y position of the xpLabel.
     */
    private static final int XP_LABEL_Y                   = 38;

    /**
     * Row position of the empty heart icon.
     */
    private static final int EMPTY_HEART_ICON_ROW_POS = 16;
    /**
     * Row position of the full heart icon.
     */
    private static final int FULL_HEART_ICON_ROW_POS  = 53;
    /**
     * Row position of the half/full heart icon.
     */
    private static final int HALF_HEART_ICON_ROW_POS  = 62;
    /**
     * Column position of the heart icons.
     */
    private static final int HEART_ICON_COLUMN        = 0;
    /**
     * Dimension of the hearts.
     */
    private static final int HEART_ICON_HEIGHT_WIDTH  = 9;
    /**
     * The position x where the heart is placed.
     */
    private static final int HEART_ICON_POS_X         = 10;
    /**
     * The offset x where the next heart should be placed.
     */
    private static final int HEART_ICON_OFFSET_X      = 10;
    /**
     * The position y where the heart is placed.
     */
    private static final int HEART_ICON_POS_Y         = 10;

    /**
     * The position y where the saturation is placed.
     */
    private static final int SATURATION_ICON_POS_Y  = 10;

    /**
     * Column of the saturation icon.
     */
    private static final int SATURATION_ICON_COLUMN = 27;

    /**
     * Dimension of the hearts.
     */
    private static final int SATURATION_ICON_HEIGHT_WIDTH  = 9;

    /**
     * Saturation icon x position.
     */
    private static final int SATURATION_ICON_POS_X = 10;

    /**
     * Saturation item x offset.
     */
    private static final int SATURATION_ICON_OFFSET_X = 10;

    /**
     * The label to find name in the gui.
     */
    private static final String WINDOW_ID_NAME        = "name";
    /**
     * The label to find xpLabel in the gui.
     */
    private static final String WINDOW_ID_XP          = "xpLabel";
    /**
     * The label to find xpBar in the gui.
     */
    private static final String WINDOW_ID_XPBAR       = "xpBar";
    /**
     * The label to find healthBar in the gui.
     */
    private static final String WINDOW_ID_HEALTHBAR   = "healthBar";

    /**
     * The position of the empty saturation icon.
     */
    private static final int EMPTY_SATURATION_ITEM_ROW_POS = 16;

    /**
     * The position of the full saturation icon.
     */
    private static final int FULL_SATURATION_ITEM_ROW_POS = 16 + 36;

    /**
     * The position of the half saturation icon.
     */
    private static final int HALF_SATURATION_ITEM_ROW_POS = 16 + 45;

    /**
     * The saturation bar of the citizen.
     */
    private static final String WINDOW_ID_SATURATION_BAR = "saturationBar";

    /**
     * The citizenData.View object.
     */
    private final CitizenDataView citizen;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowCitizen(final CitizenDataView citizen)
    {
        super(Constants.MOD_ID + CITIZEN_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(WINDOW_ID_NAME, Label.class).setLabelText(citizen.getName());

        createHealthBar();
        createSaturationBar();
        createXpBar();
        createSkillContent();
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     */
    private void createHealthBar()
    {
        findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        //MaxHealth (Black hearts).
        for (int i = 0; i < citizen.getMaxHealth() / 2; i++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, EMPTY_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(i * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Current health (Red hearts).
        int heartPos;
        for (heartPos = 0; heartPos < ((int) citizen.getHealth() / 2); heartPos++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, FULL_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Half hearts.
        if (citizen.getHealth() / 2 % 1 > 0)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, HALF_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     */
    private void createSaturationBar()
    {
        findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        //Max saturation (Black food items).
        for (int i = 0; i < CitizenData.MAX_SATURATION; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, EMPTY_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);

            saturation.setPosition(i * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Current saturation (Full food hearts).
        int saturationPos;
        for (saturationPos = 0; saturationPos < ((int) citizen.getSaturation()); saturationPos++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, FULL_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(saturationPos * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Half food items.
        if (citizen.getSaturation() / 2 % 1 > 0)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, HALF_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(saturationPos * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }
    }

    /**
     * Creates the xp bar for each citizen.
     * Calculates an xpBarCap which is the maximum of xp to fit into the bar.
     * Then creates an xp bar and fills it up with the available xp.
     */
    private void createXpBar()
    {
        //Calculates how much percent of the next level has been completed.
        final double experienceRatio = ExperienceUtils.getPercentOfLevelCompleted(citizen.getExperience(), citizen.getLevel());

        findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setLabelText(Integer.toString(citizen.getLevel()));
        findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setPosition(XP_LABEL_X, XP_LABEL_Y);

        @NotNull final Image xpBar = new Image();
        xpBar.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT);
        xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);

        @NotNull final Image xpBar2 = new Image();
        xpBar2.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT);
        xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y);

        findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar);
        findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar2);

        if (experienceRatio > 0)
        {
            @NotNull final Image xpBarFull = new Image();
            xpBarFull.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT);
            xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);
            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBarFull);
        }
    }

    /**
     * Fills the citizen gui with it's skill values.
     */
    private void createSkillContent()
    {
        findPaneOfTypeByID(STRENGTH, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.strength", citizen.getStrength()));
        findPaneOfTypeByID(ENDURANCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.endurance", citizen.getEndurance()));
        findPaneOfTypeByID(CHARISMA, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.charisma", citizen.getCharisma()));
        findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.intelligence", citizen.getIntelligence()));
        findPaneOfTypeByID(DEXTERITY, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.dexterity", citizen.getDexterity()));
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(INVENTORY_BUTTON_ID))
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(citizen));
        }
    }
}
