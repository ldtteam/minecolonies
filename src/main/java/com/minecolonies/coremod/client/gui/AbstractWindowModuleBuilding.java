package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildRequestMessage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Manage windows associated with Buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingView}.
 */
public abstract class AbstractWindowModuleBuilding<B extends IBuildingView> extends AbstractModuleWindow
{
    /**
     * Type B is a class that extends {@link com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView}.
     */
    protected final B      building;
    private final   Text   title;
    private final   Button buttonBuild;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param building Class extending {@link AbstractBuildingView}.
     * @param resource Resource location string.
     */
    public AbstractWindowModuleBuilding(final B building, final String resource)
    {
        super(building, resource);

        this.building = building;
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_INFO, this::infoClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        registerButton(BUTTON_EDIT_NAME, this::editName);
        registerButton(BUTTON_ALLINVENTORY, this::allInventoryClicked);

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);
        buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        Button buttonInfo = findPaneOfTypeByID(BUTTON_INFO, Button.class);
        if (buttonInfo != null)
        {
            buttonInfo.setVisible(I18n.exists(COM_MINECOLONIES_INFO_PREFIX + building.getSchematicName() + ".0"));
        }
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
     * Action when info button is clicked.
     */
    private void infoClicked()
    {
        @NotNull final WindowInfo window = new WindowInfo(building);
        window.open();
    }

    /**
     * Action when allInventory button is clicked.
     */
    private void allInventoryClicked()
    {
        @NotNull final WindowHutAllInventory window = new WindowHutAllInventory(building, this);
        window.open();
    }

    /**
     * Action when build button is clicked.
     */
    private void buildClicked()
    {
        final String buttonLabel = buttonBuild.getTextAsString();
        if (buttonLabel.equalsIgnoreCase(LanguageHandler.format(ACTION_CANCEL_BUILD))
              || buttonLabel.equalsIgnoreCase(LanguageHandler.format(ACTION_CANCEL_UPGRADE)))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.BUILD, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(LanguageHandler.format(ACTION_CANCEL_REPAIR)))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REPAIR, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(LanguageHandler.format(ACTION_CANCEL_DECONSTRUCTION)))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REMOVE, BlockPos.ZERO));
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
        Network.getNetwork().sendToServer(new OpenInventoryMessage(building));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateButtonBuild(building);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    public String getBuildingName()
    {
        return  "com." + buildingView.getBuildingType().getRegistryName().getNamespace() + ".building." + buildingView.getBuildingType().getRegistryName().getPath();
    }

    /**
     * Update the state and label for the Build button.
     *
     * @param buildingView the view to update from.
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
                buttonBuild.setText(new TranslationTextComponent(ACTION_CANCEL_BUILD));
            }
            else
            {
                buttonBuild.setText(new TranslationTextComponent(ACTION_CANCEL_UPGRADE));
            }
        }
        else if (buildingView.isRepairing())
        {
            buttonBuild.setText(new TranslationTextComponent(ACTION_CANCEL_REPAIR));
        }
        else if (buildingView.isDeconstructing())
        {
            buttonBuild.setText(new TranslationTextComponent(ACTION_CANCEL_DECONSTRUCTION));
        }
        else
        {
            buttonBuild.setText(new TranslationTextComponent(ACTION_BUILD_REPAIR));
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setPage(false, 0);

        final IFormattableTextComponent component = building.getCustomName().isEmpty() ? new TranslationTextComponent(getBuildingName()) : new StringTextComponent(building.getCustomName());
        if (switchView != null && switchView.getID().equals(GUI_LIST_BUTTON_SWITCH + PAGE_ACTIONS))
        {
            // Town hall does not need level in colony name
            title.setText(component);
            findPaneOfTypeByID(LEVEL_LABEL, Text.class).setText(LanguageHandler.format(CMC_GUI_TOWNHALL_BUILDING_LEVEL) + ": " + buildingView.getBuildingLevel());
        }
        else if (title != null)
        {
            final IFormattableTextComponent componentWithLevel = component.append(" ").append(String.valueOf(buildingView.getBuildingLevel()));
            title.setText(componentWithLevel);
        }
    }
}
