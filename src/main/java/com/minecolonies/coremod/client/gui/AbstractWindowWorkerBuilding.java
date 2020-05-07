package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityStateMessage;
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
     * Id of the type label in the GUI.
     */
    private static final String LABEL_BUILDINGTYPE = "type";

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
    private static final String BUTTON_CRAFTING  = "crafting";

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
     * Button to set delivery prio state
     */
    private static final String BUTTON_DP_STATE = "deliveryPrioState";

    private static final String DP_MODE_STATIC = "com.minecolonies.coremod.gui.workerhuts.deliveryPrio.static";

    private static final String DP_MODE_AUTOMATIC = "com.minecolonies.coremod.gui.workerhuts.deliveryPrio.automatic";

    private int prio = building.getBuildingDmPrio();

    private boolean state = building.isBuildingDmPrioState();

    private String stateString = state ? DP_MODE_STATIC : DP_MODE_AUTOMATIC;

    /**
     * Defines whether or not the recipes of this buildings are read-only.
     * Crafters with intrinsic recipes for example would override this to return true.
     * @return Boolean stating if the teach-recipes button should be shown and Remove should be available.
     */
    protected boolean hasReadOnlyRecipes() {
        return false;
    }

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
        super.registerButton(BUTTON_DP_STATE, this::changeDPState);
    }

    private void deliveryPrioUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, true));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(prio + "/10");
    }

    private void deliveryPrioDown()
    {
        if (prio != 1)
        {
            prio--;
        }
        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(building, false));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(prio + "/10");
    }

    private void changeDPState()
    {
        state = !state;
        stateString = state ? DP_MODE_STATIC : DP_MODE_AUTOMATIC;

        Network.getNetwork().sendToServer(new ChangeDeliveryPriorityStateMessage(building));
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(stateString));
        Log.getLogger().info(state);
    }

    private void recipeListClicked()
    {
        @NotNull final WindowListRecipes window = new WindowListRecipes(building.getColony(), building.getPosition(), !hasReadOnlyRecipes());
        window.open();
    }

    /**
     * If crafting is clicked this happens. Override if needed.
     */
    public void craftingClicked()
    {
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

    /**
     * Called when the GUI has been opened.
     */
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
                              .setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.workerLevel", worker.getCitizenSkillHandler().getJobModifier(building)));
                        }
                    }
                }
            });
        }

        findPaneOfTypeByID(BUTTON_CRAFTING, ButtonImage.class).setVisible(!hasReadOnlyRecipes());

        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(building.getBuildingDmPrio() + "/10");
        findPaneOfTypeByID(BUTTON_DP_STATE, Button.class).setLabel(LanguageHandler.format(stateString));
    }
}
