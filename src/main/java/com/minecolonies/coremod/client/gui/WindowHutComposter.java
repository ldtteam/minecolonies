package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.network.messages.server.colony.building.composter.ComposterRetrievalMessage;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Composter window class. Specifies the extras the composter has for its list.
 */
public class WindowHutComposter extends AbstractHutFilterableLists
{
    /**
     * Id of the button to toggle replant of saplings
     */
    private static final String BUTTON_TOGGLE_RETRIEVE_DIRT = "retrieveDirt";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "compostables";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutcomposter.xml";

    /**
     * The building of the lumberjack (Client side representation).
     */
    private final BuildingComposter.View ownBuilding;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutComposter(final BuildingComposter.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format("com.minecolonies.gui.workerhuts.composter.compostables"),
          PAGE_ITEMS_VIEW,
          false);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;

        setupRetrieveDirtButton(findPaneOfTypeByID(BUTTON_TOGGLE_RETRIEVE_DIRT, Button.class));
        registerButton(BUTTON_TOGGLE_RETRIEVE_DIRT, this::switchReplant);
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfCompostableItems().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    /**
     * Setup replant button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupRetrieveDirtButton(final Button button)
    {
        if (ownBuilding.retrieveDirtFromCompostBin)
        {
            button.setLabel(LanguageHandler.format(Blocks.DIRT.getTranslationKey()));
        }
        else
        {
            button.setLabel(LanguageHandler.format(ModItems.compost.getTranslationKey(new ItemStack(ModItems.compost))));
        }
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     * 
     * @param retrieve the button to update.
     */
    private void switchReplant(final Button retrieve)
    {
        final BuildingComposter.View composter = ownBuilding;
        composter.retrieveDirtFromCompostBin = !composter.retrieveDirtFromCompostBin;
        setupRetrieveDirtButton(retrieve);
        Network.getNetwork().sendToServer(new ComposterRetrievalMessage(composter, composter.retrieveDirtFromCompostBin));
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.composter";
    }
}
