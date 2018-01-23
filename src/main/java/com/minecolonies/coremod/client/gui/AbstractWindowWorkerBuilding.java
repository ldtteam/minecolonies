package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.OpenCraftingGUIMessage;
import com.minecolonies.coremod.network.messages.RecallCitizenMessage;
import net.minecraft.client.Minecraft;
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
    private static final String BUILDER_HUT_NAME = "com.minecolonies.coremod.gui.workerHuts.buildersHut";

    /**
     * Button to access the crafting grid.
     */
    private static final String BUTTON_CRAFTING  = "crafting";

    /**
     * Button to access the recipe list.
     */
    private static final String BUTTON_RECIPES_LIST = "recipelist";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingWorker.View}.
     * @param resource Resource of the window.
     */
    AbstractWindowWorkerBuilding(final B building, final String resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_CRAFTING, this::craftingClicked);
        super.registerButton(BUTTON_RECIPES_LIST, this::recipeListClicked);
    }

    private void recipeListClicked()
    {
        @NotNull final WindowListRecipes window = new WindowListRecipes(building.getColony(), building.getLocation());
        window.open();
    }

    private void craftingClicked()
    {
        final BlockPos pos = building.getLocation();
        Minecraft.getMinecraft().player.openGui(MineColonies.instance, 0, Minecraft.getMinecraft().world, pos.getX(), pos.getY(), pos.getZ());
        MineColonies.getNetwork().sendToServer(new OpenCraftingGUIMessage(building, 2));
    }

    /**
     * Action when a hire button is clicked.
     * If there is no worker (worker.Id == 0) => Contract someone.
     * Else => Fire the current worker.
     *
     * @param button the clicked button.
     */
    private void hireClicked(@NotNull final Button button)
    {
        if (building.getColony().isManualHiring())
        {
            if (building.getBuildingLevel() == 0 && !BUILDER_HUT_NAME.equals(getBuildingName()))
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.gui.workerHuts.level0");
                return;
            }

            @NotNull final WindowHireWorker window = new WindowHireWorker(building.getColony(), building.getLocation());
            window.open();
        }
    }

    /**
     * Action when a recall button is clicked.
     */
    private void recallClicked()
    {
        MineColonies.getNetwork().sendToServer(new RecallCitizenMessage(building));
    }

    /**
     * Called when the GUI has been opened.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        String workerName = "";
        String workerLevel = "";

        if (!building.getWorkerId().isEmpty())
        {
            final CitizenDataView worker = building.getColony().getCitizen(building.getWorkerId().get(0));
            if (worker != null)
            {
                workerName = worker.getName();
                workerLevel = String.format("%d", worker.getLevel());
            }
        }

        findPaneOfTypeByID(BUTTON_HIRE, Button.class).setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.hire"));

        findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabelText(workerName);
        findPaneOfTypeByID(LABEL_WORKERLEVEL, Label.class)
          .setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.workerLevel", workerLevel));
        findPaneOfTypeByID(LABEL_BUILDINGTYPE, Label.class).setLabelText(building.getBuildingDmPrio() + "/10");
    }
}
