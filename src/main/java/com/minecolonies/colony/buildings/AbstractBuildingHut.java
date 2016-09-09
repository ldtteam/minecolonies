package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.BlockPos;
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
    public AbstractBuildingHut(@NotNull Colony c, BlockPos l)
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
    public static class View extends AbstractBuilding.View
    {
        /**
         * Constructor for the BuildingHut view.
         *
         * @param c ColonyView associated with this building.
         * @param l The location of this building.
         */
        protected View(ColonyView c, @NotNull BlockPos l)
        {
            super(c, l);
        }
    }
}
