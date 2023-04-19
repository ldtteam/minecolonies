package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Happiness forge registry to facilitate loading and saving to nbt.
 */
public class HappinessFactorTypeRegistry
{
    /**
     * Get the reward registry.
     *
     * @return the reward registry.
     */
    static IForgeRegistry<HappinessFactorTypeTypeEntry> getHappinessTypeRegistry()
    {
        return IMinecoloniesAPI.getInstance().getHappinessTypeRegistry();
    }

    /**
     * Happiness Factor type.
     */
    public static class HappinessFactorTypeTypeEntry
    {
        private final Supplier<IHappinessModifier> supplier;

        public HappinessFactorTypeTypeEntry(final Supplier<IHappinessModifier> productionFunction)
        {
            this.supplier = productionFunction;
        }

        /**
         * Create one from json.
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

    public static ResourceLocation STATIC_MODIFIER      = new ResourceLocation(Constants.MOD_ID, "static");
    public static ResourceLocation EXPIRATION_MODIFIER  = new ResourceLocation(Constants.MOD_ID, "expiration");
    public static ResourceLocation TIME_PERIOD_MODIFIER = new ResourceLocation(Constants.MOD_ID, "time");

    public static RegistryObject<HappinessFactorTypeTypeEntry> staticHappinessModifier;
    public static RegistryObject<HappinessFactorTypeTypeEntry> expirationBasedHappinessModifier;
    public static RegistryObject<HappinessFactorTypeTypeEntry> timeBasedHappinessModifier;
}
