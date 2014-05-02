package com.minecolonies.client.gui;

import com.minecolonies.tilentities.TileEntityTownHall;
import net.minecraft.client.gui.GuiScreen;

public class GuiTownHall extends GuiScreen
{
    private TileEntityTownHall tileEntityTownHall;
    private int numberOfButtons = 8;
    private int buildTownhall = 0;
    private int repairTownhall = 1;
    private int recallCitizens = 2;
    private int toggleSpecialization = 3;
    private int renameColony = 4;
    private int information = 5;
    private int actions = 6;
    private int settings = 7;
    private int xSize;
    private int ySize;
    private int middleX = width / 2;
    private int middleY = (height - ySize) / 2;

    public GuiTownHall(TileEntityTownHall tileEntityTownHall)
    {
        xSize = 171;
        ySize = 247;
        this.tileEntityTownHall = tileEntityTownHall;
    }

    @Override
    public void initGui()
    {
        addButtons();
        super.initGui();
    }

    private void addButtons()
    {

    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        //drawGuiBackground();
        //drawGuiForeground();

        //TODO buttons
    }
}
