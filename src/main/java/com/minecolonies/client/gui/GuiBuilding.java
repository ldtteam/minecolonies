package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public abstract class GuiBuilding extends GuiBase
{
    //IDs for default layout
    protected final int BUTTON_HIRE_FIRE = 0, BUTTON_RECALL = 1, BUTTON_BUILD = 2, BUTTON_REPAIR = 3;

    protected final Building.View building;

    public GuiBuilding(Building.View building)
    {
        super();
        this.building = building;
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type)
    {
        addDefaultWorkerLayout(hutName, workerName, level, type, 0);
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type, int yPadding)
    {
        String workerAssigned = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerAssigned");
        String workerLevel = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel", level);
        String buildType = LanguageHandler.format("com.minecolonies.gui.workerHuts.buildType");

        addCenteredLabel(hutName, topY + yPadding, 0xff0000);
        addCenteredLabel(workerAssigned, topY + yPadding + 18);
        addCenteredLabel(workerName, topY + yPadding + 28);
        addCenteredLabel(workerLevel, topY + yPadding + 44);
        addButton(BUTTON_HIRE_FIRE, LanguageHandler.format("com.minecolonies.gui.workerHuts.hire"), buttonMiddleX, topY + yPadding + 64, buttonWidth, buttonHeight);
        addButton(BUTTON_RECALL, LanguageHandler.format("com.minecolonies.gui.workerHuts.recall"), buttonMiddleX, topY + yPadding + 88, buttonWidth, buttonHeight);
        addButton(BUTTON_BUILD, LanguageHandler.format("com.minecolonies.gui.workerHuts.build"), buttonMiddleX, topY + yPadding + 120, buttonWidth, buttonHeight);
        addButton(BUTTON_REPAIR, LanguageHandler.format("com.minecolonies.gui.workerHuts.repair"), buttonMiddleX, topY + yPadding + 144, buttonWidth, buttonHeight);
        addCenteredLabel(buildType, topY + yPadding + 172);
        addCenteredLabel(type, topY + yPadding + 182);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case BUTTON_HIRE_FIRE:
                if(guiButton.displayString.equals(LanguageHandler.format("com.minecolonies.gui.workerHuts.hire")))
                {
                    //TODO: hire worker
                    guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.fire");
                }
                else
                {
                    //TODO: fire worker
                    guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.hire");
                }
                break;
            case BUTTON_RECALL:
                //TODO recall
                break;
            case BUTTON_BUILD:
                MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
                break;
            case BUTTON_REPAIR:
                MineColonies.network.sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
                break;
            default:
                super.actionPerformed(guiButton);
        }
    }
}
