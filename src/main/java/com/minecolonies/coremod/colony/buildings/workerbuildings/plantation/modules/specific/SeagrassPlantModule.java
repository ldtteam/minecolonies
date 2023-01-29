package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_SEA;

/**
 * Planter module for growing {@link Items#SEAGRASS}.
 */
public class SeagrassPlantModule extends BoneMealedPlantModule
{
    /**
     * Default constructor.
     */
    public SeagrassPlantModule()
    {
        super("seagrass_field", "seagrass", Items.SEAGRASS);
    }

    @Override
    protected @Nullable BlockPos getPositionToHarvest(final PlantationField field)
    {
        // Because seagrass grows underwater, we can't check for air blocks when checking for blocks to harvest.
        // Instead, we check for water blocks directly, any other block should be harvestable (because it's either a full block instead of water or a waterlogged block).
        return field.getWorkingPositions().stream()
                 .map(BlockPos::above)
                 .filter(pos -> field.getColony().getWorld().getBlockState(pos).getFluidState().is(Fluids.WATER))
                 .findFirst()
                 .orElse(null);
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_SEA;
    }
}
