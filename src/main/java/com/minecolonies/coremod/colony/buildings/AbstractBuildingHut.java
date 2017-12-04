package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Contains basic methods that all Huts will need.
 */
public abstract class AbstractBuildingHut extends AbstractBuilding
{

    /**
     * Simple constructor, just calls super.
     *
     * @param c The colony that this building belongs too.
     * @param l The location of this building.
     */
    public AbstractBuildingHut(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Returns the max amount of inhabitants.
     *
     * @return Max inhabitants.
     */
    public int getMaxInhabitants()
    {
        return 1;
    }

    /**
     * BuildingHut view for the client.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Constructor for the BuildingHut view.
         *
         * @param c ColonyView associated with this building.
         * @param l The location of this building.
         */
        protected View(final ColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }
    }
}
