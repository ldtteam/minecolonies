package com.minecolonies.api.research.effects.registry;

import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IResearchEffect <?>} registry. Makes it possible to create a single registry for a {@link IResearchEffect <?>}.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public class ResearchEffectEntry extends ForgeRegistryEntry<ResearchEffectEntry>
{
    private final Function<CompoundNBT, IResearchEffect<?>> readFromNBT;

    /**
     * A builder class for {@link ResearchEffectEntry}.
     */
    public static final class Builder
    {
        private Function<CompoundNBT, IResearchEffect<?>>      readFromNBT;
        private ResourceLocation                               registryName;

        /**
         * Set the function to reconstruct the ResearchEffect during deserialization.
         * This function will be available from ResearchEffectEntry.readFromNBT
         * It must take a CompoundNBT and return an IResearchEffect containing a meaningful Description.
         * Implementing either a Constructor or static method is encouraged.
         * @param readFromNBT  a function taking CompoundNBT and returning an IResearchEffect.
         * @return The builder.
         */
        public Builder setReadFromNBT(final Function<CompoundNBT, IResearchEffect<?>> readFromNBT)
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

            return new ResearchEffectEntry(readFromNBT).setRegistryName(registryName);
        }
    }

    public IResearchEffect<?> readFromNBT(CompoundNBT nbt)
    {
        return readFromNBT.apply(nbt);
    }

    private ResearchEffectEntry(
      final Function<CompoundNBT, IResearchEffect<?>> readFromNBT)
    {
        super();
        this.readFromNBT = readFromNBT;
    }
}
