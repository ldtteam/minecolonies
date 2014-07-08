package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiTownHallInfo extends GuiTownHall
{
    private final int idInformation = 0, idActions = 1, idSettings = 2;
    protected int labelSpan = 9;

    public GuiTownHallInfo(TileEntityTownHall tileEntityTownHall, EntityPlayer player, World world, int x, int y, int z)
    {
        super(tileEntityTownHall, player, world, x, y, z);
    }

    @Override
    protected void addTownHallElements()
    {
        String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens") + " " + tileEntityTownHall.getCitizens().size() + "/" + tileEntityTownHall.getMaxCitizens();
        String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed") + " " + (tileEntityTownHall.getCitizens().size() - tileEntityTownHall.getWorkers().size());
        String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders") + " " + tileEntityTownHall.getBuilders().size();

        int y = middleY + 13;
        int x = middleX - fontRendererObj.getStringWidth(numberOfCitizens) / 2 + 3;

        addLabel(numberOfCitizens, x, y);
        y += labelSpan * 2;
        addLabel(numberOfUnemployed, x, y);
        y += labelSpan;
        addLabel(numberOfBuilders, x, y);

        GuiButton infoButton = addButton(idInformation, LanguageHandler.format("com.minecolonies.gui.workerHuts.information"), middleX - 76, middleY + ySize - 34, 64, buttonHeight);
        infoButton.enabled = false;
        addButton(idActions, LanguageHandler.format("com.minecolonies.gui.townhall.actions"), middleX - 10, middleY + ySize - 34, 44, buttonHeight);
        addButton(idSettings, LanguageHandler.format("com.minecolonies.gui.workerHuts.settings"), middleX + xSize / 2 - 50, middleY + ySize - 34, 46, buttonHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case idInformation:
                break;
            case idActions:
                player.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
                break;
            case idSettings:
                break;
        }
    }
}
