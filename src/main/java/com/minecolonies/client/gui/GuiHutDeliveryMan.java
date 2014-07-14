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
    private final int BUTTON_SETTINGS = 4;

    //IDs for Settings
    private final int BUTTON_BLACKSMITH_GOLD = 0, BUTTON_BLACKSMITH_DIAMOND = 1, BUTTON_STONEMASON_STONE = 2, BUTTON_STONEMASON_SAND = 3, BUTTON_STONEMASON_NETHERRACK = 4, BUTTON_STONEMASON_QUARTZ = 5, BUTTON_GUARD_ARMOR = 6, BUTTON_GUARD_WEAPON = 7, BUTTON_CITIZEN = 8, BUTTON_INFORMATION = 9;

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
        String information = LanguageHandler.format("com.minecolonies.gui.workerHuts.information");
        String toBlacksmith = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toBlacksmith");
        String toStonemason = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toStonemason");
        String toGuards = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toGuards");
        String visitCitizenChests = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.visitCitizenChests");

        addCenteredLabel(toBlacksmith, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Items.gold_ingot, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_GOLD, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.diamond, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_DIAMOND, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addCenteredLabel(toStonemason, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Blocks.cobblestone, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_STONE, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Blocks.sand, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_SAND, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addIcon(new ItemStack(Blocks.netherrack, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_NETHERRACK, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.quartz, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_QUARTZ, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addCenteredLabel(toGuards, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Items.iron_chestplate, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_ARMOR, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.iron_sword, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_WEAPON, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addCenteredLabel(visitCitizenChests, y + textPaddTop);
        y += textPaddTop + 10;
        addButton(BUTTON_CITIZEN, no, middleX - smallButton / 2, y, smallButton, buttonHeight);

        addButton(BUTTON_INFORMATION, information, middleX + 2, middleY + ySize - 34, (int) (buttonWidth / 1.5), buttonHeight);

    }

    @Override
    protected void addElements()
    {
        super.addElements();

        switch(page)
        {
            case PAGE_INFORMATION:
                addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);

                String settings = LanguageHandler.format("com.minecolonies.gui.workerHuts.settings");
                addButton(BUTTON_SETTINGS, settings, middleX - ((int) (buttonWidth / 1.5)), middleY + ySize - 34, (int) (buttonWidth / 1.5), buttonHeight);
                break;
            case PAGE_SETTINGS:
                addDeliverySettingElements();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        //Actions for Information Tab
        if(page == PAGE_INFORMATION)
        {
            super.actionPerformed(guiButton);

            switch(guiButton.id)
            {
                case BUTTON_SETTINGS:
                    page = PAGE_SETTINGS;
                    addElements();
                    break;
            }
        }
        else if(page == PAGE_SETTINGS)
        {
            switch(guiButton.id)
            {
                case BUTTON_BLACKSMITH_GOLD:
                    break;
                case BUTTON_INFORMATION:
                    page = PAGE_INFORMATION;
                    addElements();
                    break;
            }
        }
    }
}
