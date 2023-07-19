package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific.*;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.colony.fields.PlantationField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public final class ModFieldsInitializer
{
    public static final DeferredRegister<FieldRegistries.FieldEntry> DEFERRED_REGISTER =
      DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "fields"), Constants.MOD_ID);
    static
    {
        FieldRegistries.farmField = createEntry(FieldRegistries.FARM_FIELD_ID,
          builder -> builder.setFieldProducer(FarmField::new));

        FieldRegistries.plantationSugarCaneField = createEntry(FieldRegistries.PLANTATION_SUGAR_CANE_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new SugarCanePlantModule(field,"sugar_field", "sugar", Items.SUGAR_CANE)));

        FieldRegistries.plantationCactusField = createEntry(FieldRegistries.PLANTATION_CACTUS_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new CactusPlantModule(field,"cactus_field", "cactus", Items.CACTUS)));

        FieldRegistries.plantationBambooField = createEntry(FieldRegistries.PLANTATION_BAMBOO_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new BambooPlantModule(field,"bamboo_field", "bamboo", Items.BAMBOO)));

        FieldRegistries.plantationCocoaBeansField = createEntry(FieldRegistries.PLANTATION_COCOA_BEANS_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new CocoaPlantModule(field,"cocoa_field", "cocoa", Items.COCOA_BEANS)));

        FieldRegistries.plantationVinesField = createEntry(FieldRegistries.PLANTATION_VINES_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new VinePlantModule(field,"vine_field", "vine", Items.VINE)));

        FieldRegistries.plantationKelpField = createEntry(FieldRegistries.PLANTATION_KELP_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new KelpPlantModule(field,"kelp_field", "kelp", Items.KELP)));

        FieldRegistries.plantationSeagrassField = createEntry(FieldRegistries.PLANTATION_SEAGRASS_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new SeagrassPlantModule(field,"seagrass_field", "seagrass", Items.SEAGRASS)));

        FieldRegistries.plantationSeaPicklesField = createEntry(FieldRegistries.PLANTATION_SEA_PICKLES_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new SeapicklePlantModule(field,"seapickle_field", "seapickle", Items.SEA_PICKLE)));

        FieldRegistries.plantationGlowberriesField = createEntry(FieldRegistries.PLANTATION_GLOWBERRIES_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new GlowBerriesPlantModule(field,"glowb_field", "glowb_vine", Items.GLOW_BERRIES)));

        FieldRegistries.plantationWeepingVinesField = createEntry(FieldRegistries.PLANTATION_WEEPING_VINES_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new WeepingVinesPlantModule(field,"weepv_field", "weepv_vine", Items.WEEPING_VINES)));

        FieldRegistries.plantationTwistingVinesField = createEntry(FieldRegistries.PLANTATION_TWISTING_VINES_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new TwistingVinesPlantModule(field,"twistv_field", "twistv_vine", Items.TWISTING_VINES)));

        FieldRegistries.plantationCrimsonPlantsField = createEntry(FieldRegistries.PLANTATION_CRIMSON_PLANTS_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new CrimsonPlantsPlantModule(field,"crimsonp_field", "crimsonp_ground", Items.CRIMSON_FUNGUS)));

        FieldRegistries.plantationWarpedPlantsField = createEntry(FieldRegistries.PLANTATION_WARPED_PLANTS_FIELD_ID,
          builder -> builder.setFieldProducer(PlantationField::new)
                       .addFieldModuleProducer(field -> new WarpedPlantsPlantModule(field,"warpedp_field", "warpedp_ground", Items.WARPED_FUNGUS)));
    }
    private ModFieldsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModFieldsInitializer but this is a Utility class.");
    }

    private static RegistryObject<FieldRegistries.FieldEntry> createEntry(ResourceLocation registryName, Consumer<FieldRegistries.FieldEntry.Builder> builder)
    {
        FieldRegistries.FieldEntry.Builder field = new FieldRegistries.FieldEntry.Builder()
                                                     .setRegistryName(registryName);
        builder.accept(field);
        return DEFERRED_REGISTER.register(registryName.getPath(), field::createFieldEntry);
    }
}
