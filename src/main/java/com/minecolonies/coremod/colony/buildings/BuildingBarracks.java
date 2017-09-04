package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Building for the Barracks.
 */
public class BuildingBarracks extends AbstractBuilding
{
    //todo GUI not working
    //todo scan all in
    //todo try to build
    //todo if not build set tower position manually.
    //todo rotate tower as neccessary
    //todo let tower get up to 5 worker (guards)
    //todo coordiante all guards using the GUI.
    /**
     * General Barracks description key.
     */
    private static final String BARRACKS = "Barracks";

    /**
     * Max hut level of the Barracks.
     */
    private static final int BARRACKS_HUT_MAX_LEVEL = 5;

    /**
     * Constructor for the Barracks building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBarracks(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Barracks schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BARRACKS;
    }

    /**
     * Gets the max level of the Barracks's hut.
     *
     * @return The max level of the Barracks's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BARRACKS_HUT_MAX_LEVEL;
    }
}
