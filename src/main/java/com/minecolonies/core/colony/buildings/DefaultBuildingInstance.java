package com.minecolonies.core.colony.buildings;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Default building instance.
 */
public class DefaultBuildingInstance extends AbstractBuilding
{
    /**
     * the schematic name of the building.
     */
    public final String schematicName;

    /**
     * Max building level.
     */
    private final int maxBuildingLevel;

    /**
     * Create a default building instance that's based on modules and not inheritance.
     * @param colony the colony.
     * @param pos the position.
     * @param schematicName the schematic name.
     */
    public DefaultBuildingInstance(final IColony colony, final BlockPos pos, final String schematicName, final int maxLevel)
    {
        super(colony, pos);
        this.schematicName = schematicName;
        this.maxBuildingLevel = maxLevel;
    }

    //todo replace in the future which a cache and schematic check (similar to rotation).
    @Override
    public int getMaxBuildingLevel()
    {
        return maxBuildingLevel;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return schematicName;
    }
}
