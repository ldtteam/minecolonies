package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the dyer building.
 */
public class BuildingArcheologist extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String ARCHEOLOGIST = "archeologist";

    /**
     * Instantiates a new dyer building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingArcheologist(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return ARCHEOLOGIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }
}
