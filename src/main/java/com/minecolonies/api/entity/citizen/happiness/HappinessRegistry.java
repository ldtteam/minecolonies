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
     * @return the modifier instance.
     */
    public static IHappinessModifier loadFrom(@NotNull final CompoundTag compound)
    {
        final ResourceLocation modifierType = compound.contains(NbtTagConstants.TAG_MODIFIER_TYPE)
                                                ? new ResourceLocation(compound.getString(NbtTagConstants.TAG_MODIFIER_TYPE))
                                                : new ResourceLocation(Constants.MOD_ID, "null");
        final IHappinessModifier modifier = getHappinessTypeRegistry().getValue(modifierType).create();

        if (modifier != null)
        {
            try
            {
                modifier.read(compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger()
                  .error(String.format("A Happiness Modifier %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                    modifierType), ex);
                return null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Happiness Modifier type '%s' or missing constructor of proper format.", modifierType));
        }

        return modifier;
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

    public static ResourceLocation STATIC_MODIFIER      = new ResourceLocation(Constants.MOD_ID, "static");
    public static ResourceLocation EXPIRATION_MODIFIER  = new ResourceLocation(Constants.MOD_ID, "expiration");
    public static ResourceLocation TIME_PERIOD_MODIFIER = new ResourceLocation(Constants.MOD_ID, "time");

    public static RegistryObject<HappinessFactorTypeEntry> staticHappinessModifier;
    public static RegistryObject<HappinessFactorTypeEntry> expirationBasedHappinessModifier;
    public static RegistryObject<HappinessFactorTypeEntry> timeBasedHappinessModifier;

    public static ResourceLocation SCHOOL_FUNCTION        = new ResourceLocation(Constants.MOD_ID, "school");
    public static ResourceLocation SECURITY_FUNCTION      = new ResourceLocation(Constants.MOD_ID, "security");
    public static ResourceLocation SOCIAL_FUNCTION        = new ResourceLocation(Constants.MOD_ID, "social");
    public static ResourceLocation MYSTICAL_SITE_FUNCTION = new ResourceLocation(Constants.MOD_ID, "mystical");

    public static ResourceLocation HOUSING_FUNCTION      = new ResourceLocation(Constants.MOD_ID, "housing");
    public static ResourceLocation UNEMPLOYMENT_FUNCTION = new ResourceLocation(Constants.MOD_ID, "unemployment");
    public static ResourceLocation HEALTH_FUNCTION       = new ResourceLocation(Constants.MOD_ID, "health");
    public static ResourceLocation IDLEATJOB_FUNCTION    = new ResourceLocation(Constants.MOD_ID, "idleatjob");
    public static ResourceLocation SLEPTTONIGHT_FUNCTION = new ResourceLocation(Constants.MOD_ID, "slepttonight");
    public static ResourceLocation FOOD_FUNCTION         = new ResourceLocation(Constants.MOD_ID, "food");

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
