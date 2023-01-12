package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.BoneMealedFieldPlantModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.VerticalUpwardsGrowingPlantModule;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;
import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Registry class for all different plantation module types.
 */
public class PlantationModuleRegistry
{
    /**
     * Map containing the field types as well as the modules belonging to it.
     */
    private static final EnumMap<PlantationFieldType, PlantationModule> plantationModules = new EnumMap<>(PlantationFieldType.class);

    private PlantationModuleRegistry()
    {
    }

    /**
     * Finds the {@link PlantationFieldType} that has a module where the field tag is the requested field tag or null if there is none.
     *
     * @param fieldTag the field tag.
     * @return the plantation field type this field tag is using.
     */
    @Nullable
    public static PlantationFieldType getFromFieldTag(final String fieldTag)
    {
        registerModules();
        return plantationModules.entrySet().stream()
                 .filter(f -> f.getValue().getFieldTag().equals(fieldTag))
                 .map(Map.Entry::getKey).findFirst()
                 .orElse(null);
    }

    /**
     * Internal method to populate the plantationModules if not yet filled in.
     */
    private static void registerModules()
    {
        if (plantationModules.isEmpty())
        {
            plantationModules.put(PlantationFieldType.SUGAR_CANE,
              new VerticalUpwardsGrowingPlantModule.Builder("sugar_field", "sugar", Blocks.SUGAR_CANE)
                .build());
            plantationModules.put(PlantationFieldType.CACTUS,
              new VerticalUpwardsGrowingPlantModule.Builder("cactus_field", "cactus", Blocks.CACTUS)
                .build());
            plantationModules.put(PlantationFieldType.BAMBOO,
              new VerticalUpwardsGrowingPlantModule.Builder("bamboo_field", "bamboo", Blocks.BAMBOO)
                .withMinimumPlantLength(6)
                .withRequiredResearchEffect(PLANTATION_JUNGLE)
                .build());
            plantationModules.put(PlantationFieldType.CRIMSON_FUNGUS,
              new BoneMealedFieldPlantModule.Builder("crimsonp_field", "plant", Blocks.CRIMSON_FUNGUS)
                .withPercentageChance(5)
                .withRequiredResearchEffect(PLANTATION_NETHER)
                .withMaxPlants(50)
                .build());
            plantationModules.put(PlantationFieldType.WARPED_FUNGUS,
              new BoneMealedFieldPlantModule.Builder("warpedp_field", "plant", Blocks.WARPED_FUNGUS)
                .withPercentageChance(5)
                .withRequiredResearchEffect(PLANTATION_NETHER)
                .withMaxPlants(50)
                .build());
        }
    }

    /**
     * Get the appropriate plantation module depending on the field type.
     *
     * @param plantationFieldType the plantation field type.
     * @return the plantation module belonging to the field type.
     */
    @Nullable
    public static PlantationModule getPlantationModule(@Nullable PlantationFieldType plantationFieldType)
    {
        registerModules();
        return plantationModules.get(plantationFieldType);
    }
}
