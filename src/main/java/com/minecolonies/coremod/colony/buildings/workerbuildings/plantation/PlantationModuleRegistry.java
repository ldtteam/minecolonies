package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific.*;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

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
            // Default plants
            plantationModules.put(PlantationFieldType.SUGAR_CANE, new SugarCanePlantModule());
            plantationModules.put(PlantationFieldType.CACTUS, new CactusPlantModule());

            // Jungle plants
            plantationModules.put(PlantationFieldType.BAMBOO, new BambooPlantModule());
            plantationModules.put(PlantationFieldType.COCOA_BEANS, new CocoaPlantModule());
            plantationModules.put(PlantationFieldType.VINES, new VinePlantModule());

            // Sea plants
            plantationModules.put(PlantationFieldType.KELP, new KelpPlantModule());
            plantationModules.put(PlantationFieldType.SEAGRASS, new SeagrassPlantModule());
            plantationModules.put(PlantationFieldType.SEA_PICKLES, new SeapicklePlantModule());

            // Exotic plants
            plantationModules.put(PlantationFieldType.GLOWBERRIES, new GlowBerriesPlantModule());

            // Nether plants
            plantationModules.put(PlantationFieldType.WEEPING_VINES, new WeepingVinesPlantModule());
            plantationModules.put(PlantationFieldType.TWISTING_VINES, new TwistingVinesPlantModule());
            plantationModules.put(PlantationFieldType.CRIMSON_FUNGUS, new CrimsonPlantsPlantModule());
            plantationModules.put(PlantationFieldType.WARPED_FUNGUS, new WarpedPlantsPlantModule());
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
