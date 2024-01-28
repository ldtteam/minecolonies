package com.minecolonies.api.colony.fields.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.modules.IFieldModule;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Registry implementation for field instances.
 */
public class FieldRegistries
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

    public static RegistryObject<FieldRegistries.FieldEntry> farmField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationSugarCaneField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationCactusField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationBambooField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationCocoaBeansField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationVinesField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationKelpField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationSeagrassField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationSeaPicklesField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationGlowberriesField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationWeepingVinesField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationTwistingVinesField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationCrimsonPlantsField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationWarpedPlantsField;

    private FieldRegistries()
    {
    }

    /**
     * Get the field registry.
     *
     * @return the field registry.
     */
    public static IForgeRegistry<FieldEntry> getFieldRegistry()
    {
        return IMinecoloniesAPI.getInstance().getFieldRegistry();
    }

    /**
     * Entry for the {@link IField} registry. Makes it possible to create a single registry for a {@link IField}. Used to lookup how to create {@link IField}.
     */
    public static class FieldEntry
    {
        private final ResourceLocation                         registryName;
        private final BiFunction<FieldEntry, BlockPos, IField> fieldProducer;
        private final List<Function<IField, IFieldModule>>     fieldModuleProducers;

        /**
         * Default internal constructor.
         */
        private FieldEntry(
          final ResourceLocation registryName,
          final BiFunction<FieldEntry, BlockPos, IField> fieldProducer,
          final List<Function<IField, IFieldModule>> fieldModuleProducers)
        {
            this.registryName = registryName;
            this.fieldProducer = fieldProducer;
            this.fieldModuleProducers = fieldModuleProducers;
        }

        /**
         * Produces a field instance based on a colony and block pos.
         *
         * @param position the position the field is at.
         * @return the field instance.
         */
        public IField produceField(final BlockPos position)
        {
            final IField field = fieldProducer.apply(this, position);
            for (final Function<IField, IFieldModule> moduleProducer : fieldModuleProducers)
            {
                field.registerModule(moduleProducer.apply(field));
            }
            return field;
        }

        /**
         * Get all field module producers.
         *
         * @return a list of all the field module producers.
         */
        public List<Function<IField, IFieldModule>> getFieldModuleProducers()
        {
            return Collections.unmodifiableList(fieldModuleProducers);
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

            final FieldEntry that = (FieldEntry) o;

            return registryName.equals(that.registryName);
        }

        /**
         * A builder class for {@link FieldEntry}.
         */
        public static class Builder
        {
            private final List<Function<IField, IFieldModule>>     fieldModuleProducers = new ArrayList<>();
            private       ResourceLocation                         registryName;
            private       BiFunction<FieldEntry, BlockPos, IField> fieldProducer;

            /**
             * Sets the registry name for the new field entry.
             *
             * @param registryName The name for the registry entry.
             * @return The builder.
             */
            public FieldEntry.Builder setRegistryName(final ResourceLocation registryName)
            {
                this.registryName = registryName;
                return this;
            }

            /**
             * Sets the callback that is used to create the {@link IField} from its position in the world.
             *
             * @param fieldProducer The callback used to create the {@link IField}.
             * @return The builder.
             */
            public FieldEntry.Builder setFieldProducer(final BiFunction<FieldEntry, BlockPos, IField> fieldProducer)
            {
                this.fieldProducer = fieldProducer;
                return this;
            }

            /**
             * Add a field module producer.
             *
             * @param moduleProducer the module producer.
             * @return the builder again.
             */
            public FieldEntry.Builder addFieldModuleProducer(final Function<IField, IFieldModule> moduleProducer)
            {
                fieldModuleProducers.add(moduleProducer);
                return this;
            }

            /**
             * Method used to create the entry.
             *
             * @return The entry.
             */
            public FieldEntry createFieldEntry()
            {
                Validate.notNull(registryName);
                Validate.notNull(fieldProducer);

                return new FieldEntry(registryName, fieldProducer, fieldModuleProducers);
            }
        }
    }
}
