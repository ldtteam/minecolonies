package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutDeliveryman extends GuiBase {
    //private TileEntityHutDeliveryMan tileEntity; //There is no TileEntity for now - Nico
    protected final int idSettings = 5;
    protected EntityPlayer player;
    protected World world;
    protected int x, y, z;
    private int span = 10, page = 0;

    public GuiHutDeliveryman(/*TileEntityHutBuilder tileEntity, */int page, EntityPlayer player, World world, int x, int y, int z) {
        super();
        //this.tileEntity = tileEntity;
        this.page = page;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private void addDeliverySettingElements() {

    }

    @Override
    protected void addElements() {
        super.addElements();

        switch(page) {
            case 0:
                addDefaultWorkerLayout(I18n.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);

                String settings = I18n.format("com.minecolonies.gui.workerHuts.settings");
                addButton(idSettings, settings, middleX - buttonWidth/2, middleY + ySize - 34, buttonWidth, buttonHeight);
                break;
            case 1:
                addDeliverySettingElements();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        switch (guiButton.id) {
            case idHireWorker:
                ((GuiButton) buttonList.get(idFireWorker)).visible = true;
                break;
            case idFireWorker:
                GuiButton hireButton = (GuiButton) buttonList.get(idHireWorker);
                if (hireButton.visible) {
                    hireButton.visible = false;
                } else {
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
}
