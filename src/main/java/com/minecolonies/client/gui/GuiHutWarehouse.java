package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingWarehouse;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHutWarehouse extends GuiBase
{
    private final int BUTTON_INFORMATION = 0, BUTTON_SETTINGS = 1, BUTTON_INVENTORY = 2;
    private final int BUTTON_BLACKSMITH_GOLD = 0, BUTTON_BLACKSMITH_DIAMOND = 1, BUTTON_STONEMASON_STONE = 2, BUTTON_STONEMASON_SAND = 3, BUTTON_STONEMASON_NETHERRACK = 4, BUTTON_STONEMASON_QUARTZ = 5, BUTTON_GUARD_ARMOR = 6, BUTTON_GUARD_WEAPON = 7, BUTTON_CITIZEN = 8, BUTTON_BACK = 9;
    private final BuildingWarehouse.View warehouse;

    private final int span = 4;

    private final int PAGE_MENU = 0, PAGE_INFORMATION = 1, PAGE_SETTINGS = 2;
    private int page = PAGE_MENU;

    public GuiHutWarehouse(BuildingWarehouse.View building, EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z, building);
        warehouse = building;
    }

    private void addDeliverySettingElements()
    {
        int smallButton = 30;
        int xl = (width - xSize) / 2 + xSize / 3 - 5;
        int xr = xl + xSize / 3;
        int y = topY + span;
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


        switch(page)
        {
            case PAGE_MENU:
                int span = buttonHeight * 3;
                String information = LanguageHandler.format("com.minecolonies.gui.workerHuts.information");
                String settings = LanguageHandler.format("com.minecolonies.gui.workerHuts.settings");
                String inventory = LanguageHandler.format("container.inventory");
                addButton(BUTTON_INFORMATION, information, buttonMiddleX, buttonMiddleY - span, buttonWidth, buttonHeight);
                addButton(BUTTON_SETTINGS, settings, buttonMiddleX, buttonMiddleY, buttonWidth, buttonHeight);
                addButton(BUTTON_INVENTORY, inventory, buttonMiddleX, buttonMiddleY + span, buttonWidth, buttonHeight);
                break;
            case PAGE_INFORMATION:
                addBottomButton(BUTTON_BACK, LanguageHandler.format("gui.back"), buttonMiddleX, buttonWidth, buttonHeight);
                addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.warehouse"), "John R. Jones", "xx (yy)", "xxxxxxxx", this.span);
                break;
            case PAGE_SETTINGS:
                addBottomButton(BUTTON_BACK, LanguageHandler.format("gui.back"), buttonMiddleX, buttonWidth, buttonHeight);
                addDeliverySettingElements();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        //Actions for Menu Tab
        if(page == PAGE_MENU)
        {
            switch(guiButton.id)
            {
                case BUTTON_INFORMATION:
                    page = PAGE_INFORMATION;
                    addElements();
                    break;
                case BUTTON_SETTINGS:
                    page = PAGE_SETTINGS;
                    addElements();
                    break;
                case BUTTON_INVENTORY:
                    MineColonies.network.sendToServer(new OpenInventoryMessage(warehouse));
                    break;
            }
        }
        //Actions for Information Tab
        else if(page == PAGE_INFORMATION)
        {
            super.actionPerformed(guiButton);

            switch(guiButton.id)
            {
                case BUTTON_BACK:
                    page = PAGE_MENU;
                    addElements();
                    break;
            }
        }
        //Actions for Settings Tab
        else if(page == PAGE_SETTINGS)
        {
            switch(guiButton.id)
            {
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
                case BUTTON_BACK:
                    page = PAGE_MENU;
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
