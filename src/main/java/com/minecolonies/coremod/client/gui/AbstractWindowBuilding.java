package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Label;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.Network;
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
public abstract class AbstractWindowBuilding<B extends IBuildingView> extends AbstractWindowSkeleton
{
    /**
     * Type B is a class that extends {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View}.
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
        registerButton(BUTTON_EDIT_NAME, this::editName);

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Label.class);
        buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
    }

    /**
     * Edit custom name action.
     */
    private void editName()
    {
        @NotNull final WindowHutNameEntry window = new WindowHutNameEntry(building);
        window.open();
    }

    /**
     * Action when build button is clicked.
     */
    private void buildClicked()
    {
        final String buttonLabel = buttonBuild.getLabel();
        if(buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelBuild"))
                || buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelUpgrade")))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelRepair")))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR, BlockPos.ZERO));
        }
        else
        {
            @NotNull final WindowBuildBuilding window = new WindowBuildBuilding(building.getColony(), building);
            window.open();
        }
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(building.getID()));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        // Check if we are on the default page
        if (switchView.getCurrentView().getID().equals(PAGE_ACTIONS))
        {
            final IBuildingView buildingView = building;
            if (title != null && buildingView != null)
            {
                final String name = building.getCustomName().isEmpty() ? LanguageHandler.format(getBuildingName()) : building.getCustomName();
                if (switchView.getID().equals(GUI_LIST_BUTTON_SWITCH + PAGE_ACTIONS))
                {
                    // Townhall does not need level in colony name
                    title.setLabelText(name);
                    findPaneOfTypeByID(LEVEL_LABEL, Label.class).setLabelText(LanguageHandler.format(CMC_GUI_TOWNHALL_BUILDING_LEVEL) + ": " + buildingView.getBuildingLevel());
                }
                else
                {
                    title.setLabelText(name + " " + buildingView.getBuildingLevel());
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
    private void updateButtonBuild(final IBuildingView buildingView)
    {
        if (buttonBuild == null)
        {
            return;
        }

        if (buildingView.isBuilding())
        {
            if (buildingView.getBuildingLevel() == 0)
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelBuild"));
            }
            else
            {
                buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelUpgrade"));
            }
        }
        else if (buildingView.isRepairing())
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.cancelRepair"));
        }
        else
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.buildRepair"));
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setPage("");
    }
}
