package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.VerticalGrowingPlantModule;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class PlantationModuleRegistry
{
    /**
     * The default quantity to request.
     */
    private static final Integer DEFAULT_MAX_PLANTS        = 20;
    /**
     * The default quantity to request.
     */
    private static final Integer DEFAULT_PLANTS_TO_REQUEST = 16;

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
              new VerticalGrowingPlantModule("sugar_field", "sugar", Blocks.SUGAR_CANE, DEFAULT_MAX_PLANTS, DEFAULT_PLANTS_TO_REQUEST, 3));
            plantationModules.put(PlantationFieldType.CACTUS,
              new VerticalGrowingPlantModule("cactus_field", "cactus", Blocks.CACTUS, DEFAULT_MAX_PLANTS, DEFAULT_PLANTS_TO_REQUEST, 3));
            plantationModules.put(PlantationFieldType.BAMBOO,
              new VerticalGrowingPlantModule("bamboo_field", "bamboo", Blocks.BAMBOO, DEFAULT_MAX_PLANTS, DEFAULT_PLANTS_TO_REQUEST, 6));
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
