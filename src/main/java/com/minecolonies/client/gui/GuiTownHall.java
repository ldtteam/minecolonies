package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.network.packets.BuildRequestPacket;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class GuiTownHall extends GuiBase
{
    private final int BUTTON_INFORMATION = 0, BUTTON_ACTIONS = 1, BUTTON_SETTINGS = 2, BUTTON_BUILD = 3, BUTTON_REPAIR = 4, BUTTON_RECALL = 5, BUTTON_SPECIALIZATION_TOGGLE = 6, BUTTON_RENAME = 7;
    protected TileEntityTownHall tileEntityTownHall;
    protected int labelSpan = 9, span = 30;

    protected EntityPlayer player;
    protected World        world;
    protected int          x, y, z;

    private       int page         = 0;
    private final int PAGE_ACTIONS = 0, PAGE_INFORMATION = 1, PAGE_SETTINGS = 2;

    public GuiTownHall(TileEntityTownHall tileEntityTownHall, EntityPlayer player, World world, int x, int y, int z)
    {
        super();
        this.tileEntityTownHall = tileEntityTownHall;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        //Bottom navigation
        GuiButton infoButton = addButton(BUTTON_INFORMATION, LanguageHandler.format("com.minecolonies.gui.workerHuts.information"), middleX - 76, middleY + ySize - 34, 64, buttonHeight);
        GuiButton actionsButton = addButton(BUTTON_ACTIONS, LanguageHandler.format("com.minecolonies.gui.townhall.actions"), middleX - 10, middleY + ySize - 34, 44, buttonHeight);
        GuiButton settingsButton = addButton(BUTTON_SETTINGS, LanguageHandler.format("com.minecolonies.gui.workerHuts.settings"), middleX + xSize / 2 - 50, middleY + ySize - 34, 46, buttonHeight);


        if(page == PAGE_ACTIONS)
        {
            actionsButton.enabled = false;

            String currentSpec = LanguageHandler.format("com.minecolonies.gui.townhall.currentSpecialization");
            String spec = "<Industrial>"; //TODO replace with actual specialisation
            String currentTownhallName = LanguageHandler.format("com.minecolonies.gui.townhall.currTownhallName");
            String townhallName = tileEntityTownHall.getCityName();

            int y = span;

            addLabel(currentTownhallName, middleX - fontRendererObj.getStringWidth(currentTownhallName) / 2 + 3, middleY + 4);
            addLabel(townhallName, middleX - fontRendererObj.getStringWidth(townhallName) / 2 + 3, middleY + 13);
            addButton(BUTTON_BUILD, LanguageHandler.format("com.minecolonies.gui.townhall.build"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(BUTTON_REPAIR, LanguageHandler.format("com.minecolonies.gui.townhall.repair"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(BUTTON_RECALL, LanguageHandler.format("com.minecolonies.gui.townhall.recall"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(BUTTON_SPECIALIZATION_TOGGLE, LanguageHandler.format("com.minecolonies.gui.townhall.togglespec"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);

            y += buttonHeight + buttonSpan;
            addLabel(currentSpec, middleX - fontRendererObj.getStringWidth(currentSpec) / 2 + 3, middleY + y);
            addLabel(spec, middleX - fontRendererObj.getStringWidth(spec) / 2 + 3, middleY + y + 11);
            y += buttonHeight + buttonSpan;

            addButton(BUTTON_RENAME, LanguageHandler.format("com.minecolonies.gui.townhall.rename"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        }
        else if(page == PAGE_INFORMATION)
        {
            infoButton.enabled = false;

            int citizensSize = tileEntityTownHall.getCitizens().size();
            int workers = 0;
            int builders = 0;
            List<Entity> citizens = Utils.getEntitiesFromID(world, tileEntityTownHall.getEntityIDs());
            if(citizens != null)
            {
                for(Entity citizen : citizens)
                {
                    if(citizen instanceof EntityBuilder)
                    {
                        builders++;
                    }
                }
                workers = builders;//+ etc..
            }

            String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens") + " " + citizensSize + "/" + tileEntityTownHall.getMaxCitizens();
            String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed") + " " + (citizensSize - workers);
            String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders") + " " + builders;

            int y = middleY + 13;
            int x = middleX - fontRendererObj.getStringWidth(numberOfCitizens) / 2 + 3;

            addLabel(numberOfCitizens, x, y);
            y += labelSpan * 2;
            addLabel(numberOfUnemployed, x, y);
            y += labelSpan;
            addLabel(numberOfBuilders, x, y);
        }
        else if(page == PAGE_SETTINGS)
        {
            settingsButton.enabled = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case BUTTON_INFORMATION:
                page = PAGE_INFORMATION;
                addElements();
                break;
            case BUTTON_ACTIONS:
                page = PAGE_ACTIONS;
                addElements();
                break;
            case BUTTON_SETTINGS:
                page = PAGE_SETTINGS;
                addElements();
                break;
            case BUTTON_BUILD:
                MineColonies.packetPipeline.sendToServer(new BuildRequestPacket(x, y, z, BuildRequestPacket.BUILD));
                break;
            case BUTTON_REPAIR:
                MineColonies.packetPipeline.sendToServer(new BuildRequestPacket(x, y, z, BuildRequestPacket.REPAIR));
                break;
            case BUTTON_RECALL:
                break;
            case BUTTON_SPECIALIZATION_TOGGLE:
                break;
            case BUTTON_RENAME:
                player.openGui(MineColonies.instance, EnumGUI.TOWNHALL_RENAME.getID(), world, x, y, z);
                break;
        }
    }
}
