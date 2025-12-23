package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.specific.*;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.colony.buildingextensions.PlantationField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.SchematicTagConstants.*;

public final class ModBuildingExtensionsInitializer
{
    public static final DeferredRegister<BuildingExtensionEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "buildingextensions"), Constants.MOD_ID);
    static
    {
        BuildingExtensionRegistries.farmField = createEntry(BuildingExtensionRegistries.FARM_FIELD_ID, builder -> builder.setExtensionProducer(FarmField::new));

        BuildingExtensionRegistries.plantationSugarCaneField = createEntry(BuildingExtensionRegistries.PLANTATION_SUGAR_CANE_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SugarCanePlantModule(field, SUGAR_FIELD, SUGAR_CROP, Items.SUGAR_CANE)));

        BuildingExtensionRegistries.plantationCactusField = createEntry(BuildingExtensionRegistries.PLANTATION_CACTUS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new CactusPlantModule(field, CACTUS_FIELD, CACTUS_CROP, Items.CACTUS)));

        BuildingExtensionRegistries.plantationBambooField = createEntry(BuildingExtensionRegistries.PLANTATION_BAMBOO_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new BambooPlantModule(field, BAMBOO_FIELD, BAMBOO_CROP, Items.BAMBOO)));

        BuildingExtensionRegistries.plantationCocoaBeansField = createEntry(BuildingExtensionRegistries.PLANTATION_COCOA_BEANS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new CocoaPlantModule(field, COCOA_FIELD, COCOA_CROP, Items.COCOA_BEANS)));

        BuildingExtensionRegistries.plantationVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new VinePlantModule(field, VINE_FIELD, VINE_CROP, Items.VINE)));

        BuildingExtensionRegistries.plantationKelpField = createEntry(BuildingExtensionRegistries.PLANTATION_KELP_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new).addExtensionModuleProducer(field -> new KelpPlantModule(field, KELP_FIELD, KELP_CROP, Items.KELP)));

        BuildingExtensionRegistries.plantationSeagrassField = createEntry(BuildingExtensionRegistries.PLANTATION_SEAGRASS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SeagrassPlantModule(field, SEA_GRASS_FIELD, SEA_GRASS_CROP, Items.SEAGRASS)));

        BuildingExtensionRegistries.plantationSeaPicklesField = createEntry(BuildingExtensionRegistries.PLANTATION_SEA_PICKLES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new SeapicklePlantModule(field, SEA_PICKLE_FIELD, SEA_PICKLE_CROP, Items.SEA_PICKLE)));

        BuildingExtensionRegistries.plantationGlowberriesField = createEntry(BuildingExtensionRegistries.PLANTATION_GLOWBERRIES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new GlowBerriesPlantModule(field, GLOW_BERRY_FIELD, GLOW_BERRY_CROP, Items.GLOW_BERRIES)));

        BuildingExtensionRegistries.plantationWeepingVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_WEEPING_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new WeepingVinesPlantModule(field, WEEPY_VINE_FIELD, WEEPY_VINE_CROP, Items.WEEPING_VINES)));

        BuildingExtensionRegistries.plantationTwistingVinesField = createEntry(BuildingExtensionRegistries.PLANTATION_TWISTING_VINES_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new TwistingVinesPlantModule(field, TWISTY_VINE_FIELD, TWISTY_VINE_CROP, Items.TWISTING_VINES)));

        BuildingExtensionRegistries.plantationCrimsonPlantsField = createEntry(BuildingExtensionRegistries.PLANTATION_CRIMSON_PLANTS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new CrimsonPlantsPlantModule(field, CRIMSON_FIELD, CRIMSON_CROP, Items.CRIMSON_FUNGUS)));

        BuildingExtensionRegistries.plantationWarpedPlantsField = createEntry(BuildingExtensionRegistries.PLANTATION_WARPED_PLANTS_FIELD_ID,
          builder -> builder.setExtensionProducer(PlantationField::new)
                       .addExtensionModuleProducer(field -> new WarpedPlantsPlantModule(field, WARPED_FIELD, WARPED_CROP, Items.WARPED_FUNGUS)));
    }
    private ModBuildingExtensionsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModFieldsInitializer but this is a Utility class.");
    }

    private static RegistryObject<BuildingExtensionEntry> createEntry(ResourceLocation registryName, Consumer<BuildingExtensionEntry.Builder> builder)
    {
        BuildingExtensionEntry.Builder field = new BuildingExtensionEntry.Builder().setRegistryName(registryName);
        builder.accept(field);
        return DEFERRED_REGISTER.register(registryName.getPath(), field::createExtensionEntry);
    }
}
