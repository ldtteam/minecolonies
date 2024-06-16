package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Loot table generator for expeditions.
 */
public class DefaultExpeditionLootProvider extends SimpleLootTableProvider
{
    /**
     * Expedition constants.
     */
    public static final ResourceLocation EXPEDITION_OVERWORLD_LOOT = new ResourceLocation(Constants.MOD_ID, "expedition_overworld");
    public static final ResourceLocation EXPEDITION_NETHER_LOOT    = new ResourceLocation(Constants.MOD_ID, "expedition_nether");
    public static final ResourceLocation EXPEDITION_END_LOOT       = new ResourceLocation(Constants.MOD_ID, "expedition_end");

    /**
     * Default constructor.
     */
    public DefaultExpeditionLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Loot";
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {

    }
}
