package com.minecolonies.api.colony.fields.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.IFieldMatcher;
import com.minecolonies.api.colony.fields.IFieldView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;

public class FieldRegistries
{
    public static final ResourceLocation FARM_FIELD_ID       = new ResourceLocation(Constants.MOD_ID, "farmfield");
    public static final ResourceLocation PLANTATION_FIELD_ID = new ResourceLocation(Constants.MOD_ID, "plantationfield");

    public static RegistryObject<FieldRegistries.FieldEntry> farmField;
    public static RegistryObject<FieldRegistries.FieldEntry> plantationField;

    private FieldRegistries()
    {
    }

    /**
     * Get the field registry.
     *
     * @return the reward registry.
     */
    public static IForgeRegistry<FieldEntry> getFieldRegistry()
    {
        return IMinecoloniesAPI.getInstance().getFieldRegistry();
    }

    /**
     * Entry for the {@link IField} registry. Makes it possible to create a single registry for a {@link IField}. Used to lookup how to create {@link IField} and {@link
     * IFieldView}.
     */
    public static class FieldEntry
    {
        private final ResourceLocation                                registryName;
        private final BiFunction<IColony, BlockPos, IField>           fieldProducer;
        private final BiFunction<IColonyView, BlockPos, IFieldView>   fieldViewProducer;
        private final BiFunction<FieldEntry, BlockPos, IFieldMatcher> fieldMatcherProducer;

        public FieldEntry(
          final ResourceLocation registryName,
          final BiFunction<IColony, BlockPos, IField> fieldProducer,
          final BiFunction<IColonyView, BlockPos, IFieldView> fieldViewProducer,
          final BiFunction<FieldEntry, BlockPos, IFieldMatcher> fieldMatcherProducer)
        {
            this.registryName = registryName;
            this.fieldProducer = fieldProducer;
            this.fieldViewProducer = fieldViewProducer;
            this.fieldMatcherProducer = fieldMatcherProducer;
        }

        public IField produceField(final IColony colony, final BlockPos position)
        {
            final IField field = fieldProducer.apply(colony, position);
            field.setFieldType(this);
            return field;
        }

        public IFieldView produceFieldView(final IColonyView colony, final BlockPos position)
        {
            final IFieldView fieldView = fieldViewProducer.apply(colony, position);
            fieldView.setFieldType(this);
            return fieldView;
        }

        public IFieldMatcher produceFieldMatcher(final BlockPos position)
        {
            return fieldMatcherProducer.apply(this, position);
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
    }
}
