package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registering of sound events for our colony.
 */
public final class ModBannerPatterns
{
    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registry.BANNER_PATTERN_REGISTRY, Constants.MOD_ID);

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
        BANNER_PATTERNS.register("horse", () -> new BannerPattern("hsy"));
        BANNER_PATTERNS.register("eagle", () -> new BannerPattern("egl"));
        BANNER_PATTERNS.register("lion", () -> new BannerPattern("lin"));
        BANNER_PATTERNS.register("tower", () -> new BannerPattern("twr"));
        BANNER_PATTERNS.register("bear", () -> new BannerPattern("ber"));
        BANNER_PATTERNS.register("fleur", () -> new BannerPattern("flr"));
        BANNER_PATTERNS.register("tinycross", () -> new BannerPattern("tcr"));
        BANNER_PATTERNS.register("cantabrian", () -> new BannerPattern("ctbr"));
    }
}
