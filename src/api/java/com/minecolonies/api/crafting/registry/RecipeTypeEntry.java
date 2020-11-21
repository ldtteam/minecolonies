package com.minecolonies.api.crafting.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.AbstractRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link AbstractRecipeType} registry.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public final class RecipeTypeEntry extends ForgeRegistryEntry<RecipeTypeEntry>
{

    private final Function<IRecipeStorage, AbstractRecipeType<IRecipeStorage>> recipeTypeProducer;

    /**
     * Builder for a {@link AbstractRecipeType}.
     */
    public static final class Builder
    {
        private Function<IRecipeStorage, AbstractRecipeType<IRecipeStorage>> recipeTypeProducer;
        private ResourceLocation                registryName;

        /**
         * Setter the for the producer.
         *
         * @param recipeTypeProducer The producer for {@link AbstractRecipeType}.
         * @return The builder.
         */
        public Builder setRecipeTypeProducer(final Function<IRecipeStorage, AbstractRecipeType<IRecipeStorage>> recipeTypeProducer)
        {
            this.recipeTypeProducer = recipeTypeProducer;
            return this;
        }

        /**
         * Setter for the registry name.
         *
         * @param registryName The registry name.
         * @return The builder.
         */
        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Creates a new {@link RecipeTypeEntry} builder.
         *
         * @return The created {@link RecipeTypeEntry}.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder is explicitly allowed to create one.
        public RecipeTypeEntry createRecipeTypeEntry()
        {
            Validate.notNull(recipeTypeProducer);
            Validate.notNull(registryName);

            return new RecipeTypeEntry(recipeTypeProducer).setRegistryName(registryName);
        }
    }

    /**
     * The producer for the {@link AbstractRecipeType}. Creates the job from a {@link ICitizenData} instance.
     *
     * @return The created {@link AbstractRecipeType}.
     */
    public Function<IRecipeStorage, AbstractRecipeType<IRecipeStorage>> getHandlerProducer()
    {
        return recipeTypeProducer;
    }

    private RecipeTypeEntry(
      final Function<IRecipeStorage, AbstractRecipeType<IRecipeStorage>> recipeTypeProducer)
    {
        super();
        this.recipeTypeProducer = recipeTypeProducer;
    }
}
