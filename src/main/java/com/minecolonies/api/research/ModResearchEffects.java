package com.minecolonies.api.research;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 * Contains a list of research effects by type. Currently only supports absolute modifiers through Global Research Effect.
 */
public class ModResearchEffects
{
    public static final ResourceLocation GLOBAL_EFFECT_ID = new ResourceLocation(Constants.MOD_ID, "global");

    public static RegistryObject<ResearchEffectEntry> globalResearchEffect;

    public ModResearchEffects() {throw new IllegalStateException("Tried to initialize: ModResearchEffects, but this is a Utility class.");}

    /**
     * Functional interface used in reading the costs from nbt.
     */
    @FunctionalInterface
    public interface ReadFromNBTFunction
    {
        IResearchEffect read(final CompoundTag compound);
    }

    /**
     * Entry for the {@link IResearchEffect} registry. Makes it possible to create a single registry for a {@link IResearchEffect}.
     */
    public static class ResearchEffectEntry
    {
        /**
         * The registry name for this entry.
         */
        private final ResourceLocation registryName;

        /**
         * Function to read this item from NBT.
         */
        private final ReadFromNBTFunction readFromNBT;

        /**
         * Default constructor.
         *
         * @param registryName the registry name for this entry.
         * @param readFromNBT  function to read this item from json.
         */
        public ResearchEffectEntry(
            final ResourceLocation registryName,
            final ReadFromNBTFunction readFromNBT)
        {
            this.registryName = registryName;
            this.readFromNBT = readFromNBT;
        }

        /**
         * Get the registry name for this entry.
         */
        public ResourceLocation getRegistryName()
        {
            return registryName;
        }

        /**
         * Read a research cost instance from NBT.
         */
        public IResearchEffect readFromNBT(final CompoundTag nbt)
        {
            return readFromNBT.read(nbt);
        }
    }
}
