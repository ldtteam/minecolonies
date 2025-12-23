package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.building.BuildRequestMessage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Base window class for the main window behind buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingView}.
 */
public abstract class AbstractBuildingMainWindow<B extends IBuildingView> extends AbstractBuildingWindow<B>
{
    /**
     * The title displayed at the top of the window showing the building name.
     */
    private final Text title;

    /**
     * The build button.
     */
    private final Button buttonBuild;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param buildingView Class extending {@link AbstractBuildingView}.
     * @param resource     window resource location.
     */
    public AbstractBuildingMainWindow(final B buildingView, final ResourceLocation resource)
    {
        super(buildingView, resource);

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
            buttonInfo.setVisible(I18n.exists(PARTIAL_INFO_TEXT + buildingView.getBuildingType().getTranslationKey().replace("com.minecolonies.building.", "") + ".0"));
        }
    }

    /**
     * Action when build button is clicked.
     */
    private void buildClicked()
    {
        final String buttonLabel = buttonBuild.getText().getContents() instanceof TranslatableContents
            ? ((TranslatableContents) buttonBuild.getText().getContents()).getKey()
            : buttonBuild.getTextAsString();

        if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_BUILD) || buttonLabel.equalsIgnoreCase(ACTION_CANCEL_UPGRADE))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.BUILD, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_REPAIR))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.REPAIR, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_DECONSTRUCTION))
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.REMOVE, BlockPos.ZERO));
        }
        else
        {
            new WindowBuildBuilding(buildingView.getColony(), buildingView).open();
        }
    }

    /**
     * Action when info button is clicked.
     */
    private void infoClicked()
    {
        new WindowInfo(buildingView).open();
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(buildingView));
    }

    /**
     * Edit custom name action.
     */
    private void editName()
    {
        new WindowHutNameEntry(buildingView).open();
    }

    /**
     * Action when allInventory button is clicked.
     */
    private void allInventoryClicked()
    {
        new WindowHutAllInventory(buildingView, this).open();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateButtonBuild(buildingView);
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
                buttonBuild.setText(Component.translatable(ACTION_CANCEL_BUILD));
            }
            else
            {
                buttonBuild.setText(Component.translatable(ACTION_CANCEL_UPGRADE));
            }
        }
        else if (buildingView.isRepairing())
        {
            buttonBuild.setText(Component.translatable(ACTION_CANCEL_REPAIR));
        }
        else if (buildingView.isDeconstructing())
        {
            buttonBuild.setText(Component.translatable(ACTION_CANCEL_DECONSTRUCTION));
        }
        else
        {
            buttonBuild.setText(Component.translatable(ACTION_BUILD_REPAIR));
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setPage(false, 0);

        if (title != null)
        {
            final MutableComponent component = Component.translatable(buildingView.getBuildingDisplayName());
            final MutableComponent componentWithLevel = component.append(" ").append(String.valueOf(buildingView.getBuildingLevel()));
            title.setText(componentWithLevel);
        }

        updateButtonBuild(this.buildingView);
    }
}
