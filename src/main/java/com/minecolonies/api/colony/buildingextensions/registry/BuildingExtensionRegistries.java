package com.minecolonies.api.colony.buildingextensions.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.modules.IBuildingExtensionModule;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Registry implementation for building extension instances.
 */
public class BuildingExtensionRegistries
{
    public static final ResourceLocation FARM_FIELD_ID                      = new ResourceLocation(Constants.MOD_ID, "farmfield");
    public static final ResourceLocation PLANTATION_SUGAR_CANE_FIELD_ID     = new ResourceLocation(Constants.MOD_ID, "plantation_sugar_cane");
    public static final ResourceLocation PLANTATION_CACTUS_FIELD_ID         = new ResourceLocation(Constants.MOD_ID, "plantation_cactus");
    public static final ResourceLocation PLANTATION_BAMBOO_FIELD_ID         = new ResourceLocation(Constants.MOD_ID, "plantation_bamboo");
    public static final ResourceLocation PLANTATION_COCOA_BEANS_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "plantation_cocoa_beans");
    public static final ResourceLocation PLANTATION_VINES_FIELD_ID          = new ResourceLocation(Constants.MOD_ID, "plantation_vines");
    public static final ResourceLocation PLANTATION_KELP_FIELD_ID           = new ResourceLocation(Constants.MOD_ID, "plantation_kelp");
    public static final ResourceLocation PLANTATION_SEAGRASS_FIELD_ID       = new ResourceLocation(Constants.MOD_ID, "plantation_seagrass");
    public static final ResourceLocation PLANTATION_SEA_PICKLES_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "plantation_sea_pickles");
    public static final ResourceLocation PLANTATION_GLOWBERRIES_FIELD_ID    = new ResourceLocation(Constants.MOD_ID, "plantation_glowberries");
    public static final ResourceLocation PLANTATION_WEEPING_VINES_FIELD_ID  = new ResourceLocation(Constants.MOD_ID, "plantation_weeping_vines");
    public static final ResourceLocation PLANTATION_TWISTING_VINES_FIELD_ID = new ResourceLocation(Constants.MOD_ID, "plantation_twisting_vines");
    public static final ResourceLocation PLANTATION_CRIMSON_PLANTS_FIELD_ID = new ResourceLocation(Constants.MOD_ID, "plantation_crimson_plants");
    public static final ResourceLocation PLANTATION_WARPED_PLANTS_FIELD_ID  = new ResourceLocation(Constants.MOD_ID, "plantation_warped_plants");

    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> farmField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationSugarCaneField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationCactusField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationBambooField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationCocoaBeansField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationVinesField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationKelpField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationSeagrassField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationSeaPicklesField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationGlowberriesField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationWeepingVinesField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationTwistingVinesField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationCrimsonPlantsField;
    public static DeferredHolder<BuildingExtensionEntry, BuildingExtensionEntry> plantationWarpedPlantsField;

    private BuildingExtensionRegistries()
    {
    }

    /**
     * Get the building extension registry.
     *
     * @return the building extension registry.
     */
    public static Registry<BuildingExtensionEntry> getBuildingExtensionRegistry()
    {
        return IMinecoloniesAPI.getInstance().getBuildingExtensionRegistry();
    }


    /**
     * Entry for the {@link IBuildingExtension} registry. Makes it possible to create a single registry for a {@link IBuildingExtension}. Used to lookup how to create {@link IBuildingExtension}.
     */
    public static class BuildingExtensionEntry
    {
        private final ResourceLocation                                                 registryName;
        private final BiFunction<BuildingExtensionEntry, BlockPos, IBuildingExtension> extensionProducer;
        private final List<Function<IBuildingExtension, IBuildingExtensionModule>>     extensionModuleProducers;

        /**
         * Default internal constructor.
         */
        private BuildingExtensionEntry(
          final ResourceLocation registryName,
          final BiFunction<BuildingExtensionEntry, BlockPos, IBuildingExtension> extensionProducer,
          final List<Function<IBuildingExtension, IBuildingExtensionModule>> extensionModuleProducers)
        {
            this.registryName = registryName;
            this.extensionProducer = extensionProducer;
            this.extensionModuleProducers = extensionModuleProducers;
        }

        /**
         * Produces a building extension instance based on a colony and block pos.
         *
         * @param position the position the building extension is at.
         * @return the building extension instance.
         */
        public IBuildingExtension produceExtension(final BlockPos position)
        {
            final IBuildingExtension extension = extensionProducer.apply(this, position);
            for (final Function<IBuildingExtension, IBuildingExtensionModule> moduleProducer : extensionModuleProducers)
            {
                extension.registerModule(moduleProducer.apply(extension));
            }
            return extension;
        }

        /**
         * Get all building extension module producers.
         *
         * @return a list of all the building extension module producers.
         */
        public List<Function<IBuildingExtension, IBuildingExtensionModule>> getExtensionModuleProducers()
        {
            return Collections.unmodifiableList(extensionModuleProducers);
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

            final BuildingExtensionEntry that = (BuildingExtensionEntry) o;

            return registryName.equals(that.registryName);
        }

        /**
         * A builder class for {@link BuildingExtensionEntry}.
         */
        public static class Builder
        {
            private final List<Function<IBuildingExtension, IBuildingExtensionModule>>     extensionModuleProducers = new ArrayList<>();
            private       ResourceLocation                                                 registryName;
            private       BiFunction<BuildingExtensionEntry, BlockPos, IBuildingExtension> extensionProducer;

            /**
             * Sets the registry name for the new building extension entry.
             *
             * @param registryName The name for the registry entry.
             * @return The builder.
             */
            public BuildingExtensionEntry.Builder setRegistryName(final ResourceLocation registryName)
            {
                this.registryName = registryName;
                return this;
            }

            /**
             * Sets the callback that is used to create the {@link IBuildingExtension} from its position in the world.
             *
             * @param extensionProducer The callback used to create the {@link IBuildingExtension}.
             * @return The builder.
             */
            public BuildingExtensionEntry.Builder setExtensionProducer(final BiFunction<BuildingExtensionEntry, BlockPos, IBuildingExtension> extensionProducer)
            {
                this.extensionProducer = extensionProducer;
                return this;
            }

            /**
             * Add a building extension module producer.
             *
             * @param moduleProducer the module producer.
             * @return the builder again.
             */
            public BuildingExtensionEntry.Builder addExtensionModuleProducer(final Function<IBuildingExtension, IBuildingExtensionModule> moduleProducer)
            {
                extensionModuleProducers.add(moduleProducer);
                return this;
            }

            /**
             * Method used to create the entry.
             *
             * @return The entry.
             */
            public BuildingExtensionEntry createExtensionEntry()
            {
                Validate.notNull(registryName);
                Validate.notNull(extensionProducer);

                return new BuildingExtensionEntry(registryName, extensionProducer, extensionModuleProducers);
            }
        }
    }
}
