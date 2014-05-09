package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiTownHall extends GuiBase {
    private TileEntityTownHall tileEntityTownHall;
    //private final int numberOfButtons = 8; //This variable is unused - Nico
    private final int idBuildTownhall = 0,
            idRepairTownhall = 1,
            idRecallCitizens = 2,
            idToggleSpecialization = 3,
            idRenameColony = 4,
            idInformation = 5,
            idActions = 6,
            idSettings = 7;
    private int buttonSpan = 4,
            span = 30;

    private EntityPlayer player;
    private World world;
    private int x, y, z;

    public GuiTownHall(TileEntityTownHall tileEntityTownHall, EntityPlayer player, World world, int x, int y, int z) {
        super();
        this.tileEntityTownHall = tileEntityTownHall;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void addElements() {
        super.addElements();

        String currentSpec = I18n.format("com.minecolonies.gui.townhall.currentSpecialization");
        String spec = "<Industrial>"; //TODO replace with actual specialisation
        String currentTownhallName = I18n.format("com.minecolonies.gui.townhall.currTownhallName");
        String townhallName = tileEntityTownHall.getCityName();

        int y = span;

        addLabel(currentTownhallName, middleX - fontRendererObj.getStringWidth(currentTownhallName) / 2 + 3, middleY + 4);
        addLabel(townhallName, middleX - fontRendererObj.getStringWidth(townhallName) / 2 + 3, middleY + 13);
        addButton(idBuildTownhall, I18n.format("com.minecolonies.gui.townhall.build"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        y += buttonHeight + buttonSpan;
        addButton(idRepairTownhall, I18n.format("com.minecolonies.gui.townhall.repair"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        y += buttonHeight + buttonSpan;
        addButton(idRecallCitizens, I18n.format("com.minecolonies.gui.townhall.recall"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        y += buttonHeight + buttonSpan;
        addButton(idToggleSpecialization, I18n.format("com.minecolonies.gui.townhall.togglespec"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);
        y += buttonHeight + buttonSpan;
        addLabel(currentSpec, middleX - fontRendererObj.getStringWidth(currentSpec) / 2 + 3, middleY + y);
        addLabel(spec, middleX - fontRendererObj.getStringWidth(spec) / 2 + 3, middleY + y + 11);
        y += buttonHeight + buttonSpan;
        addButton(idRenameColony, I18n.format("com.minecolonies.gui.townhall.rename"), middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight);

        //Bottom navigation
        addButton(idInformation, I18n.format("com.minecolonies.gui.townhall.information"), middleX - 76, middleY + ySize - 34, 64, buttonHeight);
        addButton(idActions, I18n.format("com.minecolonies.gui.townhall.actions"), middleX - 10, middleY + ySize - 34, 44, buttonHeight);
        addButton(idSettings, I18n.format("com.minecolonies.gui.townhall.settings"), middleX + xSize / 2 - 50, middleY + ySize - 34, 46, buttonHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        switch (guiButton.id) {
            case idBuildTownhall:
                break;
            case idRepairTownhall:
                break;
            case idRecallCitizens:
                break;
            case idToggleSpecialization:
                break;
            case idRenameColony:
                player.openGui(MineColonies.instance, 1, world, x, y, z);
                break;
            case idInformation:
                break;
            case idActions:
                break;
            case idSettings:
                break;
        }
    }
}
