package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.fields.plantation.registry.PlantationFieldRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModPlantationFieldsInitializer
{
    public static final DeferredRegister<PlantationFieldRegistries.FieldEntry> DEFERRED_REGISTER =
      DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "plantationfields"), Constants.MOD_ID);
    static
    {
        PlantationFieldRegistries.sugarCaneField = registerEntry(PlantationFieldRegistries.SUGAR_CANE_FIELD_ID,
          new SugarCanePlantModule("sugar_field", "sugar", Items.SUGAR_CANE));

        PlantationFieldRegistries.cactusField = registerEntry(PlantationFieldRegistries.CACTUS_FIELD_ID,
          new CactusPlantModule("cactus_field", "cactus", Items.CACTUS));

        PlantationFieldRegistries.bambooField = registerEntry(PlantationFieldRegistries.BAMBOO_FIELD_ID,
          new BambooPlantModule("bamboo_field", "bamboo", Items.BAMBOO));

        PlantationFieldRegistries.cocoaBeansField = registerEntry(PlantationFieldRegistries.COCOA_BEANS_FIELD_ID,
          new CocoaPlantModule("cocoa_field", "cocoa", Items.COCOA_BEANS));

        PlantationFieldRegistries.vinesField = registerEntry(PlantationFieldRegistries.VINES_FIELD_ID,
          new VinePlantModule("vine_field", "vine", Items.VINE));

        PlantationFieldRegistries.kelpField = registerEntry(PlantationFieldRegistries.KELP_FIELD_ID,
          new KelpPlantModule("kelp_field", "kelp", Items.KELP));

        PlantationFieldRegistries.seagrassField = registerEntry(PlantationFieldRegistries.SEAGRASS_FIELD_ID,
          new SeagrassPlantModule("seagrass_field", "seagrass", Items.SEAGRASS));

        PlantationFieldRegistries.seaPicklesField = registerEntry(PlantationFieldRegistries.SEA_PICKLES_FIELD_ID,
          new SeapicklePlantModule("seapickle_field", "seapickle", Items.SEA_PICKLE));

        PlantationFieldRegistries.glowberriesField = registerEntry(PlantationFieldRegistries.GLOWBERRIES_FIELD_ID,
          new GlowBerriesPlantModule("glowb_field", "glowb_vine", Items.GLOW_BERRIES));

        PlantationFieldRegistries.weepingVinesField = registerEntry(PlantationFieldRegistries.WEEPING_VINES_FIELD_ID,
          new WeepingVinesPlantModule("weepv_field", "weepv_vine", Items.WEEPING_VINES));

        PlantationFieldRegistries.twistingVinesField = registerEntry(PlantationFieldRegistries.TWISTING_VINES_FIELD_ID,
          new TwistingVinesPlantModule("twistv_field", "twistv_vine", Items.TWISTING_VINES));

        PlantationFieldRegistries.twistingVinesField = registerEntry(PlantationFieldRegistries.CRIMSON_PLANTS_FIELD_ID,
          new CrimsonPlantsPlantModule("crimsonp_field", "crimsonp_ground", Items.CRIMSON_FUNGUS));

        PlantationFieldRegistries.twistingVinesField = registerEntry(PlantationFieldRegistries.WARPED_PLANTS_FIELD_ID,
          new WarpedPlantsPlantModule("warpedp_field", "warpedp_ground", Items.WARPED_FUNGUS));
    }
    private ModPlantationFieldsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModPlantationFieldsInitializer but this is a Utility class.");
    }

    private static RegistryObject<PlantationFieldRegistries.FieldEntry> registerEntry(final ResourceLocation resourceLocation, final IPlantationModule plantationModule)
    {
        return DEFERRED_REGISTER.register(resourceLocation.getPath(), () -> new PlantationFieldRegistries.FieldEntry(resourceLocation, plantationModule));
    }
}
