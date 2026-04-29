package com.minecolonies.core.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.moduleviews.LivingBuildingModuleView;
import com.minecolonies.core.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Living building view.
 */
public abstract class LivingBuildingView extends AbstractBuildingView
{
    /**
     * Creates an instance of the citizen hut window.
     *
     * @param c the colonyView.
     * @param l the position the hut is at.
     */
    public LivingBuildingView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter for the list of residents.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<Integer> getResidents()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getAssignedCitizens();
    }

    /**
     * Removes a resident from the building.
     *
     * @param id the index to remove it from.
     */
    public void removeResident(final int id)
    {
        getModuleViewByType(LivingBuildingModuleView.class).remove(id);
    }

    /**
     * Add a resident from the building.
     *
     * @param id the id of the citizen.
     */
    public void addResident(final int id)
    {
        getModuleViewByType(LivingBuildingModuleView.class).add(id);
    }

    /**
     * Get the max citizens.
     * @return the max.
     */
    public int getMax()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getMax();
    }

    /**
     * Get the  hiring mode.
     * @return the mode.
     */
    public HiringMode getHiringMode()
    {
        return getModuleViewByType(LivingBuildingModuleView.class).getHiringMode();
    }

    /**
     * Adjust the hiring mode.
     * @param value the value to set it to.
     */
    public void setHiringMode(final HiringMode value)
    {
        getModuleViewByType(LivingBuildingModuleView.class).setHiringMode(value);
        new BuildingHiringModeMessage(this, value, getModuleViewByType(LivingBuildingModuleView.class).getProducer().getRuntimeID()).sendToServer();
    }

    @Override
    public String getHoverWarningForLevel()
    {
        switch (getBuildingLevel())
        {
            case 1 ->
                {
                    // Have a fisher or farmer
                    if (getColony().getCommonBuildingManager().hasBuilding(ModBuildings.fisherman.get().getRegistryName(), 1, false)
                        || getColony().getCommonBuildingManager().hasBuilding(ModBuildings.farmer.get().getRegistryName(), 1, false)
                    )
                    {
                        return "";
                    }
                    return "com.minecolonies.core.gui.residence.warning." + (getBuildingLevel() + 1);
                }
            case 2 ->
            {
                if (checkColonyMenu(getColony(), 1))
                {
                    return "";
                }

                return "com.minecolonies.core.gui.residence.warning." + (getBuildingLevel() + 1);
            }
            case 3, 4 ->
            {
                if (checkColonyMenu(getColony(), 2))
                {
                    return "";
                }

                return "com.minecolonies.core.gui.residence.warning." + (getBuildingLevel() + 1);
            }
            default -> super.getHoverWarningForLevel();
        }
        return "";
    }

    /**
     * Check if the colony has a dining hall with some min food on the menu.
     * @param colonyView the colony to check this for.
     * @param minTier the min food tier.
     * @return true if so.
     */
    private static boolean checkColonyMenu(final IColonyView colonyView, final int minTier)
    {
        for (final IBuildingView buildingView : colonyView.getClientBuildingManager().getBuildings().values())
        {
            if (buildingView.getBuildingType() == ModBuildings.cook.get())
            {
                for (final ItemStorage storage : buildingView.getModuleView(BuildingModules.RESTAURANT_MENU).getMenu())
                {
                    if (storage.getItem() instanceof IMinecoloniesFoodItem minecoloniesFoodItem && minecoloniesFoodItem.getTier() >= minTier)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
