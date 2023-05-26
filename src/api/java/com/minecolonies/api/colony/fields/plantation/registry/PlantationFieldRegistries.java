package com.minecolonies.api.colony.fields.plantation.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

/**
 * Registry implementation for plantation field instances.
 */
public class PlantationFieldRegistries
{
    public static final ResourceLocation SUGAR_CANE_FIELD_ID     = new ResourceLocation(Constants.MOD_ID, "sugar_cane");
    public static final ResourceLocation CACTUS_FIELD_ID         = new ResourceLocation(Constants.MOD_ID, "cactus");
    public static final ResourceLocation BAMBOO_FIELD_ID         = new ResourceLocation(Constants.MOD_ID, "bamboo");
    public static final ResourceLocation COCOA_BEANS_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "cocoa_beans");
    public static final ResourceLocation VINES_FIELD_ID          = new ResourceLocation(Constants.MOD_ID, "vines");
    public static final ResourceLocation KELP_FIELD_ID           = new ResourceLocation(Constants.MOD_ID, "kelp");
    public static final ResourceLocation SEAGRASS_FIELD_ID       = new ResourceLocation(Constants.MOD_ID, "seagrass");
    public static final ResourceLocation SEA_PICKLES_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "sea_pickles");
    public static final ResourceLocation GLOWBERRIES_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "glowberries");
    public static final ResourceLocation WEEPING_VINES_FIELD_ID  = new ResourceLocation(Constants.MOD_ID, "weeping_vines");
    public static final ResourceLocation TWISTING_VINES_FIELD_ID = new ResourceLocation(Constants.MOD_ID, "twisting_vines");
    public static final ResourceLocation CRIMSON_PLANTS_FIELD_ID = new ResourceLocation(Constants.MOD_ID, "crimson_plants");
    public static final ResourceLocation WARPED_PLANTS_FIELD_ID  = new ResourceLocation(Constants.MOD_ID, "warped_plants");

    public static RegistryObject<PlantationFieldRegistries.FieldEntry> sugarCaneField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> cactusField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> bambooField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> cocoaBeansField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> vinesField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> kelpField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> seagrassField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> seaPicklesField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> glowberriesField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> weepingVinesField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> twistingVinesField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> crimsonPlantsField;
    public static RegistryObject<PlantationFieldRegistries.FieldEntry> warpedPlantsField;

    private PlantationFieldRegistries()
    {
    }

    /**
     * Get the plantation field registry.
     *
     * @return the plantation field registry.
     */
    public static IForgeRegistry<PlantationFieldRegistries.FieldEntry> getPlantationFieldRegistry()
    {
        return IMinecoloniesAPI.getInstance().getPlantationFieldRegistry();
    }

    /**
     * Entry for the {@link IField} registry. Makes it possible to create a single registry for a {@link IField}. Used to lookup how to create {@link IField}.
     */
    public static class FieldEntry
    {
        private final ResourceLocation registryName;

        private final @NotNull IPlantationModule plantationModule;

        public FieldEntry(
          final ResourceLocation registryName,
          final @NotNull IPlantationModule plantationModule)
        {
            this.registryName = registryName;
            this.plantationModule = plantationModule;
        }

        /**
         * Get the assigned registry name.
         *
         * @return the resource location.
         */
        public ResourceLocation getRegistryName()
        {
            return registryName;
        }

        /**
         * Gets the plantation module instance of this plantation field registry.
         *
         * @return the field instance.
         */
        @NotNull
        public IPlantationModule getModule()
        {
            return plantationModule;
        }

        @Override
        public int hashCode()
        {
            return registryName.hashCode();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final PlantationFieldRegistries.FieldEntry that = (PlantationFieldRegistries.FieldEntry) o;

            return registryName.equals(that.registryName);
        }
    }
}
