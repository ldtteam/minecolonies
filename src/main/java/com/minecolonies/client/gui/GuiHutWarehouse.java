package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityHutWarehouse;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHutWarehouse extends GuiBase
{
    private final int BUTTON_INFORMATION = 0, BUTTON_SETTINGS = 1, BUTTON_BLACKSMITH_GOLD = 2, BUTTON_BLACKSMITH_DIAMOND = 3, BUTTON_STONEMASON_STONE = 4, BUTTON_STONEMASON_SAND = 5, BUTTON_STONEMASON_NETHERRACK = 6, BUTTON_STONEMASON_QUARTZ = 7, BUTTON_GUARD_ARMOR = 8, BUTTON_GUARD_WEAPON = 9, BUTTON_CITIZEN = 10;
    private final TileEntityHutWarehouse warehouse;

    private final int span = 4;

    private final int PAGE_INFORMATION = 0, PAGE_SETTINGS = 1;
    private int page = PAGE_INFORMATION;

    public GuiHutWarehouse(TileEntityHutWarehouse tileEntityHutWarehouse, EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z);
        warehouse = tileEntityHutWarehouse;
    }

    private void addDeliverySettingElements()
    {
        int smallButton = 30;
        int xl = (width - xSize) / 2 + xSize / 3 - 5;
        int xr = xl + xSize / 3;
        int y = middleY + span;
        int textPaddTop = 6, textPaddRight = 3;

        String toBlacksmith = LanguageHandler.format("com.minecolonies.gui.warehouse.toBlacksmith");
        String toStonemason = LanguageHandler.format("com.minecolonies.gui.warehouse.toStonemason");
        String toGuards = LanguageHandler.format("com.minecolonies.gui.warehouse.toGuards");
        String visitCitizenChests = LanguageHandler.format("com.minecolonies.gui.warehouse.visitCitizenChests");

        addCenteredLabel(toBlacksmith, y + textPaddTop);

        addIcon(new ItemStack(Items.gold_ingot, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_GOLD, getYesOrNo(warehouse.blacksmithGold), xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.diamond, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_BLACKSMITH_DIAMOND, getYesOrNo(warehouse.blacksmithDiamond), xr, y, smallButton, buttonHeight);

        addCenteredLabel(toStonemason, (y += buttonHeight + buttonSpan + 1) + textPaddTop);

        addIcon(new ItemStack(Blocks.cobblestone, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_STONE, getYesOrNo(warehouse.stonemasonStone), xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Blocks.sand, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_SAND, getYesOrNo(warehouse.stonemasonSand), xr, y, smallButton, buttonHeight);

        addIcon(new ItemStack(Blocks.netherrack, 1), xl - 16 - textPaddRight, (y += buttonHeight + buttonSpan) + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_NETHERRACK, getYesOrNo(warehouse.stonemasonNetherrack), xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.quartz, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_STONEMASON_QUARTZ, getYesOrNo(warehouse.stonemasonQuartz), xr, y, smallButton, buttonHeight);

        addCenteredLabel(toGuards, (y += buttonHeight + buttonSpan + 1) + textPaddTop);

        addIcon(new ItemStack(Items.iron_chestplate, 1), xl - 16 - textPaddRight, (y += buttonHeight) + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_ARMOR, getYesOrNo(warehouse.guardArmor), xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.iron_sword, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(BUTTON_GUARD_WEAPON, getYesOrNo(warehouse.guardWeapon), xr, y, smallButton, buttonHeight);

        addCenteredLabel(visitCitizenChests, (y += buttonHeight + buttonSpan + 1) + textPaddTop);
        addButton(BUTTON_CITIZEN, getYesOrNo(warehouse.citizenVisit), middleX - smallButton / 2, (y += textPaddTop + 10), smallButton, buttonHeight);
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        String information = LanguageHandler.format("com.minecolonies.gui.workerHuts.information");
        String settings = LanguageHandler.format("com.minecolonies.gui.workerHuts.settings");
        GuiButton infoButton = addBottomButton(BUTTON_INFORMATION, information, middleX - ((int) (buttonWidth / 1.5)), (int) (buttonWidth / 1.5), buttonHeight);
        GuiButton settingsButton = addBottomButton(BUTTON_SETTINGS, settings, middleX + 2, (int) (buttonWidth / 1.5), buttonHeight);

        switch(page)
        {
            case PAGE_INFORMATION:
                infoButton.enabled = false;
                addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.warehouse"), "John R. Jones", "xx (yy)", "xxxxxxxx", span, false);
                break;
            case PAGE_SETTINGS:
                settingsButton.enabled = false;
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
        //Actions for Settings Tab
        else if(page == PAGE_SETTINGS)
        {
            switch(guiButton.id)
            {
                case BUTTON_INFORMATION:
                    page = PAGE_INFORMATION;
                    break;
                case BUTTON_BLACKSMITH_GOLD:
                    warehouse.blacksmithGold = !warehouse.blacksmithGold;
                    break;
                case BUTTON_BLACKSMITH_DIAMOND:
                    warehouse.blacksmithDiamond = !warehouse.blacksmithDiamond;
                    break;
                case BUTTON_STONEMASON_STONE:
                    warehouse.stonemasonStone = !warehouse.stonemasonStone;
                    break;
                case BUTTON_STONEMASON_SAND:
                    warehouse.stonemasonSand = !warehouse.stonemasonSand;
                    break;
                case BUTTON_STONEMASON_NETHERRACK:
                    warehouse.stonemasonNetherrack = !warehouse.stonemasonNetherrack;
                    break;
                case BUTTON_STONEMASON_QUARTZ:
                    warehouse.stonemasonQuartz = !warehouse.stonemasonQuartz;
                    break;
                case BUTTON_GUARD_ARMOR:
                    warehouse.guardArmor = !warehouse.guardArmor;
                    break;
                case BUTTON_GUARD_WEAPON:
                    warehouse.guardWeapon = !warehouse.guardWeapon;
                    break;
                case BUTTON_CITIZEN:
                    warehouse.citizenVisit = !warehouse.citizenVisit;
                    break;
            }
            addElements();
        }
    }

    private String getYesOrNo(boolean bool)
    {
        return bool ? LanguageHandler.format("gui.yes") : LanguageHandler.format("gui.no");
    }
}
