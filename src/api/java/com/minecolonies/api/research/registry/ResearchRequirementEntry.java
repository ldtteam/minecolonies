package com.minecolonies.api.research.registry;

import com.minecolonies.api.research.IResearchRequirement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IResearchRequirement} registry. Makes it possible to create a single registry for a {@link IResearchRequirement}.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public class ResearchRequirementEntry
{
    private final Function<CompoundTag, IResearchRequirement> readFromNBT;
    private final ResourceLocation registryname;

    /**
     * A builder class for {@link ResearchRequirementEntry}.
     */
    public static final class Builder
    {
        private Function<CompoundTag, IResearchRequirement> readFromNBT;
        private ResourceLocation                            registryName;

        /**
         * Set the function to reconstruct the ResearchRequirement during deserialization.
         * This function will be available from ResearchRequirementEntry.readFromNBT
         * It must take a CompoundTag and return an IResearchRequirement containing a meaningful Description:
         * implementing either a Constructor or static method is encouraged.
         * @param readFromNBT  a function taking CompoundTag and returning an IResearchRequirement.
         * @return The builder.
         */
        public Builder setReadFromNBT(final Function<CompoundTag, IResearchRequirement> readFromNBT)
        {
            this.readFromNBT = readFromNBT;
            return this;
        }

        /**
         * Sets the registry name for the new ResearchRequirement.
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
        public ResearchRequirementEntry createResearchRequirementEntry()
        {
            Validate.notNull(readFromNBT);
            Validate.notNull(registryName);

            return new ResearchRequirementEntry(readFromNBT, registryName);
        }
    }

    public IResearchRequirement readFromNBT(CompoundTag nbt)
    {
        return readFromNBT.apply(nbt);
    }

    private ResearchRequirementEntry(
      final Function<CompoundTag, IResearchRequirement> readFromNBT, final ResourceLocation registryName)
    {
        super();
        this.readFromNBT = readFromNBT;
        this.registryname = registryName;
    }

    public ResourceLocation getRegistryName()
    {
        return registryname;
    }
}
