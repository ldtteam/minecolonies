package com.minecolonies.api.research.effects.registry;

import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IResearchEffect} registry. Makes it possible to create a single registry for a {@link IResearchEffect}.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public class ResearchEffectEntry
{
    private final Function<CompoundTag, IResearchEffect<?>> readFromNBT;
    private final ResourceLocation registryName;

    /**
     * A builder class for {@link ResearchEffectEntry}.
     */
    public static final class Builder
    {
        private Function<CompoundTag, IResearchEffect<?>>      readFromNBT;
        private ResourceLocation                               registryName;

        /**
         * Set the function to reconstruct the ResearchEffect during deserialization.
         * This function will be available from ResearchEffectEntry.readFromNBT
         * It must take a CompoundTag and return an IResearchEffect containing a meaningful Description.
         * Implementing either a Constructor or static method is encouraged.
         * @param readFromNBT  a function taking CompoundTag and returning an IResearchEffect.
         * @return The builder.
         */
        public Builder setReadFromNBT(final Function<CompoundTag, IResearchEffect<?>> readFromNBT)
        {
            this.readFromNBT = readFromNBT;
            return this;
        }

        /**
         * Sets the registry name for the new ResearchEffect.
         *
         * @param registryName The name for the registry entry.
         * @return The builder.
         */
        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Method used to create the entry.
         *
         * @return The entry.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder explicitly allowed to create an instance.
        public ResearchEffectEntry createResearchEffectEntry()
        {
            Validate.notNull(readFromNBT);
            Validate.notNull(registryName);

            return new ResearchEffectEntry(readFromNBT, registryName);
        }
    }

    public IResearchEffect<?> readFromNBT(CompoundTag nbt)
    {
        return readFromNBT.apply(nbt);
    }

    private ResearchEffectEntry(final Function<CompoundTag, IResearchEffect<?>> readFromNBT, final ResourceLocation registryName)
    {
        super();
        this.readFromNBT = readFromNBT;
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName()
    {
        return registryName;
    }
}
