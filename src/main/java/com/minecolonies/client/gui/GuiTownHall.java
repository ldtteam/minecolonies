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
    //private final int numberOfButtons = 8; //This variable is unused - Nico
    public static final int idInformation = 0, idActions = 1, idSettings = 2;
    private final int idBuildTownhall = 3, idRepairTownhall = 4, idRecallCitizens = 5, idToggleSpecialization = 6, idRenameColony = 7;
    private final int page;
    protected TileEntityTownHall tileEntityTownHall;
    protected int labelSpan = 9, span = 30;

    protected EntityPlayer player;
    protected World        world;
    protected int          x, y, z;

    public GuiTownHall(TileEntityTownHall tileEntityTownHall, EntityPlayer player, World world, int x, int y, int z)
    {
        this(tileEntityTownHall, player, world, x, y, z, idActions);
    }

    public GuiTownHall(TileEntityTownHall tileEntityTownHall, EntityPlayer player, World world, int x, int y, int z, int page)
    {
        super();
        this.tileEntityTownHall = tileEntityTownHall;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.page = page;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        //Bottom navigation
        GuiButton infoButton = addButton(idInformation, LanguageHandler.format("com.minecolonies.gui.workerHuts.information"), middleX - 76, middleY + ySize - 34, 64, buttonHeight);
        GuiButton actionsButton = addButton(idActions, LanguageHandler.format("com.minecolonies.gui.townhall.actions"), middleX - 10, middleY + ySize - 34, 44, buttonHeight);
        GuiButton settingsButton = addButton(idSettings, LanguageHandler.format("com.minecolonies.gui.workerHuts.settings"), middleX + xSize / 2 - 50, middleY + ySize - 34, 46, buttonHeight);

        if (page == idInformation)
        {
            infoButton.enabled = false;

            List<Entity> citizens = Utils.getEntitiesFromUUID(world, tileEntityTownHall.getCitizens());
            List<EntityBuilder> builders = new ArrayList<EntityBuilder>();
            if (citizens != null)
            {
                for (Entity citizen : citizens)
                {
                    if (citizen instanceof EntityBuilder)
                    {
                        builders.add((EntityBuilder) citizen);
                    }
                }
            }
            List<EntityCitizen> workers = new ArrayList<EntityCitizen>(builders);

            String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens") + " " + tileEntityTownHall.getCitizens().size() + "/" + tileEntityTownHall.getMaxCitizens();
            String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed") + " " + (tileEntityTownHall.getCitizens().size() - workers.size());
            String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders") + " " + builders.size();

            int y = middleY + 13;
            int x = middleX - fontRendererObj.getStringWidth(numberOfCitizens) / 2 + 3;

            addLabel(numberOfCitizens, x, y);
            y += labelSpan * 2;
            addLabel(numberOfUnemployed, x, y);
            y += labelSpan;
            addLabel(numberOfBuilders, x, y);
        }
        else if (page == idSettings)
        {
        }
        else
        {
            actionsButton.enabled = false;

            String currentSpec = LanguageHandler.format("com.minecolonies.gui.townhall.currentSpecialization");
            String spec = "<Industrial>"; //TODO replace with actual specialisation
            String currentTownhallName = LanguageHandler.format("com.minecolonies.gui.townhall.currTownhallName");
            String townhallName = tileEntityTownHall.getCityName();

            int y = span;

            addLabel(currentTownhallName, middleX - fontRendererObj.getStringWidth(currentTownhallName) / 2 + 3, middleY + 4);
            addLabel(townhallName, middleX - fontRendererObj.getStringWidth(townhallName) / 2 + 3, middleY + 13);
            addButton(idBuildTownhall, LanguageHandler.format("com.minecolonies.gui.townhall.build"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(idRepairTownhall, LanguageHandler.format("com.minecolonies.gui.townhall.repair"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(idRecallCitizens, LanguageHandler.format("com.minecolonies.gui.townhall.recall"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
            y += buttonHeight + buttonSpan;
            addButton(idToggleSpecialization, LanguageHandler.format("com.minecolonies.gui.townhall.togglespec"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);

            y += buttonHeight + buttonSpan;
            addLabel(currentSpec, middleX - fontRendererObj.getStringWidth(currentSpec) / 2 + 3, middleY + y);
            addLabel(spec, middleX - fontRendererObj.getStringWidth(spec) / 2 + 3, middleY + y + 11);
            y += buttonHeight + buttonSpan;

            addButton(idRenameColony, LanguageHandler.format("com.minecolonies.gui.townhall.rename"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case idInformation:
                player.openGui(MineColonies.instance, EnumGUI.TOWNHALL_INFORMATION.getID(), world, x, y, z);
                break;
            case idActions:
                player.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
                break;
            case idSettings:
                break;
            case idBuildTownhall:
                MineColonies.packetPipeline.sendToServer(new BuildRequestPacket(x, y, z, BuildRequestPacket.BUILD));
                break;
            case idRepairTownhall:
                MineColonies.packetPipeline.sendToServer(new BuildRequestPacket(x, y, z, BuildRequestPacket.REPAIR));
                break;
            case idRecallCitizens:
                break;
            case idToggleSpecialization:
                break;
            case idRenameColony:
                player.openGui(MineColonies.instance, EnumGUI.TOWNHALL_RENAME.getID(), world, x, y, z);
                break;
        }
    }
}
