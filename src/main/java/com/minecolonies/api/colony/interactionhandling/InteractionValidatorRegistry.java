package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Utility class to store all validator predicates for the chat handling.
 */
public final class InteractionValidatorRegistry
{
    /**
     * Map of all validator predicates.
     */
    private static Map<Component, Predicate<ICitizenData>> map = new HashMap<>();

    /**
     * Map of all pos based validator predicates.
     */
    private static Map<Component, BiPredicate<ICitizenData, BlockPos>> posMap = new HashMap<>();

    /**
     * Map of all IToken based validator predicates.
     */
    private static Map<Component, BiPredicate<ICitizenData, IToken<?>>> tokenMap = new HashMap<>();

    /**
     * Get the StandardInteractionValidatorPredicate.
     *
     * @param key the key of it.
     * @return the predicate.
     */
    public static Predicate<ICitizenData> getStandardInteractionValidatorPredicate(final Component key)
    {
        return map.get(key);
    }

    /**
     * Get the PosBasedInteractionValidatorPredicate.
     *
     * @param key the key of it.
     * @return the predicate.
     */
    public static BiPredicate<ICitizenData, BlockPos> getPosBasedInteractionValidatorPredicate(final Component key)
    {
        return posMap.get(key);
    }

    /**
     * Get the PosBasedInteractionValidatorPredicate.
     *
     * @param key the key of it.
     * @return the predicate.
     */
    public static BiPredicate<ICitizenData, IToken<?>> getTokenBasedInteractionValidatorPredicate(final Component key)
    {
        return tokenMap.get(key);
    }

    /**
     * Add a new StandardInteractionValidatorPredicate.
     *
     * @param key       it's key.
     * @param predicate it's predicate.
     */
    public static void registerStandardPredicate(final Component key, final Predicate<ICitizenData> predicate)
    {
        map.put(key, predicate);
    }

    /**
     * Add a new PosBasedInteractionValidatorPredicate.
     *
     * @param key       it's key.
     * @param predicate it's predicate.
     */
    public static void registerPosBasedPredicate(final Component key, final BiPredicate<ICitizenData, BlockPos> predicate)
    {
        posMap.put(key, predicate);
    }

    /**
     * Add a new TokenBasedInteractionValidatorPredicate.
     *
     * @param key       it's key.
     * @param predicate it's predicate.
     */
    public static void registerTokenBasedPredicate(final Component key, final BiPredicate<ICitizenData, IToken<?>> predicate)
    {
        tokenMap.put(key, predicate);
    }

    /**
     * Check if there is a validator with a certain key.
     *
     * @param component the key to check.
     * @return true if so.
     */
    public static boolean hasValidator(final MutableComponent component)
    {
        return map.containsKey(component) || posMap.containsKey(component) || tokenMap.containsKey(component);
    }

    /**
     * Private constructor to hide public one.
     */
    private InteractionValidatorRegistry()
    {
        /*
         * Intentionally left empty.
         */
    }
}
