package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.network.messages.LumberjackReplantSaplingToggleMessage;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
          LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.saplingList"),
          PAGE_ITEMS_VIEW,
          true);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;

        setupReplantButton(findPaneOfTypeByID(BUTTON_TOGGLE_REPLANT, Button.class));
        registerButton(BUTTON_TOGGLE_REPLANT, this::switchReplant);
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return ColonyManager.getCompatibilityManager().getCopyOfSaplings().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
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
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void switchReplant(final Button replant)
    {
        ownBuilding.shouldReplant = !ownBuilding.shouldReplant;
        setupReplantButton(replant);
        MineColonies.getNetwork().sendToServer(new LumberjackReplantSaplingToggleMessage(ownBuilding, ownBuilding.shouldReplant));
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

