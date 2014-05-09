package com.minecolonies.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiHutDeliveryMan extends GuiBase {
    //private TileEntityHutDeliveryMan tileEntity; //There is no TileEntity for now - Nico
    protected final int idSettings = 5;
    private int span = 10;

    public GuiHutDeliveryMan(/*TileEntityHutBuilder tileEntity*/) {
        super();
        //this.tileEntity = tileEntity;
    }

    @Override
    protected void addElements() {
        super.addElements();

        addDefaultWorkerLayout(I18n.format("com.minecolonies.gui.workerHuts.deliverymansHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);
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
                break;
        }
    }
}
