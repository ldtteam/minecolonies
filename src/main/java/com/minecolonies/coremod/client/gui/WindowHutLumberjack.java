package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackReplantSaplingToggleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackRestrictionToggleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackScepterMessage;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the lumberjack hut.
 */
public class WindowHutLumberjack extends AbstractHutFilterableLists
{
    /**
     * Id of the button to toggle replant of saplings
     */
    private static final String BUTTON_TOGGLE_REPLANT = "saplingReplant";

    /**
     * Id of the button to give tool
     */
    private static final String BUTTON_GIVE_TOOL = "giveTool";

    /**
     * Id of the button to toggle restrict
     */
    private static final String BUTTON_TOGGLE_RESTRICTION = "toggleRestriction";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "saplings";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutlumberjack.xml";

    /**
     * The building of the lumberjack (Client side representation).
     */
    private final BuildingLumberjack.View ownBuilding;

    /**
     * Constructor for the window of the lumberjack.
     *
     * @param building {@link BuildingLumberjack.View}.
     */
    public WindowHutLumberjack(final BuildingLumberjack.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.saplingList"),
          PAGE_ITEMS_VIEW,
          true);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;

        registerButton(BUTTON_TOGGLE_REPLANT, this::switchReplant);
        registerButton(BUTTON_TOGGLE_RESTRICTION, this::toggleRestriction);
        registerButton(BUTTON_GIVE_TOOL, this::giveTool);


        setupReplantButton(findPaneOfTypeByID(BUTTON_TOGGLE_REPLANT, Button.class));
        setupRestrictionButton(findPaneOfTypeByID(BUTTON_TOGGLE_RESTRICTION, Button.class));
        setupGiveToolButton(findPaneOfTypeByID(BUTTON_GIVE_TOOL, Button.class));


    }

    private void giveTool()
    {
        givePlayerScepter();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     */
    private void givePlayerScepter()
    {
        Network.getNetwork().sendToServer(new LumberjackScepterMessage(building));
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfSaplings().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    /**
     * Setup replant button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupReplantButton(final Button button)
    {
        if (ownBuilding.shouldReplant)
        {
            button.setLabel(LanguageHandler.format(TOGGLE_REPLANT_SAPLINGS_ON));
        }
        else
        {
            button.setLabel(LanguageHandler.format(TOGGLE_REPLANT_SAPLINGS_OFF));
        }
    }

    /**
     * Setup giveTool button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupGiveToolButton(final Button button)
    {
        // TODO: Use localisation when this has proper UI
        button.setLabel(LanguageHandler.format("Give tool"));
    }

    /**
     * Setup toggleRestriction button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupRestrictionButton(final Button button)
    {
        button.setLabel(LanguageHandler.format( ownBuilding.shouldRestrict ? "com.minecolonies.coremod.gui.workerHuts.togglerestrictionon" : "com.minecolonies.coremod.gui.workerHuts.togglerestrictionoff" ));
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void switchReplant(final Button replant)
    {
        ownBuilding.shouldReplant = !ownBuilding.shouldReplant;
        setupReplantButton(replant);
        Network.getNetwork().sendToServer(new LumberjackReplantSaplingToggleMessage(ownBuilding, ownBuilding.shouldReplant));
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void toggleRestriction(final Button restriction)
    {
        ownBuilding.shouldRestrict = !ownBuilding.shouldRestrict;
        setupRestrictionButton(restriction);
        Network.getNetwork().sendToServer(new LumberjackRestrictionToggleMessage(ownBuilding, ownBuilding.shouldRestrict));
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return COM_MINECOLONIES_COREMOD_GUI_LUMBERJACK;
    }
}

