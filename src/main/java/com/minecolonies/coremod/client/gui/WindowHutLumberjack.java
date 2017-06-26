package com.minecolonies.coremod.client.gui;

import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.BuildingLumberjack;
import com.minecolonies.coremod.network.messages.LumberjackSaplingSelectorMessage;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the fisherman hut.
 */
public class WindowHutLumberjack extends AbstractWindowWorkerBuilding<BuildingLumberjack.View>
{
    /**
     * Id of the list in the pane.
     */
    private static final String LIST_SAPLINGS               = "trees";

    /**
     * Page the sapling selector is at.
     */
    private static final String PAGE_SAPLINGS               = "saplingActions";

    /**
     * Id of the button to change between true and false of the sapling.
     */
    private static final String BUTTON_CURRENT_SAPLING = "switch";

    /**
     * String describing on for the gui.
     */
    private static final String ON  = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON);

    /**
     * String describing off for the gui.
     */
    private static final String OFF = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF);

    /**
     * Id of the pages view.
     */
    private static final String VIEW_PAGES                           = "pages";

    /**
     * Scrolling list containing the saplings.
     */
    private       ScrollingList saplingsList;

    /**
     * List of saplings the lumberjack should, or should not fell (true if should, false if should not).
     */
    private final Map<ItemStorage, Boolean> treesToFell = new LinkedHashMap<>();

    /**
     * The building of the lumberjack (Client side representation).
     */
    private final BuildingLumberjack.View ownBuilding;

    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingLumberjack.View}.
     */
    public WindowHutLumberjack(final BuildingLumberjack.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutLumberjack.xml");
        this.ownBuilding = building;
        pullLevelsFromHut();
    }


    /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
    {
        if (building.getColony().getBuilding(building.getID()) != null)
        {
            treesToFell.clear();
            treesToFell.putAll(building.treesToFell);
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        saplingsList = findPaneOfTypeByID(LIST_SAPLINGS, ScrollingList.class);
        saplingsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return treesToFell.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack sapling = treesToFell.keySet().toArray(new ItemStorage[treesToFell.size()])[index].getItemStack();
                rowPane.findPaneOfTypeByID("symbol", ItemIcon.class).setItem(sapling);
                rowPane.findPaneOfTypeByID("name", Label.class).setLabelText(sapling.getDisplayName());

                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_CURRENT_SAPLING, Button.class);

                if(treesToFell.get(new ItemStorage(sapling)))
                {
                    switchButton.setLabel(ON);
                }
                else
                {
                    switchButton.setLabel(OFF);
                }
            }
        });
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if(button.getID().equals(BUTTON_CURRENT_SAPLING))
        {
            final int row = saplingsList.getListElementIndexByPane(button);

            final ItemStorage saplingStack = treesToFell.keySet().toArray(new ItemStorage[treesToFell.size()])[row];

            final boolean shouldCut = !treesToFell.get(saplingStack);
            treesToFell.put(saplingStack, shouldCut);
            MineColonies.getNetwork().sendToServer(new LumberjackSaplingSelectorMessage(building, saplingStack.getItemStack(), shouldCut));

            this.ownBuilding.treesToFell.clear();
            this.ownBuilding.treesToFell.putAll(treesToFell);

        }
        else
        {
            super.onButtonClicked(button);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_SAPLINGS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_SAPLINGS, ScrollingList.class).refreshElementPanes();
        }
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

