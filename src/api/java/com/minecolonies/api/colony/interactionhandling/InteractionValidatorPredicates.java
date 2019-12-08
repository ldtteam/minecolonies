package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Utility class to store all validator predicates for the chat handling.
 */
public final class InteractionValidatorPredicates
{
    /**
     * Map of all validator predicates.
     */
    private static Map<IInteractionIdentifier, Predicate<ICitizenData>> map = new HashMap<>();

    /**
     * Map of all pos based validator predicates.
     */
    private static Map<IInteractionIdentifier, BiPredicate<ICitizenData, BlockPos>> posMap = new HashMap<>();

    /**
     * Map of all IToken based validator predicates.
     */
    private static Map<IInteractionIdentifier, BiPredicate<ICitizenData, IToken>> tokenMap = new HashMap<>();

    /**
     * Get the StandardInteractionValidatorPredicate.
     * @param key the key of it.
     * @return the predicate.
     */
    public static Predicate<ICitizenData> getStandardInteractionValidatorPredicate(final IInteractionIdentifier key)
    {
        return map.get(key);
    }

    /**
     * Get the PosBasedInteractionValidatorPredicate.
     * @param key the key of it.
     * @return the predicate.
     */
    public static BiPredicate<ICitizenData, BlockPos> getPosBasedInteractionValidatorPredicate(final IInteractionIdentifier key)
    {
        return posMap.get(key);
    }

    /**
     * Get the PosBasedInteractionValidatorPredicate.
     * @param key the key of it.
     * @return the predicate.
     */
    public static BiPredicate<ICitizenData, IToken> getTokenBasedInteractionValidatorPredicate(final IInteractionIdentifier key)
    {
        return tokenMap.get(key);
    }

    /**
     * Add a new StandardInteractionValidatorPredicate.
     * @param key it's key.
     * @param predicate it's predicate.
     */
    public static void registerStandardPredicate(final IInteractionIdentifier key, final Predicate<ICitizenData> predicate)
    {
        map.put(key, predicate);
    }

    /**
     * Add a new PosBasedInteractionValidatorPredicate.
     * @param key it's key.
     * @param predicate it's predicate.
     */
    public static void registerPosBasedPredicate(final IInteractionIdentifier key, final BiPredicate<ICitizenData, BlockPos> predicate)
    {
        posMap.put(key, predicate);
    }

    /**
     * Add a new TokenBasedInteractionValidatorPredicate.
     * @param key it's key.
     * @param predicate it's predicate.
     */
    public static void registerTokenBasedPredicate(final IInteractionIdentifier key, final BiPredicate<ICitizenData, IToken> predicate)
    {
        tokenMap.put(key, predicate);
    }

    /**
     * Private constructor to hide public one.
     */
    private InteractionValidatorPredicates()
    {
        /*
         * Intentionally left empty.
         */
    }
}
