package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.CMC_GUI_TOWNHALL_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Manage windows associated with Buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingView}.
 */
public abstract class AbstractWindowBuilding<B extends AbstractBuildingView> extends AbstractWindowSkeleton
{
    /**
     * Type B is a class that extends {@link AbstractBuildingWorker.View}.
     */
    protected final B          building;
    private final   Label      title;
    private final   Button     buttonBuild;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param building Class extending {@link AbstractBuildingView}.
     * @param resource Resource location string.
     */
    public AbstractWindowBuilding(final B building, final String resource)
    {
        super(resource);

        this.building = building;
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class);
        buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
    }

    /**
     * Action when build button is clicked.
     */
    private void buildClicked()
    {
        final String buttonLabel = buttonBuild.getLabel();
        if (buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelBuild"))
              || buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelUpgrade")))
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD, BlockPos.ORIGIN));
        }
        else if (buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelRepair")))
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR, BlockPos.ORIGIN));
        }
        else
        {
            @NotNull final WindowBuildBuilding window = new WindowBuildBuilding(building.getColony(), building.getLocation());
            window.open();
        }
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(building.getID()));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        // Check if we are on the default page
        if (switchView.getCurrentView().getID().equals(PAGE_ACTIONS))
        {
            final AbstractBuildingView buildingView = building.getColony().getBuilding(building.getID());

            if (title != null)
            {
                if (switchView.getID().equals(GUI_LIST_BUTTON_SWITCH + PAGE_ACTIONS))
                {
                    // Townhall does not need level in colony name
                    title.setLabelText(getBuildingName());
                    findPaneOfTypeByID(LEVEL_LABEL, Label.class).setLabelText(LanguageHandler.format(CMC_GUI_TOWNHALL_BUILDING_LEVEL) + ": " + buildingView.getBuildingLevel());
                }
                else
                {
                    title.setLabelText(LanguageHandler.format(getBuildingName()) + " " + buildingView.getBuildingLevel());
                }
            }

            updateButtonBuild(buildingView);
        }
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    public abstract String getBuildingName();

    /**
     * Update the state and label for the Build button.
     */
    private void updateButtonBuild(final AbstractBuildingView buildingView)
    {
        if (buttonBuild == null)
        {
            return;
        }

        if (buildingView.isBuilding())
        {
            if (buildingView.getBuildingLevel() == 0)
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelBuild"));
            }
            else
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelUpgrade"));
            }
        }
        else if (buildingView.isRepairing())
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelRepair"));
        }
        else
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.buildRepair"));
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        setPage("");
    }
}
