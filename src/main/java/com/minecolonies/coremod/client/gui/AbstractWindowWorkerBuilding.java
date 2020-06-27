package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ForcePickupMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.OpenCraftingGUIMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for window for worker building.
 *
 * @param <B> Class extending {@link AbstractBuildingWorker.View}
 */
public abstract class AbstractWindowWorkerBuilding<B extends AbstractBuildingWorker.View> extends AbstractWindowBuilding<B>

{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE = "hire";

    /**
     * Id of the scroll view
     */
    private static final String LIST_WORKERS = "workers";

    /**
     * Id of the recall button in the GUI.
     */
    private static final String BUTTON_RECALL = "recall";

    /**
     * Id of the priority value label in the GUI.
     */
    private static final String LABEL_PRIO_VALUE = "prioValue";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Id of the level label in the GUI.
     */
    private static final String LABEL_WORKERLEVEL = "workerLevel";

    /**
     * Name string of the builder hut.
     */
    private static final String BUILDER_HUT_NAME = "com.minecolonies.coremod.gui.workerhuts.buildersHut";

    /**
     * Button to access the crafting grid.
     */
    private static final String BUTTON_CRAFTING = "crafting";

    /**
     * Button to access the recipe list.
     */
    private static final String BUTTON_RECIPES_LIST = "recipelist";

    /**
     * Button to increase delivery prio.
     */
    private static final String BUTTON_DP_UP = "deliveryPrioUp";

    /**
     * Button to decrease delivery prio.
     */
    private static final String BUTTON_DP_DOWN = "deliveryPrioDown";

    /**
     * Button to force a pickup
     */
    private static final String BUTTON_FORCE_PICKUP = "forcePickup";

    /**
     * Current pickup priority of the building.
     */
    private int prio = building.getBuildingDmPrio();

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View}.
     * @param resource Resource of the window.
     */
    AbstractWindowWorkerBuilding(final B building, final String resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_CRAFTING, this::craftingClicked);
        super.registerButton(BUTTON_RECIPES_LIST, this::recipeListClicked);
        super.registerButton(BUTTON_DP_UP, this::deliveryPrioUp);
        super.registerButton(BUTTON_DP_DOWN, this::deliveryPrioDown);
        super.registerButton(BUTTON_FORCE_PICKUP, this::forcePickup);

        // The recipe list is visible when the user can alter recipes, or when the building has at least one recipe (regardless of allowRecipeAlterations())
        // The thought behind this is to show users player-thaught recipes and also built-in recipes.
        // But if it's a building that simply does not use recipes, we hide this button to make it less confusing for newer players.
        findPaneOfTypeByID(BUTTON_RECIPES_LIST, ButtonImage.class).setVisible(building.isRecipeAlterationAllowed() || !building.getRecipes().isEmpty());

        findPaneOfTypeByID(BUTTON_CRAFTING, ButtonImage.class).setVisible(building.isRecipeAlterationAllowed());
    }

    private void updatePriorityLabel()
    {
        if (prio == 0)
        {
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setLabelText(
              LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.buildPrio") + LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.deliveryprio.never"));
        }
        else
        {
            findPaneOfTypeByID(LABEL_PRIO_VALUE, Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.buildPrio") + prio + "/10");
        }
    }

    private void deliveryPrioUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, true));
        updatePriorityLabel();
    }

    private void deliveryPrioDown()
    {
        if (prio != 0)
        {
            prio--;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, false));
        updatePriorityLabel();
    }

    private void forcePickup()
    {
        Network.getNetwork().sendToServer(new ForcePickupMessage(building));
    }

    private void recipeListClicked()
    {
        if (!building.isRecipeAlterationAllowed() && building.getRecipes().isEmpty())
        {
            /**
             * @see #onOpened() for the reasoning behind this.
             */
            // This should never happen, because the button is hidden. But if someone glitches into the interface, stop him here.
            return;
        }
        @NotNull final WindowListRecipes window = new WindowListRecipes(building.getColony(), building.getPosition());
        window.open();
    }

    /**
     * If crafting is clicked this happens. Override if needed.
     */
    public void craftingClicked()
    {
        if (!building.isRecipeAlterationAllowed())
        {
            // This should never happen, because the button is hidden. But if someone glitches into the interface, stop him here.
            return;
        }
        final BlockPos pos = building.getPosition();
        Minecraft.getInstance().player.openContainer((INamedContainerProvider) Minecraft.getInstance().world.getTileEntity(pos));
        Network.getNetwork().sendToServer(new OpenCraftingGUIMessage(building));
    }

    /**
     * Action when a hire button is clicked.
     * If there is no worker (worker.Id == 0) then Contract someone.
     * Else then Fire the current worker.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        if (building.getBuildingLevel() == 0 && !BUILDER_HUT_NAME.equals(getBuildingName()))
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.minecolonies.coremod.gui.workerhuts.level0");
            return;
        }

        @NotNull final WindowHireWorker window = new WindowHireWorker(building.getColony(), building.getPosition());
        window.open();
    }

    /**
     * Action when a recall button is clicked.
     */
    private void recallClicked()
    {
        Network.getNetwork().sendToServer(new RecallCitizenMessage(building));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return building.getWorkerId().size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    if (!building.getWorkerId().isEmpty())
                    {
                        final ICitizenDataView worker = building.getColony().getCitizen(building.getWorkerId().get(index));
                        if (worker != null)
                        {
                            rowPane.findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabelText(worker.getName());
                            rowPane.findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class)
                              .setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.workerLevel",
                                worker.getCitizenSkillHandler().getJobModifier(building)));
                        }
                    }
                }
            });
        }

        updatePriorityLabel();
    }
}
