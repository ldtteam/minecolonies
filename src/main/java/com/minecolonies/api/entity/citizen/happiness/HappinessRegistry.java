package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Happiness forge registry to facilitate loading and saving to nbt.
 */
public class HappinessRegistry
{
    /**
     * Get the reward registry.
     *
     * @return the reward registry.
     */
    static IForgeRegistry<HappinessFactorTypeEntry> getHappinessTypeRegistry()
    {
        return IMinecoloniesAPI.getInstance().getHappinessTypeRegistry();
    }

    /**
     * Get the reward registry.
     *
     * @return the reward registry.
     */
    static IForgeRegistry<HappinessFunctionEntry> getHappinessFunctionRegistry()
    {
        return IMinecoloniesAPI.getInstance().getHappinessFunctionRegistry();
    }

    /**
     * Happiness Factor type.
     */
    public static class HappinessFactorTypeEntry
    {
        private final Supplier<IHappinessModifier> supplier;

        public HappinessFactorTypeEntry(final Supplier<IHappinessModifier> productionFunction)
        {
            this.supplier = productionFunction;
        }

        /**
         * Get the modifier.
         *
         * @return the modifier.
         */
        public IHappinessModifier create()
        {
            return supplier.get();
        }
    }

    /**
     * Static getter to load a happiness modifier from a compound.
     *
     * @param compound the compound to load it from.
     * @param persist  whether we're reading from persisted data or from networking.
     * @return the modifier instance.
     */
    @Nullable
    public static IHappinessModifier loadFrom(@NotNull final CompoundTag compound, final boolean persist)
    {
        final ResourceLocation modifierType = ResourceLocation.tryParse(compound.getString(NbtTagConstants.TAG_MODIFIER_TYPE));
        if (modifierType == null || !getHappinessTypeRegistry().containsKey(modifierType))
        {
            Log.getLogger().warn("Unknown Happiness Modifier type '{}', its state cannot be restored.", modifierType);
            return null;
        }

        try
        {
            final HappinessFactorTypeEntry entry = getHappinessTypeRegistry().getValue(modifierType);
            final IHappinessModifier modifier = entry == null ? null : entry.create();
            if (modifier == null)
            {
                Log.getLogger().warn("Happiness Modifier type '{}' has no usable factory, its state cannot be restored.", modifierType);
                return null;
            }

            modifier.read(compound, persist);
            return modifier;
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error("A Happiness Modifier of type '{}' has thrown an exception during loading, its state cannot be restored. Report this to the mod author.",
              modifierType, ex);
            return null;
        }
    }

    /**
     * Happiness Factor type.
     */
    public static class HappinessFunctionEntry
    {
        private final Function<ICitizenData, Double> doubleSupplier;

        /**
         * Create a new entry type.
         *
         * @param doubleSupplier th
         */
        public HappinessFunctionEntry(final Function<ICitizenData, Double> doubleSupplier)
        {
            this.doubleSupplier = doubleSupplier;
        }

        /**
         * Get the double supplier.
         *
         * @return the function.
         */
        public Function<ICitizenData, Double> getDoubleSupplier()
        {
            return doubleSupplier;
        }
    }

    /**
     * Registry ID for happiness modifier types.
     */
    public static final ResourceLocation HAPPINESS_FACTOR_TYPE_REGISTRY_ID = new ResourceLocation(Constants.MOD_ID, "happinessfactortypes");

    /**
     * Registry ID for happiness functions.
     */
    public static final ResourceLocation HAPPINESS_FUNCTION_REGISTRY_ID = new ResourceLocation(Constants.MOD_ID, "happinessfunction");

    public static final ResourceLocation STATIC_MODIFIER      = new ResourceLocation(Constants.MOD_ID, "static");
    public static final ResourceLocation EXPIRATION_MODIFIER  = new ResourceLocation(Constants.MOD_ID, "expiration");
    public static final ResourceLocation TIME_PERIOD_MODIFIER = new ResourceLocation(Constants.MOD_ID, "time");

    public static RegistryObject<HappinessFactorTypeEntry> staticHappinessModifier;
    public static RegistryObject<HappinessFactorTypeEntry> expirationBasedHappinessModifier;
    public static RegistryObject<HappinessFactorTypeEntry> timeBasedHappinessModifier;

    public static final ResourceLocation SCHOOL_FUNCTION        = new ResourceLocation(Constants.MOD_ID, "school");
    public static final ResourceLocation SECURITY_FUNCTION      = new ResourceLocation(Constants.MOD_ID, "security");
    public static final ResourceLocation SOCIAL_FUNCTION        = new ResourceLocation(Constants.MOD_ID, "social");
    public static final ResourceLocation MYSTICAL_SITE_FUNCTION = new ResourceLocation(Constants.MOD_ID, "mystical");

    public static final ResourceLocation HOUSING_FUNCTION      = new ResourceLocation(Constants.MOD_ID, "housing");
    public static final ResourceLocation UNEMPLOYMENT_FUNCTION = new ResourceLocation(Constants.MOD_ID, "unemployment");
    public static final ResourceLocation HEALTH_FUNCTION       = new ResourceLocation(Constants.MOD_ID, "health");
    public static final ResourceLocation IDLEATJOB_FUNCTION    = new ResourceLocation(Constants.MOD_ID, "idleatjob");
    public static final ResourceLocation SLEPTTONIGHT_FUNCTION = new ResourceLocation(Constants.MOD_ID, "slepttonight");
    public static final ResourceLocation FOOD_FUNCTION         = new ResourceLocation(Constants.MOD_ID, "food");

    public static RegistryObject<HappinessFunctionEntry> schoolFunction;
    public static RegistryObject<HappinessFunctionEntry> securityFunction;
    public static RegistryObject<HappinessFunctionEntry> socialFunction;
    public static RegistryObject<HappinessFunctionEntry> mysticalSiteFunction;

    public static RegistryObject<HappinessFunctionEntry> housingFunction;
    public static RegistryObject<HappinessFunctionEntry> unemploymentFunction;
    public static RegistryObject<HappinessFunctionEntry> healthFunction;
    public static RegistryObject<HappinessFunctionEntry> idleatjobFunction;
    public static RegistryObject<HappinessFunctionEntry> sleptTonightFunction;
    public static RegistryObject<HappinessFunctionEntry> foodFunction;
    public static RegistryObject<HappinessFunctionEntry> greatFoodFunction;

}
