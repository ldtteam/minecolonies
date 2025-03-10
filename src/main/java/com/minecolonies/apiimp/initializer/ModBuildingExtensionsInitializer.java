package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.specific.*;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.colony.buildingextensions.PlantationField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public final class ModBuildingExtensionsInitializer
{
    public static final DeferredRegister<BuildingExtensionEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.BUILDING_EXTENSIONS, Constants.MOD_ID);
    static
    {
        BuildingExtensionRegistries.farmField = createEntry(BuildingExtensionRegistries.FARM_FIELD_ID, builder -> builder.setExtensionProducer(FarmField::new));

        BuildingExtensionRegistries.plantationSugarCaneField = createEntry(BuildingExtensionRegistries.PLANTATION_SUGAR_CANE_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SugarCanePlantModule(field, "sugar_field", "sugar", Items.SUGAR_CANE)));

        BuildingExtensionRegistries.plantationCactusField = createEntry(BuildingExtensionRegistries.PLANTATION_CACTUS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new CactusPlantModule(field, "cactus_field", "cactus", Items.CACTUS)));

        BuildingExtensionRegistries.plantationBambooField = createEntry(BuildingExtensionRegistries.PLANTATION_BAMBOO_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new BambooPlantModule(field, "bamboo_field", "bamboo", Items.BAMBOO)));

        BuildingExtensionRegistries.plantationCocoaBeansField = createEntry(BuildingExtensionRegistries.PLANTATION_COCOA_BEANS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new CocoaPlantModule(field, "cocoa_field", "cocoa", Items.COCOA_BEANS)));

        BuildingExtensionRegistries.plantationVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new VinePlantModule(field, "vine_field", "vine", Items.VINE)));

        BuildingExtensionRegistries.plantationKelpField = createEntry(BuildingExtensionRegistries.PLANTATION_KELP_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new KelpPlantModule(field, "kelp_field", "kelp", Items.KELP)));

        BuildingExtensionRegistries.plantationSeagrassField = createEntry(BuildingExtensionRegistries.PLANTATION_SEAGRASS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SeagrassPlantModule(field, "seagrass_field", "seagrass", Items.SEAGRASS)));

        BuildingExtensionRegistries.plantationSeaPicklesField = createEntry(BuildingExtensionRegistries.PLANTATION_SEA_PICKLES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SeapicklePlantModule(field, "seapickle_field", "seapickle", Items.SEA_PICKLE)));

        BuildingExtensionRegistries.plantationGlowberriesField = createEntry(BuildingExtensionRegistries.PLANTATION_GLOWBERRIES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new GlowBerriesPlantModule(field, "glowb_field", "glowb_vine", Items.GLOW_BERRIES)));

        BuildingExtensionRegistries.plantationWeepingVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_WEEPING_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new WeepingVinesPlantModule(field, "weepv_field", "weepv_vine", Items.WEEPING_VINES)));

        BuildingExtensionRegistries.plantationTwistingVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_TWISTING_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new TwistingVinesPlantModule(field, "twistv_field", "twistv_vine", Items.TWISTING_VINES)));

        BuildingExtensionRegistries.plantationCrimsonPlantsField = createEntry(BuildingExtensionRegistries.PLANTATION_CRIMSON_PLANTS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new CrimsonPlantsPlantModule(field, "crimsonp_field", "crimsonp_ground", Items.CRIMSON_FUNGUS)));

        BuildingExtensionRegistries.plantationWarpedPlantsField = createEntry(BuildingExtensionRegistries.PLANTATION_WARPED_PLANTS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new WarpedPlantsPlantModule(field, "warpedp_field", "warpedp_ground", Items.WARPED_FUNGUS)));
    }
    private ModBuildingExtensionsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBuildingExtensionsInitializer but this is a Utility class.");
    }

    private static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> createEntry(ResourceLocation registryName, Consumer<BuildingExtensionEntry.Builder> builder)
    {
        BuildingExtensionEntry.Builder field = new BuildingExtensionEntry.Builder().setRegistryName(registryName);
        builder.accept(field);
        return DEFERRED_REGISTER.register(registryName.getPath(), field::createExtensionEntry);
    }
}
