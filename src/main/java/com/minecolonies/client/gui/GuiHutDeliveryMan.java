package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutDeliveryman extends GuiBase
{
    //private TileEntityHutDeliveryMan tileEntity; //There is no TileEntity for now - Nico

    //IDs for Information
    protected final int idSettings = 5;

    //IDs for Settings
    protected final int idGoldToBlacksmith = 0,
                        idDiamondToBlacksmith = 1,
                        idStoneToStonemason = 2,
                        idSandToStonemason = 3,
                        idNetherrackToStonemason = 4,
                        idQuartzToStonemason = 5,
                        idArmorToGuards = 6,
                        idWeaponToGuards = 7,
                        idInformation = 8;
    String ntos = I18n.format("com.minecolonies.gui.deliverymanHut.netherrackToStonemason");
    String qtos = I18n.format("com.minecolonies.gui.deliverymanHut.quartzToStonemason");
    String atog = I18n.format("com.minecolonies.gui.deliverymanHut.armorToStonemason");
    String wtog = I18n.format("com.minecolonies.gui.deliverymanHut.weaponsToGuards");

    protected EntityPlayer player;
    protected World        world;
    protected int          x, y, z;
    private int span = 10, page = 0;

    public GuiHutDeliveryman(/*TileEntityHutBuilder tileEntity, */int page, EntityPlayer player, World world, int x, int y, int z)
    {
        super();
        //this.tileEntity = tileEntity;
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
        int x = width - (width - xSize) / 2 - smallButton - 10;
        int y = middleY + span;
        int textPaddTop = 6, textPaddRight = 3;

        String yes = I18n.format("com.minecolonies.gui.yes");
        String no = I18n.format("com.minecolonies.gui.no");
        String information = I18n.format("com.minecolonies.gui.workerHuts.information");
        String gtob = I18n.format("com.minecolonies.gui.deliverymanHut.goldToBlacksmith");
        String dtob = I18n.format("com.minecolonies.gui.deliverymanHut.diamondToBlacksmith");
        String stos = I18n.format("com.minecolonies.gui.deliverymanHut.stoneToStonemason");
        String satos = I18n.format("com.minecolonies.gui.deliverymanHut.sandToStonemason");
        String ntos = I18n.format("com.minecolonies.gui.deliverymanHut.netherrackToStonemason");
        String qtos = I18n.format("com.minecolonies.gui.deliverymanHut.quartzToStonemason");
        String atog = I18n.format("com.minecolonies.gui.deliverymanHut.armorToStonemason");
        String wtog = I18n.format("com.minecolonies.gui.deliverymanHut.weaponsToGuards");

        addLabel(gtob, x - fontRendererObj.getStringWidth(gtob) - textPaddRight, y+textPaddTop);
        addButton(idGoldToBlacksmith, no, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(dtob, x - fontRendererObj.getStringWidth(dtob) - textPaddRight, y+textPaddTop);
        addButton(idDiamondToBlacksmith, no, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(stos, x - fontRendererObj.getStringWidth(stos) - textPaddRight, y+textPaddTop);
        addButton(idStoneToStonemason, yes, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(satos, x - fontRendererObj.getStringWidth(satos) - textPaddRight, y+textPaddTop);
        addButton(idSandToStonemason, no, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(ntos, x - fontRendererObj.getStringWidth(ntos) - textPaddRight, y+textPaddTop);
        addButton(idNetherrackToStonemason, yes, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(qtos, x - fontRendererObj.getStringWidth(qtos) - textPaddRight, y+textPaddTop);
        addButton(idQuartzToStonemason, no, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(atog, x - fontRendererObj.getStringWidth(atog) - textPaddRight, y+textPaddTop);
        addButton(idArmorToGuards, no, x, y, smallButton, buttonHeight);
        y += buttonHeight + buttonSpan;

        addLabel(wtog, x - fontRendererObj.getStringWidth(wtog) - textPaddRight, y+textPaddTop);
        addButton(idWeaponToGuards, no, x, y, smallButton, buttonHeight);

        addButton(idInformation, information, middleX - buttonWidth / 2, middleY + ySize - 34, buttonWidth, buttonHeight);
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        switch(page)
        {
            case 0:
                addDefaultWorkerLayout(I18n.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);

                String settings = I18n.format("com.minecolonies.gui.workerHuts.settings");
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
