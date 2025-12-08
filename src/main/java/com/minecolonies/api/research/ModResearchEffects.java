package com.minecolonies.api.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.research.BuildingResearchEffect;
import com.minecolonies.core.research.CitizenCapResearchEffect;
import com.minecolonies.core.research.GlobalResearchEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Contains a list of research effects by type. Currently only supports absolute modifiers through Global Research Effect.
 */
public class ModResearchEffects
{
    public final static DeferredRegister<ResearchEffectEntry> DEFERRED_REGISTER =
        DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researcheffecttypes"), Constants.MOD_ID);

    public static final ResourceLocation GLOBAL_EFFECT_ID      = new ResourceLocation(Constants.MOD_ID, "global");
    public static final ResourceLocation CITIZEN_CAP_EFFECT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_cap");
    public static final ResourceLocation BUILDING_EFFECT_ID    = new ResourceLocation(Constants.MOD_ID, "building");

    public static RegistryObject<ResearchEffectEntry> globalResearchEffect = create(GLOBAL_EFFECT_ID, GlobalResearchEffect::new, GlobalResearchEffect::new);

    public static RegistryObject<ResearchEffectEntry> citizenCapResearchEffect = create(CITIZEN_CAP_EFFECT_ID, CitizenCapResearchEffect::new, CitizenCapResearchEffect::new);

    public static RegistryObject<ResearchEffectEntry> buildingResearchEffect = create(BUILDING_EFFECT_ID, BuildingResearchEffect::new, BuildingResearchEffect::new);

    public ModResearchEffects()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchEffects, but this is a Utility class.");
    }

    /**
     * Utility method to aid in the creation of a research effect.
     *
     * @param registryName the registry name for this entry.
     * @param readFromNBT  function to read this item from json.
     * @return the finalized registry object.
     */
    private static RegistryObject<ResearchEffectEntry> create(final ResourceLocation registryName, final ReadFromNBTFunction readFromNBT, final ReadFromJsonFunction readFromJson)
    {
        return DEFERRED_REGISTER.register(registryName.getPath(), () -> new ResearchEffectEntry(registryName, readFromNBT, readFromJson));
    }

    /**
     * Functional interface used in reading the costs from nbt.
     */
    @FunctionalInterface
    public interface ReadFromNBTFunction
    {
        IResearchEffect read(final CompoundTag compound);
    }

    /**
     * Functional interface used in reading the costs from json.
     */
    @FunctionalInterface
    public interface ReadFromJsonFunction
    {
        IResearchEffect read(final ResourceLocation effectId, final int effectLevel, final JsonObject jsonObject);
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
         * Function to read this item from JSON.
         */
        private final ReadFromJsonFunction readFromJson;

        /**
         * Default constructor.
         *
         * @param registryName the registry name for this entry.
         * @param readFromNBT  function to read this item from json.
         */
        public ResearchEffectEntry(final ResourceLocation registryName, final ReadFromNBTFunction readFromNBT, final ReadFromJsonFunction readFromJson)
        {
            this.registryName = registryName;
            this.readFromNBT = readFromNBT;
            this.readFromJson = readFromJson;
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

        /**
         * Read a research cost instance from json.
         */
        public IResearchEffect readFromJson(final ResourceLocation effectId, final int effectLevel, final JsonObject json)
        {
            return readFromJson.read(effectId, effectLevel, json);
        }
    }
}
