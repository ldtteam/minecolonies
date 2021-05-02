package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperScepterMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperSetHarvestHoneycombsMessage;

import static com.minecolonies.api.research.util.ResearchConstants.BEEKEEP_2;

/**
 * Window for the beekeeper hut.
 */
public class WindowHutBeekeeperModule extends AbstractWindowWorkerModuleBuilding<BuildingBeekeeper.View>
{
    private static final String BUTTON_HARVEST_HONEYCOMB = "harvestHoneycomb";

    /**
     * Id of the button to give tool
     */
    private static final String BUTTON_GIVE_TOOL = "giveTool";

    /**
     * Button for toggling honeycomb harvesting.
     */
    private final Button buttonHarvestHoneycombs;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building {@link BuildingBeekeeper.View}.
     */
    public WindowHutBeekeeperModule(final BuildingBeekeeper.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowhutbeekeeper.xml");
        registerButton(BUTTON_HARVEST_HONEYCOMB, this::harvestHoneycombClicked);

        buttonHarvestHoneycombs = findPaneOfTypeByID(BUTTON_HARVEST_HONEYCOMB, Button.class);

        switch (building.isHarvestHoneycombs())
        {
            case HONEYCOMBS:
                buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTCOMB));
                break;
            case HONEY_BOTTLES:
                buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTBOTTLE));
                break;
            case BOTH:
                buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTBOTH));
                break;
        }

        registerButton(BUTTON_GIVE_TOOL, this::givePlayerScepter);
    }

    /**
     * Send message to player to add scepter to his inventory.
     */
    private void givePlayerScepter()
    {
        Network.getNetwork().sendToServer(new BeekeeperScepterMessage(building));
    }

    private void harvestHoneycombClicked()
    {
        // can't use switch/case, due to LanguageHandlers.
        if (buttonHarvestHoneycombs.getTextAsString().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTCOMB)))
        {
            buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTBOTTLE));
            building.setHarvestHoneycombs(BeekeeperSetHarvestHoneycombsMessage.HarvestType.HONEY_BOTTLES);
        }
        else if(buttonHarvestHoneycombs.getTextAsString().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTBOTTLE)) &&
                 building.getColony().getResearchManager().getResearchEffects().getEffectStrength(BEEKEEP_2) > 0)
        {
            buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTBOTH));
            building.setHarvestHoneycombs(BeekeeperSetHarvestHoneycombsMessage.HarvestType.BOTH);
        }
        else
        {
            buttonHarvestHoneycombs.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BEEKEEPER_COLLECTCOMB));
            building.setHarvestHoneycombs(BeekeeperSetHarvestHoneycombsMessage.HarvestType.HONEYCOMBS);
        }
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.beekeeperhut";
    }
}
