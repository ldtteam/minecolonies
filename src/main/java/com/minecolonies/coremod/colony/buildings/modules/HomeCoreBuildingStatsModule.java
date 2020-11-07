package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IDefinesCoreBuildingStatsModule;
import com.minecolonies.coremod.client.gui.WindowHutCitizen;
import com.minecolonies.coremod.colony.buildings.views.LivingBuildingView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The class of the citizen hut.
 */
public class HomeCoreBuildingStatsModule extends AbstractBuildingModule implements IDefinesCoreBuildingStatsModule
{
    /**
     * Creates a new home building module.
     * @param building the building it is assigned to.
     */
    public HomeCoreBuildingStatsModule(final IBuilding building)
    {
        super(building);
    }

    @Override
    public int getMaxInhabitants()
    {
        return building.getBuildingLevel();
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends LivingBuildingView
    {
        /**
         * Creates an instance of the citizen hut window.
         *
         * @param c the colonyView.
         * @param l the position the hut is at.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutCitizen(this);
        }
    }
}
