package com.minecolonies.core.client.gui.citizen;

import com.ldtteam.blockui.PaneBuilders;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.client.gui.AbstractWindowRequestTree;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * BOWindow for the citizen.
 */
public abstract class AbstractWindowCitizen extends AbstractWindowRequestTree
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     * @param ui the xml res loc.
     */
    public AbstractWindowCitizen(final ICitizenDataView citizen, final String ui)
    {
        super(citizen.getWorkBuilding(), ui, IColonyManager.getInstance().getColonyView(citizen.getColonyId(), Minecraft.getInstance().level.dimension()));

        registerButton("mainTab", () -> new MainWindowCitizen(citizen).open());
        registerButton("mainIcon", () -> new MainWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("mainIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.main"));

        registerButton("requestTab", () -> new RequestWindowCitizen(citizen).open());
        registerButton("requestIcon", () -> new RequestWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("requestIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.requests"));

        registerButton("inventoryTab", () -> new OpenInventoryMessage(colony, citizen.getName(), citizen.getEntityId()).sendToServer());
        registerButton("inventoryIcon", () -> new OpenInventoryMessage(colony, citizen.getName(), citizen.getEntityId()).sendToServer());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("inventoryIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.inventory"));

        registerButton("happinessTab", () -> new HappinessWindowCitizen(citizen).open());
        registerButton("happinessIcon", () -> new HappinessWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("happinessIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.happiness"));

        registerButton("familyTab", () -> new FamilyWindowCitizen(citizen).open());
        registerButton("familyIcon", () -> new FamilyWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("familyIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.family"));

        final IBuildingView building = colony.getBuilding(citizen.getWorkBuilding());

        if (building instanceof AbstractBuildingView && building.getBuildingType() != ModBuildings.library.get())
        {
            findPaneByID("jobTab").setVisible(true);
            findPaneByID("jobIcon").setVisible(true);

            registerButton("jobTab", () -> new JobWindowCitizen(citizen).open());
            registerButton("jobIcon", () -> new JobWindowCitizen(citizen).open());
            PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("jobIcon")).build().setText(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.job"));
        }
        else
        {
            findPaneByID("jobTab").setVisible(false);
            findPaneByID("jobIcon").setVisible(false);
        }
    }
}
