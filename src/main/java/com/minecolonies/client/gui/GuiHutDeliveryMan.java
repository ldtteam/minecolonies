package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHutDeliveryMan extends GuiBase
{
    //IDs for Information
    protected final int idSettings = 5;

    //IDs for Settings
    protected final int idGoldToBlacksmith = 0, idDiamondToBlacksmith = 1, idStoneToStonemason = 2, idSandToStonemason = 3, idNetherrackToStonemason = 4, idQuartzToStonemason = 5, idArmorToGuards = 6, idWeaponToGuards = 7, idVisitCitizenChests = 8, idInformation = 9;

    protected EntityPlayer player;
    protected World        world;
    protected int          x, y, z;
    private int span = 4, page = 0;

    public GuiHutDeliveryMan(int page, EntityPlayer player, World world, int x, int y, int z)
    {
        super();
        this.page = page;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private void addDeliverySettingElements()
    {
        int smallButton = 30;
        int xl = (width - xSize) / 2 + xSize / 3 - 5;
        int xr = xl + xSize / 3;
        int y = middleY + span;
        int textPaddTop = 6, textPaddRight = 3;

        String yes = LanguageHandler.format("com.minecolonies.gui.yes");
        String no = LanguageHandler.format("com.minecolonies.gui.no");
        String information = LanguageHandler.format("com.minecolonies.gui.workerHuts.information");
        String toBlacksmith = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toBlacksmith");
        String toStonemason = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toStonemason");
        String toGuards = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.toGuards");
        String visitCitizenChests = LanguageHandler.format("com.minecolonies.gui.deliverymanHut.visitCitizenChests");

        addLabel(toBlacksmith, middleX - fontRendererObj.getStringWidth(toBlacksmith) / 2, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Items.gold_ingot, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idGoldToBlacksmith, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.diamond, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idDiamondToBlacksmith, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addLabel(toStonemason, middleX - fontRendererObj.getStringWidth(toStonemason) / 2, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Blocks.stone, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idStoneToStonemason, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Blocks.sand, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idSandToStonemason, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addIcon(new ItemStack(Blocks.netherrack, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idNetherrackToStonemason, yes, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.quartz, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idQuartzToStonemason, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addLabel(toGuards, middleX - fontRendererObj.getStringWidth(toGuards) / 2, y + textPaddTop);
        y += buttonHeight;

        addIcon(new ItemStack(Items.iron_chestplate, 1), xl - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idArmorToGuards, no, xl, y, smallButton, buttonHeight);
        addIcon(new ItemStack(Items.iron_sword, 1), xr - 16 - textPaddRight, y + (buttonHeight - 16) / 2);
        addButton(idWeaponToGuards, no, xr, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan + 1;

        addLabel(visitCitizenChests, middleX - fontRendererObj.getStringWidth(visitCitizenChests) / 2, y + textPaddTop);
        y += textPaddTop + 10;
        addButton(idVisitCitizenChests, no, middleX - smallButton / 2, y, smallButton, buttonHeight);

        addButton(idInformation, information, middleX - buttonWidth / 2, middleY + ySize - 34, buttonWidth, buttonHeight);

    }

    @Override
    protected void addElements()
    {
        super.addElements();

        switch(page)
        {
            case 0:
                addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);

                String settings = LanguageHandler.format("com.minecolonies.gui.workerHuts.settings");
                addButton(idSettings, settings, middleX - buttonWidth / 2, middleY + ySize - 34, buttonWidth, buttonHeight);
                break;
            case 1:
                addDeliverySettingElements();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        //Actions for Information Tab
        if(page == 0)
        {
            switch(guiButton.id)
            {
                case idHireWorker:
                    ((GuiButton) buttonList.get(idFireWorker)).visible = true;
                    break;
                case idFireWorker:
                    GuiButton hireButton = (GuiButton) buttonList.get(idHireWorker);
                    if(hireButton.visible)
                    {
                        hireButton.visible = false;
                    }
                    else
                    {
                        guiButton.visible = false;
                        hireButton.visible = true;
                    }
                    break;
                case idRecallWorker:
                    break;
                case idBuildBuilding:
                    break;
                case idRepairBuilding:
                    break;
                case idSettings:
                    player.openGui(MineColonies.instance, Constants.Gui.HutDeliverymanSettings.ordinal(), world, x, y, z);
                    break;
            }
        }
        else if(page == 1)
        {
            switch(guiButton.id)
            {
                case idGoldToBlacksmith:
                    break;
                case idInformation:
                    player.openGui(MineColonies.instance, Constants.Gui.HutDeliveryman.ordinal(), world, x, y, z);
                    break;
            }
        }
    }
}
