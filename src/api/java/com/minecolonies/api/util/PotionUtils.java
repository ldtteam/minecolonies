package com.minecolonies.api.util;

import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility methods regarding Potions
 */
public final class PotionUtils
{
    private static final Map<ResourceLocation, Integer> HARM_POTIONS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> HEAL_POTIONS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> BUFF_POTIONS = new HashMap<>();
    static
    {
        //heal
        registerHealPotion(Potions.HEALING, 1);
        registerHealPotion(Potions.STRONG_HEALING, 2);
        registerHealPotion(Potions.REGENERATION, 3);
        registerHealPotion(Potions.LONG_REGENERATION, 4);
        registerHealPotion(Potions.STRONG_REGENERATION, 5);

        //buff
        registerBuffPotion(Potions.SWIFTNESS, 1);
        registerBuffPotion(Potions.LONG_SWIFTNESS, 2);
        registerBuffPotion(Potions.STRONG_SWIFTNESS, 3);
        registerBuffPotion(Potions.STRENGTH, 2);
        registerBuffPotion(Potions.STRONG_STRENGTH, 5);
        registerBuffPotion(Potions.LONG_STRENGTH, 5);
        registerBuffPotion(Potions.FIRE_RESISTANCE, 4);
        registerBuffPotion(Potions.LONG_FIRE_RESISTANCE, 5);

        //harm
        registerHarmPotion(Potions.HARMING, 1);
        registerHarmPotion(Potions.STRONG_HARMING, 2);
        registerHarmPotion(Potions.POISON, 3);
        registerHarmPotion(Potions.LONG_POISON, 4);
        registerHarmPotion(Potions.STRONG_POISON, 5);
    }
    private PotionUtils() {}

    /**
     * Register a {@link Potion} as a harming potion with a given required level.
     *
     * @param potion the {@link Potion} to register
     * @param level  the minimum required level to use this potion
     */
    public static void registerHarmPotion(@NotNull final Potion potion, final int level)
    {
        Objects.requireNonNull(potion);
        Objects.requireNonNull(potion.getRegistryName());
        if (level <= 0)
        {
            throw new IllegalArgumentException("Can't register potion with level <= 0");
        }
        HARM_POTIONS.put(potion.getRegistryName(), level);
    }

    /**
     * Register a {@link Potion} as a healing potion with a given required level.
     *
     * @param potion the {@link Potion} to register
     * @param level  the minimum required level to use this potion
     */
    public static void registerHealPotion(@NotNull final Potion potion, final int level)
    {
        Objects.requireNonNull(potion);
        Objects.requireNonNull(potion.getRegistryName());
        if (level <= 0)
        {
            throw new IllegalArgumentException("Can't register potion with level <= 0");
        }
        HEAL_POTIONS.put(potion.getRegistryName(), level);
    }

    /**
     * Register a {@link Potion} as a buffing potion with a given required level.
     *
     * @param potion the {@link Potion} to register
     * @param level  the minimum required level to use this potion
     */
    public static void registerBuffPotion(@NotNull final Potion potion, final int level)
    {
        Objects.requireNonNull(potion);
        Objects.requireNonNull(potion.getRegistryName());
        if (level <= 0)
        {
            throw new IllegalArgumentException("Can't register potion with level <= 0");
        }
        BUFF_POTIONS.put(potion.getRegistryName(), level);
    }

    /**
     * Get the {@link Collection} of registered harming potions registry names
     *
     * @return {@link Collection} of registered harming potions registry names
     */
    @NotNull
    public static Collection<ResourceLocation> getHarmPotions()
    {
        return HARM_POTIONS.keySet();
    }

    /**
     * Get the {@link Collection} of registered healing potions registry names
     *
     * @return {@link Collection} of registered healing potions registry names
     */
    @NotNull
    public static Collection<ResourceLocation> getHealPotions()
    {
        return HEAL_POTIONS.keySet();
    }

    /**
     * Get the {@link Collection} of registered buffing potions registry names
     *
     * @return {@link Collection} of registered buffing potions registry names
     */
    @NotNull
    public static Collection<ResourceLocation> getBuffPotions()
    {
        return BUFF_POTIONS.keySet();
    }

    /**
     * Checks if the given potion is a registered harming potion
     *
     * @param potion the {@link Potion} to check
     * @return true if so
     */
    public static boolean isHarmPotion(@NotNull final Potion potion)
    {
        return HARM_POTIONS.containsKey(potion.getRegistryName());
    }

    /**
     * Checks if the given potion is a registered healing potion
     *
     * @param potion the {@link Potion} to check
     * @return true if so
     */
    public static boolean isHealPotion(@NotNull final Potion potion)
    {
        return HEAL_POTIONS.containsKey(potion.getRegistryName());
    }

    /**
     * Checks if the given potion is a registered buffing potion
     *
     * @param potion the {@link Potion} to check
     * @return true if so
     */
    public static boolean isBuffPotion(@NotNull final Potion potion)
    {
        return BUFF_POTIONS.containsKey(potion.getRegistryName());
    }

    /**
     * Gets the level of the given potion
     *
     * @param potion the potion to get the level for
     * @return the level of the given potion if it is registered oterwise returns 1
     */
    public static int getPotionLevel(@NotNull final Potion potion)
    {
        if (isHarmPotion(potion))
        {
            return HARM_POTIONS.get(potion.getRegistryName());
        }
        else if (isHealPotion(potion))
        {
            return HEAL_POTIONS.get(potion.getRegistryName());
        }
        else if (isBuffPotion(potion))
        {
            return BUFF_POTIONS.get(potion.getRegistryName());
        }
        else
        {
            return 1;
        }
    }
}
