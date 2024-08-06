package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registering of sound events for our colony.
 */
public final class ModBannerPatterns
{
    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, Constants.MOD_ID);

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBannerPatterns()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Register the {@link SoundEvent}s.
     *
     * @param registry the registry to register at.
     */
    static
    {
        BANNER_PATTERNS.register("horse", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "hsy"), "com.minecolonies.core.pattern.horse"));
        BANNER_PATTERNS.register("eagle", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "egl"), "com.minecolonies.core.pattern.eagle"));
        BANNER_PATTERNS.register("lion", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "lin"), "com.minecolonies.core.pattern.lion"));
        BANNER_PATTERNS.register("tower", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "twr"), "com.minecolonies.core.pattern.tower"));
        BANNER_PATTERNS.register("bear", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "ber"), "com.minecolonies.core.pattern.bear"));
        BANNER_PATTERNS.register("fleur", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "flr"), "com.minecolonies.core.pattern.fleur"));
        BANNER_PATTERNS.register("tinycross", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "tcr"), "com.minecolonies.core.pattern.tinycross"));
        BANNER_PATTERNS.register("cantabrian", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "ctbr"), "com.minecolonies.core.pattern.cantabrian"));
        BANNER_PATTERNS.register("threetriangles", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "ttan"), "com.minecolonies.core.pattern.threetriangles"));
        BANNER_PATTERNS.register("theart", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "thrt"), "com.minecolonies.core.pattern.theart"));
        BANNER_PATTERNS.register("dragonhead", () -> new BannerPattern(new ResourceLocation(Constants.MOD_ID, "drhd"), "com.minecolonies.core.pattern.dragonhead"));
    }
}
