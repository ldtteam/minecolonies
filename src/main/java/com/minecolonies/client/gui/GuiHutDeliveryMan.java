package com.minecolonies.client.gui;

import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHutDeliveryMan extends GuiBase
{
    //IDs for Information
    private final int BUTTON_INFO_INFORMATION = 4, BUTTON_INFO_SETTINGS = 5;

    //IDs for Settings
    private final int BUTTON_BLACKSMITH_GOLD = 0, BUTTON_BLACKSMITH_DIAMOND = 1, BUTTON_STONEMASON_STONE = 2, BUTTON_STONEMASON_SAND = 3, BUTTON_STONEMASON_NETHERRACK = 4, BUTTON_STONEMASON_QUARTZ = 5, BUTTON_GUARD_ARMOR = 6, BUTTON_GUARD_WEAPON = 7, BUTTON_CITIZEN = 8, BUTTON_SET_INFORMATION = 9, BUTTON_SET_SETTINGS = 10;

    private final int span = 4;

    private final int PAGE_INFORMATION = 0, PAGE_SETTINGS = 1;
    private int page = PAGE_INFORMATION;

    public GuiHutDeliveryMan(EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z);
    }

    private void addDeliverySettingElements()
    {
        int smallButton = 30;
        int xl = (width - xSize) / 2 + xSize / 3 - 5;
        int xr = xl + xSize / 3;
        int y = middleY + span;
        int textPaddTop = 6, textPaddRight = 3;

        String yes = LanguageHandler.format("gui.yes");
        String no = LanguageHandler.format("gui.no");
        String toBlacksmith = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toBlacksmith");
        String toStonemason = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toStonemason");
        String toGuards = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toGuards");
        String visitCitizenChests = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.visitCitizenChests");

        addCenteredLabel(toBlacksmith, y + textPaddTop);

        addIcon(new ItemStack(Items.gold_ingot, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_GOLD, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.diamond, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_DIAMOND, no, xr, y, smallButton, buttonHeight);

        addCenteredLabel(toStonemason, (y += buttonHeight + buttonSpan + 1) + textPaddTop);

        addIcon(new ItemStack(Blocks.cobblestone, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_STONE, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Blocks.sand, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_SAND, no, xr, y, smallButton, buttonHeight);

        addIcon(new ItemStack(Blocks.netherrack, 1), xl - 16 - textPaddRight, (y += buttonHeight + buttonSpan) + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_NETHERRACK, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.quartz, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_QUARTZ, no, xr, y, smallButton, buttonHeight);

        addCenteredLabel(toGuards, (y += buttonHeight + buttonSpan + 1) + textPaddTop);

        addIcon(new ItemStack(Items.iron_chestplate, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_ARMOR, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.iron_sword, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_WEAPON, no, xr, y, smallButton, buttonHeight);

        addCenteredLabel(visitCitizenChests, (y += buttonHeight + buttonSpan + 1) + textPaddTop);
        addButton(BUTTON_CITIZEN, no, middleX - smallButton / 2, (y += textPaddTop + 10), smallButton, buttonHeight);
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        boolean infoButtonEnabled = true, settingsButtonEnabled = true;
        int infoButtonId = BUTTON_INFO_INFORMATION, settingsButtonId = BUTTON_INFO_SETTINGS;

        switch(page)
        {
            case PAGE_INFORMATION:
                infoButtonEnabled = false;
                addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);
                break;
            case PAGE_SETTINGS:
                infoButtonId = BUTTON_SET_INFORMATION;
                settingsButtonId = BUTTON_SET_SETTINGS;
                settingsButtonEnabled = false;
                addDeliverySettingElements();
                break;
        }

        String information = LanguageHandler.format("com.minecolonies.gui.workerHuts.information");
        String settings = LanguageHandler.format("com.minecolonies.gui.workerHuts.settings");
        GuiButton infoButton = addButton(infoButtonId, information, middleX - ((int) (buttonWidth / 1.5)), middleY + ySize - 34, (int) (buttonWidth / 1.5), buttonHeight);
        GuiButton settingsButton = addButton(settingsButtonId, settings, middleX + 2, middleY + ySize - 34, (int) (buttonWidth / 1.5), buttonHeight);
        infoButton.enabled = infoButtonEnabled;
        settingsButton.enabled = settingsButtonEnabled;
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        //Actions for Information Tab
        if(page == PAGE_INFORMATION)
        {
            super.actionPerformed(guiButton);

            if (guiButton.id == BUTTON_INFO_SETTINGS)
            {
                page = PAGE_SETTINGS;
                addElements();
            }
        }
        //Actions for Settings Tab
        else if(page == PAGE_SETTINGS)
        {
            switch(guiButton.id)
            {
                case BUTTON_BLACKSMITH_GOLD:
                    break;
            }

            if(guiButton.id == BUTTON_SET_INFORMATION)
            {
                page = PAGE_INFORMATION;
                addElements();
            }
        }
    }
}
