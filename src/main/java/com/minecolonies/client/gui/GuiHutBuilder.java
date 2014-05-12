package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityHutBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiHutBuilder extends GuiBase
{
    private TileEntityHutBuilder tileEntity;
    private int numberOfButtons = 4;
    private int span            = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder)
    {
        super();
        this.tileEntity = tileEntityHutBuilder;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        addDefaultWorkerLayout(I18n.format("com.minecolonies.gui.workerHuts.buildersHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
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
        }
    }
}
