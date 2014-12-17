package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;

public class GuiTownHall extends GuiBuilding
{
    private final int BUTTON_BUILD = 0, BUTTON_REPAIR = 1, BUTTON_RECALL = 2, BUTTON_SPECIALIZATION_TOGGLE = 3, BUTTON_RENAME = 4, BUTTON_INFORMATION = 5, BUTTON_ACTIONS = 6, BUTTON_SETTINGS = 7;
    private final BuildingTownHall.View townhall;
    private final ColonyView colony;

    private final int PAGE_ACTIONS = 0, PAGE_INFORMATION = 1, PAGE_SETTINGS = 2;
    private int page = PAGE_ACTIONS;

    public GuiTownHall(BuildingTownHall.View building)
    {
        super(building);
        this.townhall = building;
        this.colony = building.getColony();
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        //Bottom navigation
        GuiButton infoButton = addBottomButton(BUTTON_INFORMATION, LanguageHandler.format("com.minecolonies.gui.workerHuts.information"), middleX - 76, 64, buttonHeight);
        GuiButton actionsButton = addBottomButton(BUTTON_ACTIONS, LanguageHandler.format("com.minecolonies.gui.townhall.actions"), middleX - 10, 44, buttonHeight);
        GuiButton settingsButton = addBottomButton(BUTTON_SETTINGS, LanguageHandler.format("com.minecolonies.gui.workerHuts.settings"), middleX + xSize / 2 - 50, 46, buttonHeight);


        if(page == PAGE_ACTIONS)
        {
            actionsButton.enabled = false;

            String currentSpec = LanguageHandler.format("com.minecolonies.gui.townhall.currentSpecialization");
            String spec = "<Industrial>"; //TODO replace with actual specialisation
            String currentTownhallName = LanguageHandler.format("com.minecolonies.gui.townhall.currTownhallName");
            String townhallName = colony.getName();

            int y = labelSpan * 3;

            addCenteredLabel(currentTownhallName, topY + labelSpan / 2);
            addCenteredLabel(townhallName, topY + labelSpan + labelSpan / 2);
            addButton(BUTTON_BUILD, LanguageHandler.format("com.minecolonies.gui.townhall.build"), buttonMiddleX, topY + y, buttonWidth, buttonHeight);
            addButton(BUTTON_REPAIR, LanguageHandler.format("com.minecolonies.gui.townhall.repair"), buttonMiddleX, topY + (y += buttonHeight + buttonSpan), buttonWidth, buttonHeight);
            addButton(BUTTON_RECALL, LanguageHandler.format("com.minecolonies.gui.townhall.recall"), buttonMiddleX, topY + (y += buttonHeight + buttonSpan), buttonWidth, buttonHeight);
            addButton(BUTTON_SPECIALIZATION_TOGGLE, LanguageHandler.format("com.minecolonies.gui.townhall.togglespec"), buttonMiddleX, topY + (y += buttonHeight + buttonSpan), buttonWidth, buttonHeight);

            addCenteredLabel(currentSpec, topY + (y += buttonHeight + buttonSpan));
            addCenteredLabel(spec, topY + y + labelSpan);

            addButton(BUTTON_RENAME, LanguageHandler.format("com.minecolonies.gui.townhall.rename"), buttonMiddleX, topY + (y += buttonHeight + buttonSpan), buttonWidth, buttonHeight);
        }
        else if(page == PAGE_INFORMATION)
        {
            infoButton.enabled = false;

            int citizensSize = colony.getCitizens().size();
            int workers = 0;
            int builders = 0, deliverymen = 0;
            //  TODO - Rewrite this based on the CitizenData
//            List<Entity> citizens = Utils.getEntitiesFromID(world, colony.getCitizens());
//            if(citizens != null)
//            {
//                for(Entity citizen : citizens)
//                {
//                    if(citizen instanceof EntityBuilder)
//                    {
//                        builders++;
//                    }
//                    else if(citizen instanceof EntityDeliveryman)
//                    {
//                        deliverymen++;
//                    }
//                }
//                workers = builders + deliverymen;
//            }

            String numberOfCitizens = LanguageHandler.format("com.minecolonies.gui.townhall.population.totalCitizens", citizensSize, colony.getMaxCitizens());
            String numberOfUnemployed = LanguageHandler.format("com.minecolonies.gui.townhall.population.unemployed", (citizensSize - workers));
            String numberOfBuilders = LanguageHandler.format("com.minecolonies.gui.townhall.population.builders", builders);
            String numberOfDeliverymen = LanguageHandler.format("com.minecolonies.gui.townhall.population.deliverymen", deliverymen);

            int y = topY + labelSpan;
            int x = getSameCenterX(numberOfCitizens, numberOfUnemployed, numberOfBuilders, numberOfDeliverymen);

            addLabel(numberOfCitizens, x, y);
            addLabel(numberOfUnemployed, x, y += labelSpan * 2);
            addLabel(numberOfBuilders, x, y += labelSpan);
            addLabel(numberOfDeliverymen, x, y += labelSpan);
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
            case BUTTON_BUILD:
                MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.BUILD));
                break;
            case BUTTON_REPAIR:
                MineColonies.network.sendToServer(new BuildRequestMessage(townhall, BuildRequestMessage.REPAIR));
                break;
            case BUTTON_RECALL:
                break;
            case BUTTON_SPECIALIZATION_TOGGLE:
                break;
            case BUTTON_RENAME:
                building.openGui(EnumGUI.TOWNHALL_RENAME);
                break;
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
        }
    }
}
